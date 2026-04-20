package com.example.hearablemusicplayer.player.di

import com.hmp.domain.playlist.usecase.ManagePlaylistUseCase
import com.hmp.domain.setting.usecase.CurrentPlaybackUseCase
import com.hmp.domain.setting.usecase.PlaybackHistoryUseCase
import com.hmp.domain.setting.usecase.TimerUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SingletonUseCaseModule {

    @Provides
    @Singleton
    fun provideCurrentPlaybackUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository,
        playlistRepository: com.hmp.domain.playlist.PlaylistRepository,
        settingsRepository: com.hmp.domain.setting.SettingsRepository
    ): CurrentPlaybackUseCase = CurrentPlaybackUseCase(musicRepository, playlistRepository, settingsRepository)

    @Provides
    @Singleton
    fun providePlaybackHistoryUseCase(
        musicRepository: com.hmp.domain.music.MusicRepository
    ): PlaybackHistoryUseCase = PlaybackHistoryUseCase(musicRepository)

    @Provides
    @Singleton
    fun provideTimerUseCase(
        settingsRepository: com.hmp.domain.setting.SettingsRepository
    ): TimerUseCase = TimerUseCase(settingsRepository)

    @Provides
    @Singleton
    fun provideManagePlaylistUseCase(
        playlistRepository: com.hmp.domain.playlist.PlaylistRepository,
        settingsRepository: com.hmp.domain.setting.SettingsRepository
    ): ManagePlaylistUseCase = ManagePlaylistUseCase(playlistRepository, settingsRepository)
}
