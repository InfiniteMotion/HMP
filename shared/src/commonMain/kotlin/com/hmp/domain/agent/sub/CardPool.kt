package com.hmp.domain.agent.sub

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * HelloSubAgent 卡片池（W0）。
 *
 * 设计：
 * - 同类型只保留一张（replace 语义）
 * - push 到栈顶（列表头），只有头卡可见
 * - 倒计时到期自动 pop（协程 delay 实现，无 Android CountDownTimer 依赖）
 * - 卡展开时暂停倒计时，收起时恢复
 * - 常驻卡（displayDurationMs=0）不创建 timer
 *
 * 线程安全：
 * - cards 通过 StateFlow.update {} 原子化
 * - timerJobs / pausedCards 由 Mutex 保护（D1 修复：iOS/Kotlin Native 上 mutableMapOf 不是线程安全的）
 */
class CardPool(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _cards = MutableStateFlow<List<SlideCard>>(emptyList())
    val cards: StateFlow<List<SlideCard>> = _cards.asStateFlow()

    /** cardId → timerJob（用于取消倒计时），Mutex 保护 */
    private val timerJobs = mutableMapOf<String, kotlinx.coroutines.Job>()

    /** 正在展开（暂停倒计时）的 cardId 集合，Mutex 保护 */
    private val pausedCards = mutableSetOf<String>()

    /** 保护 timerJobs / pausedCards 的互斥锁（D1） */
    private val poolMutex = Mutex()

    // ═══ 核心操作 ═══

    /** 同类型只保留一张：先 pop 同类型旧卡 + 停它的 timer，再 push 新卡 */
    fun replace(type: SlideType, card: SlideCard) {
        popByType(type)
        push(card)
    }

    /** push 到栈顶（列表头），同时启动倒计时 */
    fun push(card: SlideCard) {
        _cards.update { listOf(card) + it }
        if (card.displayDurationMs > 0) {
            startTimer(card)
        }
        Logger.d("Agent.Hello") { "CardPool push: type=${card.type} id=${card.cardId} dur=${card.displayDurationMs}ms" }
    }

    /** 按类型 pop */
    fun popByType(type: SlideType) {
        val removed = _cards.value.filter { it.type == type }
        removed.forEach { stopTimer(it.cardId) }
        _cards.update { it.filter { c -> c.type != type } }
        if (removed.isNotEmpty()) {
            Logger.d("Agent.Hello") { "CardPool popByType: type=$type removed=${removed.size}" }
        }
    }

    /** 按 cardId pop（倒计时到期时调用） */
    internal fun pop(cardId: String) {
        stopTimer(cardId)
        _cards.update { it.filter { c -> c.cardId != cardId } }
        Logger.d("Agent.Hello") { "CardPool pop: id=$cardId" }
    }

    /** 清空所有卡（shutdown 用） */
    fun clear() {
        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
        pausedCards.clear()
        _cards.value = emptyList()
    }

    // ═══ 暂停/恢复倒计时 ═══

    /** 卡展开时暂停倒计时 */
    suspend fun pauseTimer(cardId: String) {
        poolMutex.withLock {
            if (pausedCards.add(cardId)) {
                stopTimerInternal(cardId)
                Logger.d("Agent.Hello") { "CardPool pauseTimer: id=$cardId" }
            }
        }
    }

    /** 卡收起时恢复倒计时（从头开始计时） */
    suspend fun resumeTimer(cardId: String) {
        poolMutex.withLock {
            if (pausedCards.remove(cardId)) {
                val card = _cards.value.firstOrNull { it.cardId == cardId }
                if (card != null && card.displayDurationMs > 0) {
                    startTimerInternal(card)
                    Logger.d("Agent.Hello") { "CardPool resumeTimer: id=$cardId" }
                }
            }
        }
    }

    // ═══ 内部：倒计时实现（协程 delay，跨平台） ═══

    private suspend fun startTimerInternal(card: SlideCard) {
        stopTimerInternal(card.cardId)
        val job = scope.launch {
            delay(card.displayDurationMs)
            if (!scope.isActive) return@launch
            // 再次确认没被暂停（Mutex 内检查）
            val isPaused = poolMutex.withLock { card.cardId in pausedCards }
            if (!isPaused) {
                pop(card.cardId)
            }
        }
        timerJobs[card.cardId] = job
    }

    private suspend fun stopTimerInternal(cardId: String) {
        timerJobs.remove(cardId)?.cancel()
    }

    /** 非 suspend 版本（push/pop/replace 内部用，调用方保证不会与 pause/resume 并发） */
    private fun startTimer(card: SlideCard) {
        stopTimer(card.cardId)
        val job = scope.launch {
            delay(card.displayDurationMs)
            if (!scope.isActive) return@launch
            val isPaused = poolMutex.withLock { card.cardId in pausedCards }
            if (!isPaused) {
                pop(card.cardId)
            }
        }
        timerJobs[card.cardId] = job
    }

    private fun stopTimer(cardId: String) {
        timerJobs.remove(cardId)?.cancel()
    }
}
