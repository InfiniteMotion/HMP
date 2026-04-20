package com.example.hearablemusicplayer.data.di

import com.example.hearablemusicplayer.data.repository.BackupFileRepositoryImpl
import com.example.hearablemusicplayer.data.repository.MusicRepositoryImpl
import com.example.hearablemusicplayer.data.repository.PlaylistRepositoryImpl
import com.example.hearablemusicplayer.data.repository.SettingsRepositoryImpl
import com.hmp.domain.backup.BackupFileRepository
import com.hmp.domain.music.MusicRepository
import com.hmp.domain.playlist.PlaylistRepository
import com.hmp.domain.setting.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl
    ): MusicRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        playlistRepositoryImpl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindBackupFileRepository(
        backupFileRepositoryImpl: BackupFileRepositoryImpl
    ): BackupFileRepository
}
