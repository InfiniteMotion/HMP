package com.example.hearablemusicplayer.ui.di

import com.hmp.domain.backup.usecase.DeleteBackupUseCase
import com.hmp.domain.backup.usecase.ExportUserDataBackupUseCase
import com.hmp.domain.backup.usecase.GetBackupsUseCase
import com.hmp.domain.backup.usecase.ImportUserDataBackupUseCase
import com.hmp.domain.music.usecase.GetAllMusicUseCase
import com.hmp.domain.music.usecase.GetDailyMusicRecommendationUseCase
import com.hmp.domain.music.usecase.GetDeletedMusicIdsGroupedByFolderUseCase
import com.hmp.domain.music.usecase.LoadMusicFromDeviceUseCase
import com.hmp.domain.music.usecase.MusicLabelUseCase
import com.hmp.domain.music.usecase.RemoveFromLibraryUseCase
import com.hmp.domain.music.usecase.RestoreToLibraryUseCase
import com.hmp.domain.music.usecase.SearchMusicUseCase
import com.hmp.domain.music.usecase.SyncMusicFromDeviceIncrementalUseCase
import com.hmp.domain.playlist.usecase.GeneratePlaylistUseCase
import com.hmp.domain.setting.usecase.GetUserUsageDataUseCase
import com.hmp.domain.setting.usecase.LyricsSettingsUseCase
import com.hmp.domain.setting.usecase.UserSettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideGetAllMusicUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository
    ): GetAllMusicUseCase = GetAllMusicUseCase(musicRepository)

    @Provides
    @ViewModelScoped
    fun provideGetDailyMusicRecommendationUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository,
        settingsRepository: com.hmp.domain.setting.SettingsRepository,
        musicLabelUseCase: MusicLabelUseCase
    ): GetDailyMusicRecommendationUseCase =
        GetDailyMusicRecommendationUseCase(musicRepository, settingsRepository, musicLabelUseCase)

    @Provides
    @ViewModelScoped
    fun provideGetDeletedMusicIdsGroupedByFolderUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository
    ): GetDeletedMusicIdsGroupedByFolderUseCase =
        GetDeletedMusicIdsGroupedByFolderUseCase(musicRepository)

    @Provides
    @ViewModelScoped
    fun provideLoadMusicFromDeviceUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository
    ): LoadMusicFromDeviceUseCase = LoadMusicFromDeviceUseCase(musicRepository)

    @Provides
    @ViewModelScoped
    fun provideMusicLabelUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository,
        playlistRepository: com.hmp.domain.playlist.PlaylistRepository
    ): MusicLabelUseCase = MusicLabelUseCase(musicRepository, playlistRepository)

    @Provides
    @ViewModelScoped
    fun provideRemoveFromLibraryUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository
    ): RemoveFromLibraryUseCase = RemoveFromLibraryUseCase(musicRepository)

    @Provides
    @ViewModelScoped
    fun provideRestoreToLibraryUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository
    ): RestoreToLibraryUseCase = RestoreToLibraryUseCase(musicRepository)

    @Provides
    @ViewModelScoped
    fun provideSearchMusicUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository
    ): SearchMusicUseCase = SearchMusicUseCase(musicRepository)

    @Provides
    @ViewModelScoped
    fun provideSyncMusicFromDeviceIncrementalUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository
    ): SyncMusicFromDeviceIncrementalUseCase =
        SyncMusicFromDeviceIncrementalUseCase(musicRepository)

    @Provides
    @ViewModelScoped
    fun provideGeneratePlaylistUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository,
        settingsRepository: com.hmp.domain.setting.SettingsRepository
    ): GeneratePlaylistUseCase =
        GeneratePlaylistUseCase(musicRepository, settingsRepository)

    @Provides
    @ViewModelScoped
    fun provideUserSettingsUseCase(
        settingsRepository: com.hmp.domain.setting.SettingsRepository
    ): UserSettingsUseCase = UserSettingsUseCase(settingsRepository)

    @Provides
    @ViewModelScoped
    fun provideLyricsSettingsUseCase(
        settingsRepository: com.hmp.domain.setting.SettingsRepository
    ): LyricsSettingsUseCase = LyricsSettingsUseCase(settingsRepository)

    @Provides
    @ViewModelScoped
    fun provideGetUserUsageDataUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository
    ): GetUserUsageDataUseCase = GetUserUsageDataUseCase(musicRepository)

    @Provides
    @ViewModelScoped
    fun provideExportUserDataBackupUseCase(
        settingsRepository: com.hmp.domain.setting.SettingsRepository,
        musicRepository: com.hmp.domain.music.MusicRepository,
        playlistRepository: com.hmp.domain.playlist.PlaylistRepository,
        backupFileRepository: com.hmp.domain.backup.BackupFileRepository
    ): ExportUserDataBackupUseCase =
        ExportUserDataBackupUseCase(settingsRepository, musicRepository, playlistRepository, backupFileRepository)

    @Provides
    @ViewModelScoped
    fun provideImportUserDataBackupUseCase(
        settingsRepository: com.hmp.domain.setting.SettingsRepository,
        musicRepository: com.hmp.domain.music.MusicRepository,
        playlistRepository: com.hmp.domain.playlist.PlaylistRepository,
        backupFileRepository: com.hmp.domain.backup.BackupFileRepository
    ): ImportUserDataBackupUseCase =
        ImportUserDataBackupUseCase(settingsRepository, musicRepository, playlistRepository, backupFileRepository)

    @Provides
    @ViewModelScoped
    fun provideGetBackupsUseCase(
        backupFileRepository: com.hmp.domain.backup.BackupFileRepository
    ): GetBackupsUseCase = GetBackupsUseCase(backupFileRepository)

    @Provides
    @ViewModelScoped
    fun provideDeleteBackupUseCase(
        backupFileRepository: com.hmp.domain.backup.BackupFileRepository
    ): DeleteBackupUseCase = DeleteBackupUseCase(backupFileRepository)
}
