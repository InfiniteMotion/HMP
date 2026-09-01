package com.hearablemusic.player.ui.chat

import co.touchlab.kermit.Logger
import com.hmp.domain.agent.runtime.ConfirmGate
import com.hmp.domain.agent.runtime.ConfirmOutcome
import com.hmp.domain.agent.runtime.ConfirmRequest
import com.hmp.domain.agent.runtime.LibraryOverview
import com.hmp.domain.agent.runtime.RunContextInput
import com.hmp.domain.agent.runtime.TerminationReason
import com.hmp.domain.agent.runtime.ToolExecutionRecord
import com.hmp.domain.agent.persona.DefaultCompanionProfiles
import com.hmp.domain.agent.port.AgentMessageStore
import com.hmp.domain.agent.port.AuditLogPort
import com.hmp.domain.agent.port.LlmMessage
import com.hmp.domain.agent.port.NowPlayingContextProvider
import com.hmp.domain.agent.runtime.ContextAssembler
import com.hmp.domain.agent.runtime.MasterAgent
import com.hmp.domain.music.MusicRepository
import com.hmp.domain.setting.model.AiEndpointConfig
import com.hearablemusic.player.ui.platform.currentTimeMillis
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * 对话页 UI 面向 Agent 的接缝事件（真实网关经 MasterChatGateway → MasterAgent 产出；单测注入 Fake 网关）。
 */
sealed interface ChatAgentEvent {
    /** 引擎请求确认：携带本轮全部待确认项，UI 据此渲染确认卡并令 [ConfirmBridge.submit] 恢复引擎。 */
    data class NeedConfirm(val turnId: String, val requests: List<ConfirmRequest>) : ChatAgentEvent
    /** 单条工具执行回执（写审计成功后送达）。 */
    data class ToolExecuted(val record: ToolExecutionRecord) : ChatAgentEvent
    /** 单轮结束：最终答复文本 + 全量执行记录 + 终止原因。 */
    data class Finished(
        val text: String,
        val toolCalls: List<ToolExecutionRecord>,
        val terminatedBy: TerminationReason,
    ) : ChatAgentEvent
    /** 电台启动：携带 songlist 曲目 + 种子标签摘要（M6-T1）。 */
    data class RadioStarted(
        val songs: List<com.hmp.domain.music.MusicInfo>,
        val seedLabels: List<String>,
        val summary: String,
    ) : ChatAgentEvent
    /** 电台状态变更（续歌/停止/耗尽）。 */
    data class RadioStateChanged(
        val isPlaying: Boolean,
        val trackCount: Int,
    ) : ChatAgentEvent

    data class Failed(val message: String) : ChatAgentEvent
}

/**
 * 确认桥：引擎侧 suspend ConfirmGate 与 UI 之间的握手。
 *
 * 引擎在 [ConfirmGate.request] 中挂起等待用户勾选；本桥经 [onRequest] 向 UI 抛
 * [ChatAgentEvent.NeedConfirm]，UI 勾选后调 [submit] 用批准列表恢复引擎。
 * [CompletableDeferred] 跨协程线程安全，客服 ContinuousFlow/VM 均可安全调用。
 */
class ConfirmBridge {
    private val pending = LinkedHashMap<String, CompletableDeferred<List<ConfirmOutcome>>>()
    private var counter = 0
    /** 向 UI 请求确认的回调（由网关在 flow 生产者上下文中注入，可 emit）。 */
    var onRequest: (suspend (String, List<ConfirmRequest>) -> Unit)? = null

    /** 引擎侧：登记一批待确认项并挂起，直到 [submit] 提供同序决策列表。 */
    suspend fun awaitApprovals(requests: List<ConfirmRequest>): List<ConfirmOutcome> {
        val turnId = "confirm_${counter++}"
        val deferred = CompletableDeferred<List<ConfirmOutcome>>()
        pending[turnId] = deferred
        Logger.i("Agent.Gateway") { "confirm await: turn=$turnId items=${requests.size} waiting..." }
        onRequest?.invoke(turnId, requests)
        return deferred.await()
    }

