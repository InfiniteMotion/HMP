package com.hmp.domain.agent.sub

import co.touchlab.kermit.Logger
import com.hmp.data.database.HelloCardCache
import com.hmp.data.database.HelloCardCacheDao
import com.hmp.data.database.HelloReportNarrativeDao
import com.hmp.data.database.HelloReportNarrativeEntity
import com.hmp.data.util.currentHour
import com.hmp.data.util.millisUntilNextLocalMidnight
import com.hmp.data.util.todayDateString
import com.hmp.domain.agent.infra.PresenceBus
import com.hmp.domain.agent.infra.PresenceEvent
import com.hmp.domain.agent.port.NowPlayingContextProvider
import com.hmp.domain.agent.port.LlmMessage
import com.hmp.domain.agent.runtime.AgentContextBudget
import com.hmp.domain.agent.runtime.AgentRunState
import com.hmp.domain.agent.runtime.StopSignal
import com.hmp.domain.agent.runtime.ToolRegistryView
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicRepository
import com.hmp.domain.enum.LabelName
import com.hmp.domain.setting.model.AiEndpointConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.hmp.data.database.currentTimeMillis
import com.hmp.platform.Volatile

/**
 * W0 HelloSubAgent：门面副驾驶（F1-F6 铁则）。
 *
 * 职责：把"音乐库 + 最近行为 + 当前时段"变成用户想看的内容卡。
 * 三个工作协程：
 * ① collectPresenceEvents —— DjBlank（每次切歌 → 更新 GREETING）
 *                          —— AgentProgress(radio) → 更新/清除 RADIO_STATUS
 * ② dailyRefreshLoop —— 每日凌晨补跑 RECOMMEND / DISCOVER / FORGOTTEN / ANNIVERSARY + 报告叙事段
 * ③ minuteTickLoop —— 每分钟检测时段变化 → 更新 RECOMMEND；刷新 ANCHOR
 *
 * 暂停/恢复：Scheduler priority=HELLO(4) 永不暂停；StopSignal 桥接 Mutex。
 * SharedFlow 丢事件的主动补偿：
 * - runLoop 启动时从 DAO 恢复上次生成的卡
 * - runLoop 启动时主动 push 初始问候卡（兜底）
 *
 * MusicRepository 依赖：所有卡型生成都通过接口方法（接口返回空列表/零值不崩）。
 * enableLlm 动态启用：chatTransport != null && enrichConfig != null → LLM 可解释理由，否则兜底文案。
 */
