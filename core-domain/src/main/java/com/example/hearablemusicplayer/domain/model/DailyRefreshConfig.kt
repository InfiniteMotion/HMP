package com.example.hearablemusicplayer.domain.model

/**
 * 每日推荐刷新配置数据类
 */
data class DailyRefreshConfig(
    val mode: String, // "time", "startup", "smart"
    val refreshHours: Int, // 按小时刷新的间隔
    val startupCount: Int, // 按启动次数刷新
    val lastRefreshTimestamp: Long, // 上次刷新时间戳
    val launchCountSinceRefresh: Int // 自上次刷新后的启动次数
)
