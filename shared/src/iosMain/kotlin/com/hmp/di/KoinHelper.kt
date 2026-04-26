package com.hmp.di

import com.hmp.domain.music.MusicRepository
import com.hmp.domain.music.usecase.GetAllMusicUseCase
import com.hmp.domain.music.usecase.LoadMusicFromDeviceUseCase
import com.hmp.domain.music.usecase.SyncMusicFromDeviceIncrementalUseCase
import com.hmp.domain.music.usecase.SearchMusicUseCase
import com.hmp.domain.music.usecase.GetDailyMusicRecommendationUseCase
import com.hmp.domain.music.usecase.RemoveFromLibraryUseCase
import com.hmp.domain.music.usecase.RestoreToLibraryUseCase
import com.hmp.domain.music.usecase.GetDeletedMusicIdsGroupedByFolderUseCase
import com.hmp.domain.music.usecase.MusicLabelUseCase
import com.hmp.domain.playlist.usecase.ManagePlaylistUseCase
import com.hmp.domain.playlist.usecase.GeneratePlaylistUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hmp.domain.setting.usecase.CurrentPlaybackUseCase
import com.hmp.domain.setting.usecase.PlaybackHistoryUseCase
import com.hmp.domain.setting.usecase.TimerUseCase
import com.hmp.domain.setting.usecase.UserSettingsUseCase
import com.hmp.domain.setting.usecase.LyricsSettingsUseCase
import com.hmp.domain.setting.usecase.GetUserUsageDataUseCase
import com.hmp.domain.backup.usecase.ExportUserDataBackupUseCase
import com.hmp.domain.backup.usecase.ImportUserDataBackupUseCase
import com.hmp.domain.backup.usecase.GetBackupsUseCase
import com.hmp.domain.backup.usecase.DeleteBackupUseCase
import com.hmp.domain.enum.LabelCategory
import com.hmp.domain.enum.LabelName
import kotlinx.coroutines.flow.first
import org.koin.mp.KoinPlatform

// Music
fun getGetAllMusicUseCase(): GetAllMusicUseCase =
    KoinPlatform.getKoin().get()

fun getLoadMusicFromDeviceUseCase(): LoadMusicFromDeviceUseCase =
    KoinPlatform.getKoin().get()

fun getSyncMusicFromDeviceIncrementalUseCase(): SyncMusicFromDeviceIncrementalUseCase =
    KoinPlatform.getKoin().get()

fun getSearchMusicUseCase(): SearchMusicUseCase =
    KoinPlatform.getKoin().get()

fun getGetDailyMusicRecommendationUseCase(): GetDailyMusicRecommendationUseCase =
    KoinPlatform.getKoin().get()

fun getRemoveFromLibraryUseCase(): RemoveFromLibraryUseCase =
    KoinPlatform.getKoin().get()

fun getRestoreToLibraryUseCase(): RestoreToLibraryUseCase =
    KoinPlatform.getKoin().get()

fun getGetDeletedMusicIdsGroupedByFolderUseCase(): GetDeletedMusicIdsGroupedByFolderUseCase =
    KoinPlatform.getKoin().get()

fun getMusicLabelUseCase(): MusicLabelUseCase =
    KoinPlatform.getKoin().get()

fun getMusicRepository(): MusicRepository =
    KoinPlatform.getKoin().get()

// Playlist
fun getManagePlaylistUseCase(): ManagePlaylistUseCase =
    KoinPlatform.getKoin().get()

fun getGeneratePlaylistUseCase(): GeneratePlaylistUseCase =
    KoinPlatform.getKoin().get()

// Settings / Playback
fun getCurrentPlaybackUseCase(): CurrentPlaybackUseCase =
    KoinPlatform.getKoin().get()

fun getPlaybackHistoryUseCase(): PlaybackHistoryUseCase =
    KoinPlatform.getKoin().get()

fun getTimerUseCase(): TimerUseCase =
    KoinPlatform.getKoin().get()

fun getSettingsRepository(): SettingsRepository =
    KoinPlatform.getKoin().get()

fun getUserSettingsUseCase(): UserSettingsUseCase =
    KoinPlatform.getKoin().get()

fun getLyricsSettingsUseCase(): LyricsSettingsUseCase =
    KoinPlatform.getKoin().get()

fun getGetUserUsageDataUseCase(): GetUserUsageDataUseCase =
    KoinPlatform.getKoin().get()

// Backup
fun getExportUserDataBackupUseCase(): ExportUserDataBackupUseCase =
    KoinPlatform.getKoin().get()

fun getImportUserDataBackupUseCase(): ImportUserDataBackupUseCase =
    KoinPlatform.getKoin().get()

fun getGetBackupsUseCase(): GetBackupsUseCase =
    KoinPlatform.getKoin().get()

fun getDeleteBackupUseCase(): DeleteBackupUseCase =
    KoinPlatform.getKoin().get()

// Flow → suspend helpers for iOS
suspend fun getSettingsEqualizerPreset(): Int =
    getSettingsRepository().equalizerPreset.first()

suspend fun getSettingsBassBoostLevel(): Int =
    getSettingsRepository().bassBoostLevel.first()

suspend fun getSettingsIsSurroundSoundEnabled(): Boolean =
    getSettingsRepository().isSurroundSoundEnabled.first()

suspend fun getSettingsReverbPreset(): Int =
    getSettingsRepository().reverbPreset.first()

suspend fun getSettingsUserName(): String =
    getUserSettingsUseCase().userName.first()

suspend fun getSettingsCustomMode(): String =
    getUserSettingsUseCase().customMode.first()

suspend fun getSettingsBackgroundStyle(): String =
    getUserSettingsUseCase().backgroundStyle.first()

suspend fun getSettingsAutoBatchProcess(): Boolean =
    getUserSettingsUseCase().autoBatchProcess.first()

suspend fun getLabelNamesByTypeFirst(category: LabelCategory): List<LabelName> {
    return getMusicLabelUseCase().getLabelNamesByType(category).first()
}

suspend fun getMusicWithMissingExtraCount(): Int =
    getGetAllMusicUseCase().getMusicWithMissingExtraCount().first()

suspend fun autoProcessMissingExtra() {
    getGetDailyMusicRecommendationUseCase().autoProcessMissingExtraInfoWithCurrentProvider()
}

suspend fun getSettingsCurrentPosition(): Long =
    getSettingsRepository().currentPosition.first()

suspend fun getCurrentMusicId(): Long? =
    getCurrentPlaybackUseCase().getCurrentMusicId().first()