    /**
     * UI 侧：提交决策列表，恢复对应 [turnId] 的挂起。
     * @return true 该批次仍待确认（成功投递）；false 该批次已关闭（竞态/已取消）。
     */
    fun submit(turnId: String, outcomes: List<ConfirmOutcome>): Boolean =
        pending.remove(turnId)?.let {
            Logger.i("Agent.Gateway") { "confirm submit: turn=$turnId outcomes=$outcomes" }
            it.complete(outcomes)
        } ?: false.also { Logger.w("Agent.Gateway") { "confirm submit: turn=$turnId 已关闭/取消" } }
}

/** 对话引擎接缝（M5-T1）：把一次用户输入换算为面向 UI 的事件流。 */
interface ChatAgentGateway {
    fun run(
        input: String,
        config: AiEndpointConfig,
        bridge: ConfirmBridge,
        ctx: RunContextInput = RunContextInput(),
    ): Flow<ChatAgentEvent>

    /**
     * 启动 AI 电台（M6-T1）。
     * 返回 songlist 曲目列表，ChatViewModel 据此渲染 SONGLIST 气泡。
     * 内部经 MasterAgent.startRadio() → RadioSubAgent 三轮协作管道。
     */
    suspend fun startRadio(seed: String? = null): ChatAgentEvent.RadioStarted?

    /** 停电台 */
    suspend fun stopRadio()

    /** 查询电台状态（供 UI 轮询徽标态） */
    fun queryRadioState(): Pair<Boolean, Int>
}

// ═══════════════════════════════════════════════════════════════════════
// T 阶段整合：MasterChatGateway —— 薄壳，对话能力由 MasterAgent 直接提供
// 不创建 ToolRegistry / PolicyGuard / Transport / ContextBudget 等
// —— 全从注入的 masterAgent 拿（已在 Koin 里构造好，ToolRegistry 含 5 个 enrich_*）
// ═══════════════════════════════════════════════════════════════════════

/**
 * 唯一网关：对话能力由 MasterAgent.handleUserMessage() 提供。
 *
 * Gateway 只做 UI 侧的上下文组装（首轮/跨轮记忆合并）+ ConfirmGate 桥接；
 * 全链路 Token 统一走 GlobalTokenCounter（MasterAgent 内部），
 * ToolRegistry 含 27 基础 + 5 enrich_* 工具（MasterAgent.init{} 自动注册）。
 */
