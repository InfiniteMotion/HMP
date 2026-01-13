package com.example.hearablemusicplayer.data.di

import com.example.hearablemusicplayer.data.repository.MusicRepositoryImpl
import com.example.hearablemusicplayer.data.repository.PlaylistRepositoryImpl
import com.example.hearablemusicplayer.data.repository.SettingsRepositoryImpl
import com.example.hearablemusicplayer.domain.repository.MusicRepository
import com.example.hearablemusicplayer.domain.repository.PlaylistRepository
import com.example.hearablemusicplayer.domain.repository.SettingsRepository
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
}
