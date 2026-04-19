package com.example.hearablemusicplayer.domain.setting.model

/**
 * 用户使用数据分析结果：聚合指标、排行、近期播放等，供「用户使用数据」页展示。
 */
data class UserUsageAnalytics(
    val totalPlayCount: Int,
    val totalSkipCount: Int,
    val likedCount: Int,
    val totalListeningMinutes: Long,
    val averageSessionMinutes: Double,
    val completionRate: Float,
    val skipRate: Float,
    val thisWeekMinutes: Long,
    val lastWeekMinutes: Long,
    val topPlayedSongs: List<TopPlayedEntry>,
    val recentPlaybackWithTitle: List<RecentPlaybackEntry>,
    val playSourceBreakdown: Map<String, Int> = emptyMap(),
    val topGenres: List<LabelCountEntry> = emptyList(),
    val topMoods: List<LabelCountEntry> = emptyList(),
    val topScenarios: List<LabelCountEntry> = emptyList(),
    val topArtists: List<ArtistCountEntry> = emptyList(),
    val customPlaylistCount: Int = 0,
    val topSongsInPlaylists: List<TopPlayedEntry> = emptyList(),
)

data class TopPlayedEntry(
    val musicId: Long,
    val title: String,
    val artist: String,
    val playCount: Int,
)

data class RecentPlaybackEntry(
    val musicId: Long,
    val title: String,
    val artist: String,
    val playedAt: Long,
    val playDuration: Long,
    val isCompleted: Boolean,
    val source: String?,
)

data class LabelCountEntry(
    val labelDisplayName: String,
    val count: Int,
)

data class ArtistCountEntry(
    val artistName: String,
    val playCount: Int,
)
