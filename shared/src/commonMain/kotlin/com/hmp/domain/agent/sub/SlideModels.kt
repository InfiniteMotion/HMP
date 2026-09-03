package com.hmp.domain.agent.sub

import kotlinx.serialization.Serializable

// ═══════════════════════════════════════════════════════════════════
// W0 HelloSubAgent 卡片模型
// 放在 agent/sub/ 同级，与 RadioSubAgent / EnrichSubAgent 同级
// ═══════════════════════════════════════════════════════════════════

/** 一天中的时段（minuteTickLoop 每分钟检测，用于 ANCHOR 卡 + RECOMMEND 时段匹配） */
@Serializable
enum class TimePhase {
    NIGHT,           // 0-6  深夜
    MORNING_COMMUTE, // 7-9  早高峰
    WORK,            // 9-12 / 14-18 工作
    LUNCH,           // 12-14 午休
    EVENING_COMMUTE, // 18-20 晚高峰
    EVENING_LEISURE, // 20-23 晚间休闲
    UNKNOWN,
}

/** 根据小时数推算时段 */
fun detectTimePhase(hour: Int): TimePhase = when (hour) {
    in 0..6 -> TimePhase.NIGHT
    in 7..9 -> TimePhase.MORNING_COMMUTE
    in 9..11 -> TimePhase.WORK
    in 12..13 -> TimePhase.LUNCH
    in 14..17 -> TimePhase.WORK
    in 18..19 -> TimePhase.EVENING_COMMUTE
    in 20..23 -> TimePhase.EVENING_LEISURE
    else -> TimePhase.UNKNOWN
}

/** 报告叙事段的时间维度 */
@Serializable
enum class NarrativeTimeRange { ALL, DAY, WEEK, MONTH, YEAR }

// ═══════════════════════════════════════════════════════════════════
// SlideType + SlideContent 密封接口 + SlideCard
// ═══════════════════════════════════════════════════════════════════

/** 卡的类型枚举 */
@Serializable
enum class SlideType {
    ANCHOR,         // 正在听（常驻）
    RADIO_STATUS,   // 电台运行态（0=常驻，Radio 停止时 pop）
    GREETING,       // 问候 + DJ 衔接语（10s）
    RECOMMEND,      // 可解释推荐（15s）
    DISCOVER,       // 歌手/风格探索（12s）
    FORGOTTEN,      // 遗忘唤醒（12s）
    ANNIVERSARY,    // 纪念日（15s）
}

/** 每种卡的内容 sealed interface——可序列化，存 DAO */
@Serializable
sealed interface SlideContent

@Serializable
data class AnchorContent(
    val trackTitle: String?,
    val artistName: String?,
    val bpm: Int?,
    val phase: TimePhase?,
) : SlideContent

@Serializable
data class RadioStatusContent(
    val targetCount: Int,
    val nextTrackName: String?,
) : SlideContent

@Serializable
data class GreetingContent(
    val text: String,
    val fromFallback: Boolean,
    val currentTrack: String?,
) : SlideContent

@Serializable
data class RecommendContent(
    val trackId: Long,
    val trackTitle: String,
    val reason: String,
    val currentPhase: TimePhase,
) : SlideContent

@Serializable
data class DiscoverContent(
    val target: String,
    val reason: String,
    val trackIds: List<Long>,
) : SlideContent

@Serializable
data class ForgottenContent(
    val trackId: Long,
    val trackTitle: String,
    val daysSince: Int,
    val playCount: Int,
) : SlideContent

@Serializable
data class AnniversaryContent(
    val trackId: Long,
    val trackTitle: String,
    val yearsAgo: Int,
    val totalPlays: Int,
) : SlideContent

/** SlideCard 主数据类 */
@Serializable
data class SlideCard(
    val cardId: String,
    val type: SlideType,
    val content: SlideContent,
    val displayDurationMs: Long,   // 0 = 常驻
) {
    companion object {
        /** KMP commonMain 没有 java.util.UUID，用计数器 + 时间戳 + 随机数生成唯一 ID */
        private var seqCounter = 0L

        fun newId(): String {
            val seq = synchronized(this) { ++seqCounter }
            val ts = com.hmp.data.database.currentTimeMillis()
            val rnd = kotlin.random.Random.nextInt(1_000_000)
            return "card_${ts}_${seq}_$rnd"
        }
    }
}
