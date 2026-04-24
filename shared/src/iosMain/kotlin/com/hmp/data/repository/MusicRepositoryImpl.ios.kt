package com.hmp.data.repository

import com.hmp.data.database.ListeningDurationDao
import com.hmp.data.database.MusicAllDao
import com.hmp.data.database.MusicDao
import com.hmp.data.database.MusicExtraDao
import com.hmp.data.database.MusicLabelDao
import com.hmp.data.database.PlaybackHistoryDao
import com.hmp.data.database.PlaylistDao
import com.hmp.data.database.PlaylistItemDao
import com.hmp.data.database.UserInfoDao
import com.hmp.data.network.MultiProviderApiAdapter
import com.hmp.data.util.DeviceMusicScanner
import com.hmp.domain.backup.ListeningStatsSnapshot
import com.hmp.domain.backup.MusicExtraUserSnapshot
import com.hmp.domain.backup.MusicLabelSnapshot
import com.hmp.domain.backup.MusicUserStateSnapshot
import com.hmp.domain.backup.UserInfoSnapshot
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicLabel
import com.hmp.domain.music.MusicRepository
import com.hmp.domain.setting.model.AiProviderConfig
import com.hmp.domain.setting.model.DailyMusicInfo
import com.hmp.domain.setting.model.ListeningDuration
import com.hmp.domain.setting.model.PlaybackHistory
import com.hmp.domain.setting.model.UserUsageAnalytics
import com.hmp.domain.enum.LabelCategory
import com.hmp.domain.enum.LabelName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class MusicRepositoryImpl(
    private val musicDao: MusicDao,
    private val musicExtraDao: MusicExtraDao,
    private val userInfoDao: UserInfoDao,
    private val musicAllDao: MusicAllDao,
    private val musicLabelDao: MusicLabelDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val listeningDurationDao: ListeningDurationDao,
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao,
    private val multiProviderApiAdapter: MultiProviderApiAdapter
) : MusicRepository {

    // MARK: - Music Query
    override suspend fun getAllMusicInfoAsList(orderBy: String, orderType: String): List<MusicInfo> = emptyList()

    override fun getMusicCount(): Flow<Int> = flowOf(0)

    override fun getMusicWithExtraCount(): Flow<Int> = flowOf(0)

    override fun getMusicWithMissingExtraCount(): Flow<Int> = flowOf(0)

    override fun getMusicInfoById(musicId: Long): Flow<MusicInfo?> = flowOf(null)

    override suspend fun getMusicListByArtist(artistName: String): List<MusicInfo> = emptyList()

    override suspend fun getMusicListByAlbum(albumName: String): List<MusicInfo> = emptyList()

    override suspend fun searchMusic(query: String): List<MusicInfo> = emptyList()

    // MARK: - Music Random
    override suspend fun getRandomMusicInfoWithMissingExtra(): MusicInfo? = null

    override suspend fun getRandomMusicInfoWithExtra(): MusicInfo? = null

    // MARK: - Music Action (Like/Dislike)
    override suspend fun updateLikedStatus(id: Long, liked: Boolean) {}

    override suspend fun getLikedStatus(id: Long): Boolean = false

    // MARK: - Soft Delete / Restore
    override suspend fun removeFromLibrary(ids: List<Long>) {}

    override suspend fun restoreToLibrary(ids: List<Long>) {}

    override suspend fun getDeletedMusicIdsGroupedByFolder(): List<Pair<String, List<Long>>> = emptyList()

    // MARK: - Labels
    override suspend fun addMusicLabel(label: MusicLabel) {}

    override fun getLabelNamesByType(type: LabelCategory): Flow<List<LabelName>> = flowOf(emptyList())

    override suspend fun getMusicIdListByType(label: LabelName): List<Long> = emptyList()

    override suspend fun getMusicLabels(musicId: Long): List<MusicLabel> = emptyList()

    // MARK: - Similarity
    override suspend fun getSimilarSongsByWeightedLabels(musicId: Long, limit: Int): List<MusicInfo> = emptyList()

    // MARK: - Listening Duration
    override fun getRecentListeningDurations(limit: Int): Flow<List<ListeningDuration>> = flowOf(emptyList())

    // MARK: - Extra Info / AI
    override suspend fun getMusicLyrics(musicId: Long): String? = null

    override suspend fun insertMusicExtra(musicId: Long, musicExtraInfo: DailyMusicInfo) {}

    override suspend fun getMusicExtraById(musicId: Long): DailyMusicInfo = DailyMusicInfo(
        genre = emptyList(), mood = emptyList(), scenario = emptyList(),
        language = "", era = "", rewards = "", lyric = "",
        singerIntroduce = "", backgroundIntroduce = "", description = "",
        relevantMusic = "", errorInfo = ""
    )

    // MARK: - Device Scan
    private val _isScanning = MutableStateFlow(false)

    override suspend fun loadMusicFromDevice(): Result<Unit> {
        _isScanning.value = true
        val result = runCatching {
            val scanned = DeviceMusicScanner.scanMusic()
            // TODO: Save scanned files to database via DAOs
        }
        _isScanning.value = false
        return result
    }

    override val isScanning: Flow<Boolean> = _isScanning

    override suspend fun syncMusicFromDeviceIncremental(): Result<Unit> {
        return loadMusicFromDevice()
    }

    // MARK: - AI / Extra Fetching
    override suspend fun fetchMusicExtraInfoWithProvider(
        providerConfig: AiProviderConfig,
        title: String,
        artist: String
    ): Result<DailyMusicInfo> {
        return Result.failure(NotImplementedError("AI fetching not implemented on iOS yet"))
    }

    override suspend fun validateProviderApiKey(providerConfig: AiProviderConfig): Result<Boolean> {
        return Result.failure(NotImplementedError("API key validation not implemented on iOS yet"))
    }

    // MARK: - Listening Duration
    override suspend fun insertPlayback(history: PlaybackHistory): Long = 0L

    override suspend fun updatePlaybackRecord(id: Long, duration: Long, isCompleted: Boolean) {}

    override suspend fun recordListeningDuration(duration: Long) {}

    override fun getPlaybackHistory(musicId: Long, limit: Int): Flow<List<PlaybackHistory>> = flowOf(emptyList())

    override suspend fun getRecentPlaybackHistoryGlobal(limit: Int): List<PlaybackHistory> = emptyList()

    // MARK: - User Usage Analytics
    override suspend fun getUserUsageAnalytics(): UserUsageAnalytics = UserUsageAnalytics(
        totalPlayCount = 0, totalSkipCount = 0, likedCount = 0, totalListeningMinutes = 0L,
        averageSessionMinutes = 0.0, completionRate = 0f, skipRate = 0f,
        thisWeekMinutes = 0L, lastWeekMinutes = 0L,
        topPlayedSongs = emptyList(), recentPlaybackWithTitle = emptyList()
    )

    // MARK: - User Stats
    override suspend fun incrementPlayCount(musicId: Long) {}

    override suspend fun incrementSkippedCount(musicId: Long) {}

    override suspend fun updateLastPlayed(musicId: Long, timestamp: Long) {}

    // MARK: - Snapshot Export/Import
    override suspend fun exportMusicUserStateSnapshot(): MusicUserStateSnapshot = MusicUserStateSnapshot(emptyList(), emptyList(), emptyList())

    override suspend fun restoreMusicUserState(snapshot: MusicUserStateSnapshot) {}

    override suspend fun exportListeningStatsSnapshot(): ListeningStatsSnapshot = ListeningStatsSnapshot(emptyList(), emptyList())

    override suspend fun restoreListeningStats(snapshot: ListeningStatsSnapshot) {}
}
