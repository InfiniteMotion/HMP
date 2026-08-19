package com.hearablemusic.player.ui.common.util

/**
 * 时间工具的多平台抽象（2b 步随 MusicListConfig 迁入）。
 *
 * 原实现基于 java.util.Calendar（JVM-only）；commonMain 组件改走本 expect/actual，
 * Android actual 用 Calendar，Desktop/iOS（第 5 步）actual 用各自时钟 API。
 * 仅覆盖 MusicList 索引锚点所需的「时间戳 → (年, 月)」与「当前时间」两个能力。
 */

/** 毫秒时间戳 → (年, 月[1..12])。 */
expect fun epochMillisToYearMonth(epochMillis: Long): Pair<Int, Int>

/** 当前时间戳（毫秒）。 */
expect fun nowEpochMillis(): Long

/** 毫秒时间戳 → 按给定 pattern 格式化（默认时区/语言环境，语义同 java.text.SimpleDateFormat；阶段一随 SongDetailScreen 迁入）。 */
expect fun formatEpochMillis(epochMillis: Long, pattern: String): String