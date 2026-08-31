package com.hmp.domain.agent.runtime

import kotlinx.coroutines.sync.Mutex

/**
 * Agent 停止/暂停信号——每个 Agent 构造时选一个实现。
 *
 * 两个实现：
 * - [AlwaysRunningStopSignal] MasterAgent 用：永不暂停
 * - [SchedulerStopSignal] Enrich/Radio 用：真正挂起等 Scheduler resume
 */
interface StopSignal {
    /** 软停止：跑完当前 step 就退出循环（token 配额耗尽 / 外部 shutdown） */
    fun shouldSoftStop(): Boolean

    /** 挂起等待（Scheduler pause 时挂起，resume 时立即恢复）——用 Mutex 实现零唤醒 */
    suspend fun waitResume()
}

/** MasterAgent 用——永不暂停，只查 token/step 熔断（ReActLoop 内部也会查 stepBudget） */
class AlwaysRunningStopSignal(
    private val tokenCounter: GlobalTokenCounter? = null,
) : StopSignal {
    override fun shouldSoftStop(): Boolean = tokenCounter?.shouldStop() ?: false
    override suspend fun waitResume() {}  // Master 永不暂停，空实现
}

/**
 * EnrichSubAgent / RadioSubAgent 用——真正挂起。
 *
 * 替换旧实现里的 while(PAUSED) { delay(500) } 轮询：
 * - 用 Mutex 实现真正挂起 → 零 CPU 唤醒 / 零延迟恢复
 * - 配合 AgentScheduler 的 onResume/onPause 回调：
 *   Scheduler 把状态改成 PAUSED 时调 onPause → pauseMutex.lock() 立刻挂起 Agent
 *   Scheduler 把状态改成 RUNNING 时调 onResume → pauseMutex.unlock() 立刻唤醒 Agent
 *
 * 使用方式：
 *   val stopSignal = SchedulerStopSignal(tokenCounter)
 *   scheduler.registerAgent(AgentRegistration(
 *       agentId = "enrich",
 *       priority = ENRICH,
 *       onPause = { stopSignal.onSchedulerPaused() },
 *       onResume = { stopSignal.onSchedulerResumed() },
 *   ))
 */
class SchedulerStopSignal(
    private val tokenCounter: GlobalTokenCounter? = null,
) : StopSignal {

    /**
     * pauseMutex 是"门闩"：
     * - 初始 unlocked（Agent 可以自由运行）
     * - Scheduler pause → lock → Agent 下一个 waitResume() 调用就被挂住
     * - Scheduler resume → unlock → 挂起的 Agent 立即恢复
     */
    private val pauseMutex = Mutex(locked = false)

    override fun shouldSoftStop(): Boolean = tokenCounter?.shouldStop() ?: false

    /**
     * Agent 每次循环开始时调用。
     * 如果 Scheduler 之前发了 pause，这里会真挂起（零 CPU 唤醒）。
     * 如果没 pause（正常 RUNNING），Mutex 是 unlocked，立即返回。
     */
    override suspend fun waitResume() {
        // tryLock() 不阻塞——如果已锁住（Scheduler 发了 pause），返回 false
        // 然后我们就 lock() 真正挂起，等 Scheduler 的 onResume 回调 unlock
        if (!pauseMutex.tryLock()) {
            pauseMutex.lock()  // ← 真正挂起，直到 Scheduler 调 onSchedulerResumed()
        }
    }

    /** Scheduler 状态变 PAUSED 时调（通过 AgentRegistration.onPause 回调） */
    fun onSchedulerPaused() {
        // tryLock 非阻塞——如果当前 unlocked（Agent 还没调 waitResume），就锁住等 Agent 过来挂起
        // 如果已经锁住（Agent 已经在 waitResume 里挂起了），tryLock 返回 false，不管
        pauseMutex.tryLock()
    }

    /** Scheduler 状态变 RUNNING 时调（通过 AgentRegistration.onResume 回调） */
    fun onSchedulerResumed() {
        try { pauseMutex.unlock() } catch (_: IllegalStateException) { /* already unlocked */ }
    }
}
