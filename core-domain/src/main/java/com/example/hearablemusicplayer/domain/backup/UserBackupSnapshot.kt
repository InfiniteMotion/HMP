package com.example.hearablemusicplayer.domain.backup

import com.example.hearablemusicplayer.domain.enum.AiProviderType
import com.example.hearablemusicplayer.domain.enum.LabelCategory
import com.example.hearablemusicplayer.domain.enum.LabelName
import com.example.hearablemusicplayer.domain.playlist.Playlist
import com.example.hearablemusicplayer.domain.playlist.PlaylistItem
import com.example.hearablemusicplayer.domain.setting.model.AiProviderConfig
import com.example.hearablemusicplayer.domain.setting.model.ListeningDuration
import com.example.hearablemusicplayer.domain.setting.model.PlaybackHistory

/**
 * 应用级用户数据备份快照
 */
data class UserBackupSnapshot(
    val version: Int = 1,
    val createdAt: Long,
    val appSettings: AppSettingsSnapshot,
    val musicUserState: MusicUserStateSnapshot,
    val playlists: PlaylistsSnapshot,
    val listeningStats: ListeningStatsSnapshot,
    val dailyRecommendation: DailyRecommendationSnapshot?
)

/**
 * 应用设置快照
 */
data class AppSettingsSnapshot(
    val userName: String?,
    val avatarUri: String?,
    val themeMode: String,
    val backgroundStyle: String,
    val autoBatchProcess: Boolean,
    val dailyRefreshMode: String,
    val dailyRefreshHours: Int,
    val dailyRefreshStartupCount: Int,
    val currentAiProvider: AiProviderType,
    val aiProviderConfigs: Map<AiProviderType, AiProviderConfig>
)

/**
 * 音乐库用户状态快照
 */
data class MusicUserStateSnapshot(
    val userInfos: List<UserInfoSnapshot>,
    val extras: List<MusicExtraUserSnapshot>,
    val labels: List<MusicLabelSnapshot>
)

data class UserInfoSnapshot(
    val id: Long,
    val liked: Boolean,
    val disLiked: Boolean,
    val lastPlayed: Long?,
    val playCount: Int?,
    val skippedCount: Int?,
    val userRating: Int?,
    val inCustomPlaylistCount: Int?
)

data class MusicExtraUserSnapshot(
    val id: Long,
    val isGetExtraInfo: Boolean,
    val rewards: String?,
    val popLyric: String?,
    val singerIntroduce: String?,
    val backgroundIntroduce: String?,
    val description: String?,
    val relevantMusic: String?
)

data class MusicLabelSnapshot(
    val musicId: Long,
    val label: LabelName,
    val category: LabelCategory
)

/**
 * 播放列表快照
 */
data class PlaylistsSnapshot(
    val playlists: List<Playlist>,
    val playlistItems: List<PlaylistItem>
)

/**
 * 听歌统计快照
 */
data class ListeningStatsSnapshot(
    val listeningDurations: List<ListeningDuration>,
    val playbackHistories: List<PlaybackHistory>
)

/**
 * 每日推荐相关快照
 */
data class DailyRecommendationSnapshot(
    val currentDailyMusicId: Long?,
    val lastRefreshTimestamp: Long,
    val mode: String,
    val refreshHours: Int,
    val startupCount: Int,
    val launchCountSinceRefresh: Int
)