class MasterChatGateway(
    private val masterAgent: MasterAgent,
    private val auditLog: com.hmp.domain.agent.port.AuditLogPort,
    private val nowPlayingProvider: com.hmp.domain.agent.port.NowPlayingContextProvider,
    private val musicRepository: MusicRepository,
    private val agentMessageStore: com.hmp.domain.agent.port.AgentMessageStore,
) : ChatAgentGateway {

    override fun run(
        input: String,
        config: AiEndpointConfig,
        bridge: ConfirmBridge,
        ctx: RunContextInput,
    ): Flow<ChatAgentEvent> = kotlinx.coroutines.flow.flow {
        bridge.onRequest = { turnId, requests -> emit(ChatAgentEvent.NeedConfirm(turnId, requests)) }

        // ═══ 前置意图检测：电台请求短路（不走 LLM，确定性触发）═══
        val radioSeed = extractRadioSeed(input)
        if (radioSeed != null) {
            Logger.i("Agent.Gateway") { "short-circuit: radio intent detected seed=\"$radioSeed\"" }
            val radioEvent = runCatching { startRadio(radioSeed) }
                .onFailure { e ->
                    Logger.e("Agent.Gateway", e) { "radio start failed" }
                    emit(ChatAgentEvent.Failed("电台启动失败：${e.message}"))
                }
                .getOrNull()
            if (radioEvent == null) {
                // runCatching.onFailure 已 emit Failed（异常场景）；此处是 startRadio 返回 emptyList
                Logger.w("Agent.Gateway") { "radio start returned null (empty playlist)" }
                emit(ChatAgentEvent.Failed("电台没有找到足够的曲目，换个描述试试？"))
            } else {
                emit(radioEvent)
                emit(ChatAgentEvent.Finished(radioEvent.summary, emptyList(), TerminationReason.ANSWERED))
            }
            return@flow
        }

        val confirmGate = ConfirmGate { requests -> bridge.awaitApprovals(requests) }
        try {
            val effectiveCtx = mergeFirstTurnContext(ctx, input)
            val result = masterAgent.handleUserMessage(input, config, effectiveCtx, confirmGate)
            result.toolCalls.forEach { emit(ChatAgentEvent.ToolExecuted(it)) }
            emit(ChatAgentEvent.Finished(result.text, result.toolCalls, result.terminatedBy))
        } catch (ce: kotlinx.coroutines.CancellationException) {
            throw ce
        } catch (e: Exception) {
            emit(ChatAgentEvent.Failed(e.message ?: "对话失败"))
        }
    }

    /**
     * 检测用户输入是否为电台请求（确定性关键词匹配，不走 LLM）。
     *
     * 触发词（命中任一即短路）：电台 / radio / DJ / 来一段 / 开个 / 放个 / 整个 / 来个 + 音乐风格词
     * 种子提取：从输入中剔除触发词后剩余文本；为空 → null（用 nowPlaying 自动提取）。
     *
     * @return seed 文本（可能为空串表示自动提取）；null 表示不是电台意图
     */
    private fun extractRadioSeed(input: String): String? {
        val lower = input.trim().lowercase()
        // 强触发词（独立出现即命中）
        val strongTriggers = listOf("电台", "radio", "dj", "打碟")
        // 弱触发词（需配合风格/情绪词）
        val weakTriggers = listOf("来一段", "来一首", "开个", "开个电台", "放个", "放一首", "整个", "来个", "放段", "来点")
        // 音乐风格/情绪词（与弱触发词配合）
        val styleHints = listOf(
            // 中文风格
            "摇滚", "爵士", "古典", "民谣", "电子", "流行", "嘻哈", "rnb", "蓝调", "金属",
            "朋克", "雷鬼", "乡村", "灵魂", "放克", "摇滚", "说唱", "edm", "house", "techno",
            "迪斯科", "后摇", "日摇", "韩流", "轻音乐", "纯音乐", "钢琴曲",
            // 中文情绪/场景
            "深夜", "夜晚", "深夜电台", "工作", "学习", "专注", "放松", "运动", "跑步",
            "开车", "通勤", "咖啡馆", "雨天", "清晨", "早晨", "下午茶", "助眠", "睡觉",
            "情歌", "伤感", "治愈", "欢快", "激情", "安静", "冥想",
            // 英文
            "rock", "jazz", "classical", "folk", "pop", "hiphop", "hip-hop", "blues",
            "metal", "punk", "reggae", "soul", "funk", "rap", "study", "focus", "chill",
            "workout", "sleep", "relax", "love songs", "instrumental",
        )

        val hasStrong = strongTriggers.any { lower.contains(it) }
        val hasWeakWithStyle = weakTriggers.any { lower.contains(it) } && styleHints.any { lower.contains(it.lowercase()) }

        if (!hasStrong && !hasWeakWithStyle) return null

        // 提取种子：剔除触发词后的剩余文本
        var seed = input.trim()
        (strongTriggers + weakTriggers).forEach { t -> seed = seed.replace(t, "", ignoreCase = true) }
        styleHints.forEach { s -> seed = seed.replace(s, "", ignoreCase = true) }
        seed = seed.trim().trim('，', '。', '？', '!', '！', '?', '、', ' ', '　')

        // 原始输入中提取风格词作为 seed 候选（比如"来一段摇滚"→ seed="摇滚"）
        val styleInInput = styleHints.firstOrNull { input.contains(it, ignoreCase = true) }
        val finalSeed = when {
            styleInInput != null -> styleInInput
            seed.isNotBlank() -> seed
            hasStrong -> ""  // 强触发词但无种子 → 自动提取
            else -> null     // 不该到这
        }
        return finalSeed
    }

    private suspend fun mergeFirstTurnContext(ctx: RunContextInput, input: String): RunContextInput {
        val first = buildFirstTurnContext()
        return ctx.copy(
            personaText = ctx.personaText ?: first.personaText,
            recognitionText = ctx.recognitionText ?: first.recognitionText,
            timeOfDayText = ctx.timeOfDayText ?: first.timeOfDayText,
            nowPlayingText = ctx.nowPlayingText ?: first.nowPlayingText,
            libraryOverviewText = ctx.libraryOverviewText ?: first.libraryOverviewText,
            history = ctx.history.ifEmpty { buildHistory(input) },
        )
    }

    private suspend fun buildHistory(input: String): List<LlmMessage> {
        val sid = agentMessageStore.currentOrNewSessionId()
        val mapped = agentMessageStore.loadSession(sid, 30).mapNotNull { m ->
            val role = when (m.role) {
                "user" -> "user"
                "agent", "assistant" -> "assistant"
                else -> null
            } ?: return@mapNotNull null
            LlmMessage(role = role, content = m.content)
        }
        val deduped = if (mapped.lastOrNull()?.role == "user" && mapped.lastOrNull()?.content == input) mapped.dropLast(1) else mapped
        Logger.i("Agent.Gateway") { "history loaded: ${deduped.size} 条（含去重 ${mapped.size - deduped.size}）" }
        return deduped
    }

    private suspend fun buildFirstTurnContext(): RunContextInput {
        val now = nowPlayingProvider.getNowPlaying()
        val nowPlayingText = ContextAssembler.buildNowPlaying(
            currentTitle = now.currentMusicInfo?.music?.title,
            currentArtist = now.currentMusicInfo?.music?.artist,
            isPlaying = now.isPlaying,
            currentPositionMs = now.currentPositionMs,
            durationMs = now.durationMs,
        )
        val total = musicRepository.getMusicCount().first()
        val known = musicRepository.getMusicWithExtraCount().first()
        val recognitionText = ContextAssembler.buildRecognitionProgress(known, total)
        val hour = ((currentTimeMillis() / 3_600_000L) % 24).toInt()
        val overviewText = ContextAssembler.buildLibraryOverview(buildLibraryOverview())
        Logger.i("Agent.Gateway") { "first-turn ctx: persona=${DefaultCompanionProfiles.DEFAULT.personaName} nowPlaying=${now.currentMusicInfo?.music?.title ?: "无"} recognized=$known/$total" }
        return RunContextInput(
            personaText = DefaultCompanionProfiles.DEFAULT.personaPrompt,
            recognitionText = recognitionText,
            timeOfDayText = ContextAssembler.buildTimeOfDay(hour),
            nowPlayingText = nowPlayingText,
            libraryOverviewText = overviewText,
        )
    }

    private suspend fun buildLibraryOverview(): LibraryOverview {
        val total = musicRepository.getMusicCount().first()
        val analytics = musicRepository.getUserUsageAnalytics()
        return LibraryOverview(
            totalCount = total,
            topGenres = analytics.topGenres.map { it.labelDisplayName to it.count },
            topPlayedSongs = analytics.topPlayedSongs.map { it.title to it.artist },
        )
    }

    // ── Radio 电台（M6-T1） ────────────────────────────────────────

    override suspend fun startRadio(seed: String?): ChatAgentEvent.RadioStarted? {
        val tracks = masterAgent.startRadio(seed)
        if (tracks.isEmpty()) {
            Logger.w("Agent.Gateway") { "startRadio: empty result" }
            return null
        }
        // 把 RadioTrack.musicId 批量查 MusicInfo
        val ids = tracks.map { it.musicId }
        val musicInfos = runCatching {
            musicRepository.getAllMusicInfoAsList("id", "asc")
                .filter { it.music.id in ids }
        }.getOrDefault(emptyList())

        // 种子标签（从 MasterAgent 或 RadioSubAgent 拿——暂用 playlist 的 why 字段提取）
        val summary = if (!seed.isNullOrBlank()) "「$seed」电台 · ${tracks.size} 首备选"
                      else "今夜电台 · ${tracks.size} 首备选"

        Logger.i("Agent.Gateway") { "startRadio: ${tracks.size} tracks → ${musicInfos.size} resolved" }
        return ChatAgentEvent.RadioStarted(
            songs = musicInfos,
            seedLabels = tracks.map { it.why },  // why 暂当 labels 用（简单）
            summary = summary,
        )
    }

    override suspend fun stopRadio() {
        masterAgent.stopRadio()
        Logger.i("Agent.Gateway") { "stopRadio: done" }
    }

    override fun queryRadioState(): Pair<Boolean, Int> {
        val state = masterAgent.queryRadioState()
        val playlist = masterAgent.queryRadioPlaylist()
        return (state == com.hmp.domain.agent.sub.RadioState.PLAYING) to playlist?.size.orZero()
    }

    private fun Int?.orZero() = this ?: 0
}