class HelloSubAgent(
    agentId: String = "hello",
    contextBudget: AgentContextBudget,
    toolRegistryView: ToolRegistryView,
    /** 注入的 DAO（nullable 降级为内存卡池） */
    private val cardCacheDao: HelloCardCacheDao? = null,
    private val narrativeDao: HelloReportNarrativeDao? = null,
    private val musicRepository: MusicRepository? = null,
    private val presenceBus: PresenceBus? = null,
    private val nowPlayingProvider: NowPlayingContextProvider? = null,
    private val stopSignal: StopSignal? = null,
    private val enrichConfig: AiEndpointConfig? = null,
    /** 门面问候轮换列表（MasterAgent.fallbackGreetings 注入） */
    private val fallbackGreetings: List<String> = DEFAULT_FALLBACK_GREETINGS,
    /** 是否启用 LLM 生成（H3 实现；H2 默认 false，走兜底） */
    private val enableLlm: Boolean = false,
    /** 电台查询代理（nullable；MasterAgent.queryRadioPlaylist 直接传进来，避免循环依赖） */
    private val radioPlaylistProvider: suspend () -> List<RadioTrack>? = { null },
) : SubAgent(agentId, contextBudget, toolRegistryView) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** 卡片池（StateFlow 暴露给 UI collect） */
    private val cardPool = CardPool(scope)

    /** UI collect 的 StateFlow */
    val cards: StateFlow<List<SlideCard>> get() = cardPool.cards

    /** 每日凌晨生成的推荐卡数量 */
    private val dailyRecommendCount = 1

    /** 当前时段（minuteTickLoop 维护） */
    @Volatile
    private var lastPhase: TimePhase? = null

    /** 门面问候轮换索引 */
    /** 门面问候轮换索引，@Volatile 避免 Dispatchers.Default 多线程 data race */
    @Volatile
    private var greetingIndex: Int = 0
    @Volatile
    private var agentPaused: Boolean = false

    /** Fix1: 内存补跑守卫（DAO=null 时用这个标志，避免无限循环 push/pop） */
    @Volatile
    private var todayCardsGenerated: Boolean = false

    // ═══ SubAgent.runLoop ═══

    override suspend fun runLoop() {
        Logger.i("Agent.Hello") { "runLoop start" }
        isActive = true
        runState = AgentRunState.RUNNING

        // ① 启动三个工作协程
        val presenceJob = scope.launch { collectPresenceEvents() }
        val dailyJob = scope.launch { dailyRefreshLoop() }
        val tickJob = scope.launch { minuteTickLoop() }

        // ② 从 DAO 恢复（SharedFlow 丢 DjBlank 的主动补偿）
        runCatching { initializeFromDao() }
            .onFailure { e -> Logger.w("Agent.Hello", e) { "initializeFromDao failed (non-fatal)" } }

        // ③ 立即 push 常驻卡（不等任何事件到来）
        runCatching { initializeAnchorCards() }
            .onFailure { e -> Logger.w("Agent.Hello", e) { "initializeAnchorCards failed (non-fatal)" } }

        // ④ runLoop 自身只负责暂停/恢复 + 优雅退出（runLoop 自身不检查 agentPaused，它是设置者）
        while (scope.isActive && isActive) {
            agentPaused = true          // D2: runLoop 阻塞期间子协程也停
            stopSignal?.waitResume()
            agentPaused = false         // D2: 恢复后子协程继续工作
            if (stopSignal?.shouldSoftStop() == true) break
            delay(500)
        }

        // ⑤ 清理
        presenceJob.cancel()
        dailyJob.cancel()
        tickJob.cancel()
        cardPool.clear()
        runState = AgentRunState.PAUSED
        Logger.i("Agent.Hello") { "runLoop exited" }
    }

    override suspend fun shutdown() {
        isActive = false
        cardPool.clear()
        scope.cancel()                              // D3: 取消所有子协程（timer 等）
        super.shutdown()
        Logger.i("Agent.Hello") { "shutdown complete" }
    }

    // ═══ 工作协程 #1：PresenceBus 事件收集 ═══

    private suspend fun collectPresenceEvents() {
        val bus = presenceBus ?: run {
            Logger.w("Agent.Hello") { "presenceBus null, skip collectPresenceEvents" }
            return
        }
        Logger.i("Agent.Hello") { "collectPresenceEvents started" }
        bus.events.collect { event ->
            if (agentPaused) return@collect  // D2: runLoop 暂停期间跳过事件
            when (event) {
                is PresenceEvent.DjBlank -> {
                    Logger.d("Agent.Hello") { "DjBlank → update GREETING" }
                    val greeting = generateGreeting()
                    cardPool.replace(
                        SlideType.GREETING,
                        SlideCard(SlideCard.newId(), SlideType.GREETING, greeting, GREETING_DURATION_MS)
                    )
                }
                is PresenceEvent.AgentProgress -> {
                    if (event.agentId != "radio") return@collect
                    if (event.total == 0) {
                        // Radio 停止 → pop 电台状态卡
                        Logger.d("Agent.Hello") { "Radio stopped → pop RADIO_STATUS" }
                        cardPool.popByType(SlideType.RADIO_STATUS)
                    } else {
                        // Radio 运行中 → replace 电台状态卡
                        val nextTrack = runCatching { radioPlaylistProvider() }
                            .getOrNull()?.firstOrNull()?.title
                        val status = SlideCard(
                            cardId = SlideCard.newId(),
                            type = SlideType.RADIO_STATUS,
                            content = RadioStatusContent(
                                targetCount = event.total,
                                nextTrackName = nextTrack,
                            ),
                            displayDurationMs = 0L,  // 常驻
                        )
                        cardPool.replace(SlideType.RADIO_STATUS, status)
                    }
                }
                else -> { /* 其他事件忽略 */ }
            }
        }
    }

    // ═══ 工作协程 #2：每日凌晨定时 ═══

    private suspend fun dailyRefreshLoop() {
        Logger.i("Agent.Hello") { "dailyRefreshLoop started" }
        while (scope.isActive && isActive && !agentPaused) {
            // ① 补跑守卫：DAO 存在 → 查 DAO；DAO=null → 内存标志位
            val today = todayString()
            val hasTodayCards = cardCacheDao?.let { dao ->
                val allTodayTypes = runCatching {
                    dao.getLatestCardTypesByDate(today)
                }.getOrDefault(emptySet())
                SlideType.RECOMMEND.name in allTodayTypes
                    || SlideType.DISCOVER.name in allTodayTypes
                    || SlideType.FORGOTTEN.name in allTodayTypes
                    || SlideType.ANNIVERSARY.name in allTodayTypes
            } ?: todayCardsGenerated

            if (!hasTodayCards) {
                Logger.i("Agent.Hello") { "dailyRefreshLoop: today=$today not yet generated" }
                runCatching { dailyRefreshOnce() }
                    .onFailure { e -> Logger.e("Agent.Hello", e) { "dailyRefreshOnce failed" } }
                if (cardCacheDao == null) {
                    todayCardsGenerated = true  // 内存模式标记已生成
                }
            } else {
                Logger.d("Agent.Hello") { "dailyRefreshLoop: today=$today already has cards, skip" }
            }

            // ② 报告叙事段也在此时检查（MasterAgent.regenerateReportNarrative 可手动触发）
            runCatching { ensureReportNarrativeUpToDate() }
                .onFailure { e -> Logger.w("Agent.Hello", e) { "report narrative ensure failed (non-fatal)" } }

            // ③ 睡到明天凌晨
            val delayMs = millisUntilNextMidnight()
            Logger.d("Agent.Hello") { "dailyRefreshLoop: sleep ${delayMs / 60_000}min until next midnight" }
            delay(delayMs.coerceAtLeast(60_000L))
        }
    }

    /** 每日批量生成：RECOMMEND + DISCOVER + FORGOTTEN + ANNIVERSARY + 写入 DAO */
    private suspend fun dailyRefreshOnce() {
        val repo = musicRepository
        // B3: getMusicCount 轻量判空，不拉全曲库
        if (repo == null || runCatching { repo.getMusicCount().first() }.getOrDefault(0) <= 0) {
            Logger.w("Agent.Hello") { "dailyRefreshOnce: musicRepository null or library empty, skip" }
            return
        }

        val today = todayString()
        val allCards = mutableListOf<SlideCard>()

        // ① RECOMMEND（兜底版：按时段 label 取曲目）
        val currentPhase = detectTimePhase(currentHour())
        // B4: 同类型卡只 replace 第一张，避免 forEach replace 语义导致只剩最后一张
        val recommendCard = runCatching {
            generateRecommendCards(phase = currentPhase, count = dailyRecommendCount).firstOrNull()
        }.getOrNull()
        if (recommendCard != null) {
            cardPool.replace(recommendCard.type, recommendCard)
            allCards += recommendCard
        }

        // ② DISCOVER（兜底版：取 POP 标签）
        val discover = runCatching { generateDiscoverCard() }.getOrNull()
        if (discover != null) {
            cardPool.replace(discover.type, discover)
            allCards += discover
        }

        // ③ FORGOTTEN
        val forgotten = runCatching { checkForgotten() }.getOrNull()
        if (forgotten != null) {
            cardPool.replace(SlideType.FORGOTTEN, forgotten)
            allCards += forgotten
        } else {
            cardPool.popByType(SlideType.FORGOTTEN)
        }

        // ④ ANNIVERSARY
        val anniversary = runCatching { checkAnniversary() }.getOrNull()
        if (anniversary != null) {
            cardPool.replace(SlideType.ANNIVERSARY, anniversary)
            allCards += anniversary
        } else {
            cardPool.popByType(SlideType.ANNIVERSARY)
        }

        // ⑤ 写入 DAO
        runCatching {
            allCards.forEach { card ->
                val json = cardContentToString(card.content)
                cardCacheDao?.insert(
                    HelloCardCache(
                        cardType = card.type.name,
                        cardContentJson = json,
                        generatedAt = currentTimeMillis(),
                        generatedForDate = today,
                    )
                )
            }
        }.onFailure { e -> Logger.w("Agent.Hello", e) { "DAO insert failed (non-fatal)" } }

        Logger.i("Agent.Hello") { "dailyRefreshOnce: done, ${allCards.size} cards generated" }
    }

    // ═══ 工作协程 #3：每分钟 tick ═══

    private suspend fun minuteTickLoop() {
        Logger.i("Agent.Hello") { "minuteTickLoop started" }
        while (scope.isActive && isActive && !agentPaused) {
            delay(60_000L)

            // ① 检查时段变化
            val currentPhase = detectTimePhase(currentHour())
            if (currentPhase != lastPhase) {
                Logger.i("Agent.Hello") { "minuteTickLoop: phase changed ${lastPhase} → $currentPhase" }
                lastPhase = currentPhase
                // 时段变了 → 刷新 RECOMMEND
                runCatching {
                    // B4: 同类型只 replace 第一张
                    val recommendCard = generateRecommendCards(phase = currentPhase, count = dailyRecommendCount).firstOrNull()
                    if (recommendCard != null) cardPool.replace(recommendCard.type, recommendCard)
                }.onFailure { e -> Logger.w("Agent.Hello", e) { "phase-change RECOMMEND refresh failed" } }
            }

            // ② 刷新正在听锚定卡
            runCatching { refreshAnchorCard() }
                .onFailure { e -> Logger.w("Agent.Hello", e) { "refreshAnchorCard failed" } }
        }
    }

    // ═══ 初始化：从 DAO 恢复 + 常驻卡 push ═══

    /** SharedFlow 丢 DjBlank 的主动补偿 */
    private suspend fun initializeFromDao() {
        val dao = cardCacheDao ?: return
        val restoreTypes = listOf(
            SlideType.RECOMMEND,
            SlideType.DISCOVER,
            SlideType.FORGOTTEN,
            SlideType.ANNIVERSARY,
        )
        var restored = 0
        for (type in restoreTypes) {
            val cache = dao.getLatestOfAnyDate(type.name) ?: continue
            val content = stringToCardContent(type, cache.cardContentJson)
            if (content != null) {
                val dur = durationForType(type)
                cardPool.push(SlideCard(SlideCard.newId(), type, content, dur))
                restored++
            }
        }
        if (restored > 0) Logger.i("Agent.Hello") { "initializeFromDao: restored $restored cards" }
    }

    /** runLoop 启动时立即 push 常驻卡 + 兜底 GREETING + 检查 Radio 状态 */
    private suspend fun initializeAnchorCards() {
        // ANCHOR（正在听，常驻）
        refreshAnchorCard()

        // 兜底 GREETING（不依赖 DjBlank）
        val greeting = SlideCard(
            SlideCard.newId(),
            SlideType.GREETING,
            GreetingContent(nextFallbackGreeting(), fromFallback = true, currentTrack = null),
            GREETING_DURATION_MS,
        )
        cardPool.replace(SlideType.GREETING, greeting)

        // Radio 已经在跑 → 也 push 一张 RADIO_STATUS
        runCatching {
            val playlist = radioPlaylistProvider()
            if (!playlist.isNullOrEmpty()) {
                val status = SlideCard(
                    SlideCard.newId(),
                    SlideType.RADIO_STATUS,
                    RadioStatusContent(
                        targetCount = playlist.size,
                        nextTrackName = playlist.firstOrNull()?.title,
                    ),
                    0L,
                )
                cardPool.replace(SlideType.RADIO_STATUS, status)
                Logger.i("Agent.Hello") { "initializeAnchorCards: Radio already running → push RADIO_STATUS" }
            }
        }.onFailure { e -> Logger.w("Agent.Hello", e) { "initializeAnchorCards: Radio status check failed" } }
    }

    private suspend fun refreshAnchorCard() {
        val ctx = nowPlayingProvider?.getNowPlaying()
        val music = ctx?.currentMusicInfo
        val phase = detectTimePhase(currentHour())
        val anchor = SlideCard(
            SlideCard.newId(),
            SlideType.ANCHOR,
            AnchorContent(
                trackTitle = music?.music?.title,
                artistName = music?.music?.artist,
                bpm = null,
                phase = phase,
            ),
            displayDurationMs = 0L,
        )
        cardPool.replace(SlideType.ANCHOR, anchor)
    }

    // ═══ 七种卡型生成（H2 非 LLM 版本，enableLlm=false） ═══

    /** GREETING：DjBlank 触发；H2 走 fallback */
    private fun generateGreeting(): GreetingContent {
        return GreetingContent(
            text = nextFallbackGreeting(),
            fromFallback = true,
            currentTrack = null,
        )
    }

    /** RECOMMEND：每日 + 时段切换，走 MusicRepository 接口 */
    private suspend fun generateRecommendCards(
        phase: TimePhase,
        count: Int,
    ): List<SlideCard> {
        val repo = musicRepository ?: return emptyList()
        val label = labelForPhase(phase) ?: return emptyList()

        val ids = runCatching { repo.getMusicIdListByType(label).take(count) }
            .getOrDefault(emptyList())
        if (ids.isEmpty()) return emptyList()

        val infos = runCatching { repo.getMusicInfoByIds(ids) }
            .getOrDefault(emptyList())
            .associate { it.music.id to it }

        return ids.map { id ->
            val info = infos[id]
            SlideCard(
                SlideCard.newId(),
                SlideType.RECOMMEND,
                RecommendContent(
                    trackId = id,
                    trackTitle = info?.music?.title ?: "推荐曲目",
                    reason = reasonForLabel(label),
                    currentPhase = phase,
                ),
                RECOMMEND_DURATION_MS,
            )
        }
    }

    /** DISCOVER：每日凌晨；H2 兜底——取 getGlobalTopLabels 第一个非空 label */
    private suspend fun generateDiscoverCard(): SlideCard? {
        val repo = musicRepository ?: return null

        // 先查活跃 label；空则兜底固定 POP
        val labels = runCatching { repo.getGlobalTopLabels(5) }
            .getOrDefault(emptyList()) + listOf(LabelName.POP, LabelName.ROCK, LabelName.CALM)

        for (label in labels.distinct()) {
            val ids = runCatching { repo.getMusicIdListByType(label).take(3) }
                .getOrDefault(emptyList())
            if (ids.isNotEmpty()) {
                return SlideCard(
                    SlideCard.newId(),
                    SlideType.DISCOVER,
                    DiscoverContent(
                        target = label.name,
                        reason = reasonForDiscover(label),
                        trackIds = ids,
                    ),
                    DISCOVER_DURATION_MS,
                )
            }
        }
        return null
    }

    /** FORGOTTEN：每日凌晨扫历史——优先 getForgottenTracks(30)，没有则 90 天 */
    private suspend fun checkForgotten(): SlideCard? {
        val repo = musicRepository ?: return null
        // B1: 拿到准确的 days 参数（30 或 90 fallback）
        val (id, days) = runCatching {
            repo.getForgottenTracks(days = 30).firstOrNull()?.let { Pair(it, 30) }
                ?: repo.getForgottenTracks(days = 90).firstOrNull()?.let { Pair(it, 90) }
        }.getOrNull() ?: return null

        val info = runCatching { repo.getMusicInfoByIds(listOf(id)).firstOrNull() }.getOrNull()
        return SlideCard(
            SlideCard.newId(),
            SlideType.FORGOTTEN,
            ForgottenContent(
                trackId = id,
                trackTitle = info?.music?.title ?: "遗忘曲目",
                daysSince = days,   // B1: 准确天数，fallback 到 90
                playCount = 0,
            ),
            FORGOTTEN_DURATION_MS,
        )
    }

    /** ANNIVERSARY：每日凌晨——getAnniversaryTracks(todayString()) 正式调用；纯数据拼接，不需要 LLM */
    private suspend fun checkAnniversary(): SlideCard? {
        val repo = musicRepository ?: return null
        val today = todayString()
        // B2: 解构 (musicId, firstPlayedAtMs) 算实际 yearsAgo
        val (id, firstPlayedAt) = runCatching {
            repo.getAnniversaryTracks(today).firstOrNull()
        }.getOrNull() ?: return null

        val info = runCatching { repo.getMusicInfoByIds(listOf(id)).firstOrNull() }.getOrNull()
        val yearsAgo = ((currentTimeMillis() - firstPlayedAt) / (365.25 * 86_400_000L)).toInt().coerceAtLeast(1)

        return SlideCard(
            SlideCard.newId(),
            SlideType.ANNIVERSARY,
            AnniversaryContent(
                trackId = id,
                trackTitle = info?.music?.title ?: "纪念日曲目",
                yearsAgo = yearsAgo,   // B2: 实际年数，从首次播放时间算
                totalPlays = runCatching { info?.userInfo?.playCount ?: 0 }.getOrDefault(0),
            ),
            ANNIVERSARY_DURATION_MS,
        )
    }

    // ═══ 报告叙事段（H4） ═══

    /** 自适应报告叙事生成——根据日均听歌时长判断频率 */
    suspend fun regenerateReportNarrative(timeRange: NarrativeTimeRange): HelloReportNarrativeEntity? {
        val repo = musicRepository ?: return null
        val dao = narrativeDao ?: return null

        val avgMinutes = runCatching { repo.getAvgDailyListeningMinutes(30) }.getOrDefault(0f)
        val frequency = adaptiveFrequency(avgMinutes)
        val range = timeRange.name

        // 频率守卫：DAO 有未过期缓存就直接返回，不重复生成
        val existing = runCatching { dao.getLatest(range) }.getOrNull()
        if (existing != null && !isNarrativeExpired(existing, frequency)) {
            Logger.d("Agent.Hello") { "report[$range]: fresh cache (avgDaily=$avgMinutes, freq=$frequency), skip" }
            return existing
        }

        // H2 骨架：生成极简统计叙事（后续 H4 补 LLM + 完整统计维度）
        val narrative = buildStatisticsNarrative(timeRange, avgMinutes)
        val entity = HelloReportNarrativeEntity(
            timeRange = range,
            narrative = narrative,
            generatedAt = currentTimeMillis(),
            avgDailyMinutes = avgMinutes,
        )
        runCatching { dao.insert(entity) }.onFailure { e ->
            Logger.w("Agent.Hello", e) { "report[$range] DAO insert failed (non-fatal)" }
        }
        Logger.i("Agent.Hello") { "report[$range] regenerated (avgDaily=${avgMinutes}min, freq=$frequency)" }
        return entity
    }

    /** 确保所有时间维度的报告叙事段都是最新的（dailyRefreshLoop 末尾调） */
    private suspend fun ensureReportNarrativeUpToDate() {
        val ranges = listOf(
            NarrativeTimeRange.ALL,
            NarrativeTimeRange.DAY,
            NarrativeTimeRange.WEEK,
            NarrativeTimeRange.MONTH,
            NarrativeTimeRange.YEAR,
        )
        for (range in ranges) {
            runCatching { regenerateReportNarrative(range) }
                .onFailure { e -> Logger.w("Agent.Hello", e) { "report[$range] ensure failed (non-fatal)" } }
        }
    }

    /** 自适应频率：日均时长 → 更新频率（小时数） */
    private fun adaptiveFrequency(avgDailyMinutes: Float): Long = when {
        avgDailyMinutes <= 30f -> 24 * 7   // ≤30min → 周更新
        avgDailyMinutes <= 120f -> 24      // 30~120min → 日更新
        else -> 24                          // ≥120min → 日更新（高活跃也日更）
    }

    private fun isNarrativeExpired(entity: HelloReportNarrativeEntity, frequencyHours: Long): Boolean {
        val ageMs = currentTimeMillis() - entity.generatedAt
        return ageMs > frequencyHours * 3_600_000L
    }

    /** H2 骨架极简统计叙事——后续 H4 补 LLM 生成 */
    private fun buildStatisticsNarrative(timeRange: NarrativeTimeRange, avgMinutes: Float): String {
        val activeDesc = when {
            avgMinutes <= 15f -> "最近听的不多"
            avgMinutes <= 30f -> "每天都有在听"
            avgMinutes <= 120f -> "音乐陪伴还不错"
            else -> "音乐成了你生活的背景"
        }
        return "（$activeDesc）日均听歌 ${avgMinutes.toInt()} 分钟。"
    }

    // ═══ 对外查询接口（MasterAgent / P5 报告页） ═══

    /** 取指定时间维度最新的报告叙事段（DAO 读，零阻塞） */
    suspend fun getReportNarrative(timeRange: NarrativeTimeRange): HelloReportNarrativeEntity? {
        val dao = narrativeDao ?: return null
        return runCatching { dao.getLatest(timeRange.name) }.getOrNull()
    }

    // ═══ 工具方法 ═══

    private fun labelForPhase(phase: TimePhase): LabelName? = when (phase) {
        TimePhase.NIGHT -> LabelName.SLEEP
        TimePhase.MORNING_COMMUTE -> LabelName.MORNING
        TimePhase.WORK -> LabelName.STUDY
        TimePhase.LUNCH -> LabelName.RELAX
        TimePhase.EVENING_COMMUTE -> LabelName.DRIVING
        TimePhase.EVENING_LEISURE -> LabelName.RELAX
        TimePhase.UNKNOWN -> null
    }

    suspend fun reasonForLabel(label: LabelName): String {
        if (enableLlm) {
            val llmReason = runCatching {
                val cfg = enrichConfig ?: return@runCatching null
                val prompt = "用一句话（≤15字）解释为什么时段 ${label.name} 适合推荐音乐。直接返回中文句子，不要加引号或前缀。"
                contextBudget.callLlmText(
                    config = cfg,
                    systemPrompt = "你是一个音乐推荐助手，负责用简洁中文解释推荐理由。",
                    newMessages = listOf(LlmMessage(role = "user", content = prompt)),
                    temperature = 0.4f,
                )?.trim()?.let { text ->
                    // LLM 可能返回带引号的，去掉（ASCII + 中文引号）
                    text.trim('\u0022', '\u300C', '\u300D', '\u201C', '\u201D')
                }
            }.getOrNull()
            if (!llmReason.isNullOrBlank()) return llmReason
        }
        // 兜底（LLM 不可用 / 调用失败）
        return when (label) {
            LabelName.SLEEP -> "深夜听这首很合适"
            LabelName.MORNING -> "早晨提神来一首"
            LabelName.STUDY -> "工作学习专注之选"
            LabelName.RELAX -> "放松一下吧"
            LabelName.DRIVING -> "开车路上的陪伴"
            else -> "这个时段听听这首不错"
        }
    }

    /** DISCOVER 发现理由——独立方法（Fix3: 为 H3 LLM 升级留入口） */
    private fun reasonForDiscover(label: LabelName): String = when (label) {
        LabelName.POP -> "你好像很久没听 POP 了，来回顾一下"
        LabelName.ROCK -> "摇滚的能量，今天来点不一样的"
        LabelName.CALM -> "静静心，来几首轻的"
        LabelName.SLEEP -> "睡前放松一下"
        LabelName.MORNING -> "早晨也可以来点不一样的"
        else -> "你好像很久没听 $label 了"
    }

    private fun nextFallbackGreeting(): String {
        val list = fallbackGreetings
        if (list.isEmpty()) return DEFAULT_FALLBACK_GREETING_SINGLE
        val g = list[greetingIndex % list.size]
        greetingIndex++
        return g
    }

    private fun durationForType(type: SlideType): Long = when (type) {
        SlideType.GREETING -> GREETING_DURATION_MS
        SlideType.RECOMMEND -> RECOMMEND_DURATION_MS
        SlideType.DISCOVER -> DISCOVER_DURATION_MS
        SlideType.FORGOTTEN -> FORGOTTEN_DURATION_MS
        SlideType.ANNIVERSARY -> ANNIVERSARY_DURATION_MS
        else -> 0L  // 常驻
    }

    private fun todayString(): String = todayDateString()

    private fun millisUntilNextMidnight(): Long = millisUntilNextLocalMidnight()

    // ═══ SlideContent JSON 序列化/反序列化 ═══

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private fun cardContentToString(content: SlideContent): String {
        return runCatching { json.encodeToString(SlideContent.serializer(), content) }
            .getOrElse { "" }
    }

    private fun stringToCardContent(type: SlideType, jsonStr: String): SlideContent? {
        if (jsonStr.isBlank()) return null
        return runCatching {
            when (type) {
                SlideType.ANCHOR -> json.decodeFromString(AnchorContent.serializer(), jsonStr)
                SlideType.RADIO_STATUS -> json.decodeFromString(RadioStatusContent.serializer(), jsonStr)
                SlideType.GREETING -> json.decodeFromString(GreetingContent.serializer(), jsonStr)
                SlideType.RECOMMEND -> json.decodeFromString(RecommendContent.serializer(), jsonStr)
                SlideType.DISCOVER -> json.decodeFromString(DiscoverContent.serializer(), jsonStr)
                SlideType.FORGOTTEN -> json.decodeFromString(ForgottenContent.serializer(), jsonStr)
                SlideType.ANNIVERSARY -> json.decodeFromString(AnniversaryContent.serializer(), jsonStr)
            }
        }.getOrNull()
    }

    companion object {
        private const val GREETING_DURATION_MS = 10_000L
        private const val RECOMMEND_DURATION_MS = 15_000L
        private const val DISCOVER_DURATION_MS = 12_000L
        private const val FORGOTTEN_DURATION_MS = 12_000L
        private const val ANNIVERSARY_DURATION_MS = 15_000L
        private const val DEFAULT_FALLBACK_GREETING_SINGLE = "嗨，继续听歌？"

        /** 默认兜底问候列表 */
        private val DEFAULT_FALLBACK_GREETINGS = listOf(
            "嗨，继续听歌？",
            "下一首也不错哦～",
            "继续享受音乐吧！",
            "听听这首怎么样？",
            "为你选了一首好歌",
            "这首特别适合此刻",
        )
    }
}

