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
import com.hmp.data.network.AiApiResult
import com.hmp.data.network.MultiProviderApiAdapter
import com.hmp.domain.backup.ListeningStatsSnapshot
import com.hmp.domain.backup.MusicExtraUserSnapshot
import com.hmp.domain.backup.MusicLabelSnapshot
import com.hmp.domain.backup.MusicUserStateSnapshot
import com.hmp.domain.backup.UserInfoSnapshot
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicRepository
import com.hmp.domain.setting.model.AiProviderConfig
import com.hmp.domain.setting.model.ArtistCountEntry
import com.hmp.domain.setting.model.DailyMusicInfo
import com.hmp.domain.setting.model.LabelCountEntry
import com.hmp.domain.setting.model.RecentPlaybackEntry
import com.hmp.domain.setting.model.TopPlayedEntry
import com.hmp.domain.setting.model.UserUsageAnalytics
import com.hmp.domain.setting.model.PlaybackHistory as PlaybackHistoryDomain
import com.hmp.domain.music.MusicLabel as MusicLabelDomain
import com.hmp.domain.setting.model.ListeningDuration as ListeningDurationDomain
import com.hmp.domain.enum.LabelCategory
import com.hmp.domain.enum.LabelName
import kotlinx.coroutines.flow.Flow
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

    override fun getAllMusic(): Flow<List<MusicInfo>> = flowOf(emptyList())

    override suspend fun searchMusic(query: String): List<MusicInfo> = emptyList()

    override suspend fun loadMusicFromDevice() {}

    override suspend fun syncMusicFromDeviceIncremental() {}

    override suspend fun removeFromLibrary(musicId: Long) {}

    override suspend fun restoreToLibrary(musicId: Long) {}

    override suspend fun getDeletedMusicIdsGroupedByFolder(): Map<String, List<Long>> = emptyMap()

    override suspend fun addLabel(musicId: Long, category: LabelCategory, labelName: LabelName) {}

    override suspend fun removeLabel(musicId: Long, category: LabelCategory, labelName: LabelName) {}

    override suspend fun getLabels(): List<MusicLabelDomain> = emptyList()

    override suspend fun getDailyMusicRecommendation(): List<DailyMusicInfo> = emptyList()

    override suspend fun saveDailyMusicRecommendation(musicInfos: List<DailyMusicInfo>) {}

    override suspend fun updateDailyMusicInfoDate(date: String) {}

    override suspend fun getLastDailyMusicInfoDate(): String? = null

    override suspend fun getUserUsageData(): UserUsageAnalytics = UserUsageAnalytics(0, 0, 0, 0, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())

    override suspend fun recordPlayback(musicId: Long, artist: String?, album: String?, title: String?) {}

    override suspend fun getTopPlayed(limit: Int): List<TopPlayedEntry> = emptyList()

    override suspend fun getRecentPlayback(limit: Int): List<RecentPlaybackEntry> = emptyList()

    override suspend fun getArtistCounts(): List<ArtistCountEntry> = emptyList()

    override suspend fun getLabelCounts(): List<LabelCountEntry> = emptyList()

    override suspend fun getAiProviderConfig(): AiProviderConfig = AiProviderConfig(null, null, null, null, null, null, null)

    override suspend fun saveAiProviderConfig(config: AiProviderConfig) {}

    override suspend fun validateProviderApiKey(providerType: com.hmp.domain.enum.AiProviderType, apiKey: String): AiApiResult<String> = AiApiResult.Failure("Not implemented on iOS yet")

    override suspend fun getLyrics(musicId: Long): String? = null

    override suspend fun saveLyrics(musicId: Long, lyrics: String) {}

    override suspend fun updatePlayCount(musicId: Long) {}

    override suspend fun updateLastPlayed(musicId: Long) {}

    override suspend fun getListeningDurations(): List<ListeningDurationDomain> = emptyList()

    override suspend fun recordListeningDuration(musicId: Long, duration: Long) {}

    override suspend fun exportUserData(): com.hmp.domain.backup.UserBackupSnapshot = com.hmp.domain.backup.UserBackupSnapshot(
        musicUserState = MusicUserStateSnapshot(emptyList(), emptyList()),
        musicExtraUser = MusicExtraUserSnapshot(emptyList(), emptyList()),
        musicLabels = MusicLabelSnapshot(emptyList()),
        userInfo = UserInfoSnapshot(emptyList()),
        listeningStats = ListeningStatsSnapshot(emptyList(), emptyList())
    )

    override suspend fun importUserData(snapshot: com.hmp.domain.backup.UserBackupSnapshot) {}

    override suspend fun getBackups(): List<com.hmp.domain.backup.UserBackupSnapshot> = emptyList()

    override suspend fun deleteBackup(backupId: String) {}
}
