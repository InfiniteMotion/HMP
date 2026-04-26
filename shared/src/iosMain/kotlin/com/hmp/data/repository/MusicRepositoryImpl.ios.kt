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
import com.hmp.domain.music.MusicRepository
import com.hmp.domain.setting.model.AiProviderConfig
import com.hmp.domain.setting.model.DailyMusicInfo
import com.hmp.domain.setting.model.ListeningDuration
import com.hmp.domain.setting.model.PlaybackHistory
import com.hmp.domain.setting.model.UserUsageAnalytics
import com.hmp.domain.backup.MusicUserStateSnapshot
import com.hmp.domain.backup.ListeningStatsSnapshot
import com.hmp.domain.enum.LabelCategory
import com.hmp.domain.enum.LabelName
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicLabel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

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

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: Flow<Boolean> = _isScanning

    override suspend fun getAllMusicInfoAsList(orderBy: String, orderType: String): List<MusicInfo> = emptyList()
    override fun getMusicCount(): Flow<Int> = MutableStateFlow(0)
    override fun getMusicWithExtraCount(): Flow<Int> = MutableStateFlow(0)
    override fun getMusicWithMissingExtraCount(): Flow<Int> = MutableStateFlow(0)
    override fun getMusicInfoById(musicId: Long): Flow<MusicInfo?> = MutableStateFlow(null)
    override suspend fun getMusicListByArtist(artistName: String): List<MusicInfo> = emptyList()
    override suspend fun getMusicListByAlbum(albumName: String): List<MusicInfo> = emptyList()
    override suspend fun searchMusic(query: String): List<MusicInfo> = emptyList()
    override suspend fun getRandomMusicInfoWithMissingExtra(): MusicInfo? = null
    override suspend fun getRandomMusicInfoWithExtra(): MusicInfo? = null
    override suspend fun updateLikedStatus(id: Long, liked: Boolean) {}
    override suspend fun getLikedStatus(id: Long): Boolean = false
    override suspend fun removeFromLibrary(ids: List<Long>) {}
    override suspend fun restoreToLibrary(ids: List<Long>) {}
    override suspend fun getDeletedMusicIdsGroupedByFolder(): List<Pair<String, List<Long>>> = emptyList()
    override suspend fun addMusicLabel(label: MusicLabel) {}
    override fun getLabelNamesByType(type: LabelCategory): Flow<List<LabelName>> = MutableStateFlow(emptyList())
    override suspend fun getMusicIdListByType(label: LabelName): List<Long> = emptyList()
    override suspend fun getMusicLabels(musicId: Long): List<MusicLabel> = emptyList()
    override suspend fun getSimilarSongsByWeightedLabels(musicId: Long, limit: Int): List<MusicInfo> = emptyList()
    override fun getRecentListeningDurations(limit: Int): Flow<List<ListeningDuration>> = MutableStateFlow(emptyList())
    override suspend fun getMusicLyrics(musicId: Long): String? = null
    override suspend fun insertMusicExtra(musicId: Long, musicExtraInfo: DailyMusicInfo) {}
    override suspend fun getMusicExtraById(musicId: Long): DailyMusicInfo = DailyMusicInfo(
        genre = emptyList(), mood = emptyList(), scenario = emptyList(),
        language = "", era = "", rewards = "", lyric = "",
        singerIntroduce = "", backgroundIntroduce = "", description = "",
        relevantMusic = "", errorInfo = ""
    )
    override suspend fun loadMusicFromDevice(): Result<Unit> {
        _isScanning.value = true
        return try {
            val scanned = DeviceMusicScanner.scanMusic()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isScanning.value = false
        }
    }
    override suspend fun syncMusicFromDeviceIncremental(): Result<Unit> = Result.success(Unit)
    override suspend fun fetchMusicExtraInfoWithProvider(providerConfig: AiProviderConfig, title: String, artist: String): Result<DailyMusicInfo> = Result.failure(NotImplementedError())
    override suspend fun validateProviderApiKey(providerConfig: AiProviderConfig): Result<Boolean> = Result.failure(NotImplementedError())
    override suspend fun insertPlayback(history: PlaybackHistory): Long = 0L
    override suspend fun updatePlaybackRecord(id: Long, duration: Long, isCompleted: Boolean) {}
    override suspend fun recordListeningDuration(duration: Long) {}
    override fun getPlaybackHistory(musicId: Long, limit: Int): Flow<List<PlaybackHistory>> = MutableStateFlow(emptyList())
    override suspend fun getRecentPlaybackHistoryGlobal(limit: Int): List<PlaybackHistory> = emptyList()
    override suspend fun getUserUsageAnalytics(): UserUsageAnalytics = UserUsageAnalytics(
        totalPlayCount = 0, totalSkipCount = 0, likedCount = 0, totalListeningMinutes = 0L,
        averageSessionMinutes = 0.0, completionRate = 0f, skipRate = 0f,
        thisWeekMinutes = 0L, lastWeekMinutes = 0L,
        topPlayedSongs = emptyList(), recentPlaybackWithTitle = emptyList()
    )
    override suspend fun incrementPlayCount(musicId: Long) {}
    override suspend fun incrementSkippedCount(musicId: Long) {}
    override suspend fun updateLastPlayed(musicId: Long, timestamp: Long) {}
    override suspend fun exportMusicUserStateSnapshot(): MusicUserStateSnapshot = MusicUserStateSnapshot()
    override suspend fun restoreMusicUserState(snapshot: MusicUserStateSnapshot) {}
    override suspend fun exportListeningStatsSnapshot(): ListeningStatsSnapshot = ListeningStatsSnapshot()
    override suspend fun restoreListeningStats(snapshot: ListeningStatsSnapshot) {}
}
