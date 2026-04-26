package com.hmp.di

import com.hmp.domain.music.MusicRepository
import com.hmp.domain.music.usecase.GetAllMusicUseCase
import com.hmp.domain.music.usecase.LoadMusicFromDeviceUseCase
import com.hmp.domain.music.usecase.SyncMusicFromDeviceIncrementalUseCase
import com.hmp.domain.playlist.usecase.ManagePlaylistUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hmp.domain.setting.usecase.CurrentPlaybackUseCase
import com.hmp.domain.setting.usecase.PlaybackHistoryUseCase
import com.hmp.domain.setting.usecase.TimerUseCase
import org.koin.mp.KoinPlatform

fun getGetAllMusicUseCase(): GetAllMusicUseCase =
    KoinPlatform.getKoin().get()

fun getLoadMusicFromDeviceUseCase(): LoadMusicFromDeviceUseCase =
    KoinPlatform.getKoin().get()

fun getSyncMusicFromDeviceIncrementalUseCase(): SyncMusicFromDeviceIncrementalUseCase =
    KoinPlatform.getKoin().get()

fun getMusicRepository(): MusicRepository =
    KoinPlatform.getKoin().get()

fun getCurrentPlaybackUseCase(): CurrentPlaybackUseCase =
    KoinPlatform.getKoin().get()

fun getPlaybackHistoryUseCase(): PlaybackHistoryUseCase =
    KoinPlatform.getKoin().get()

fun getTimerUseCase(): TimerUseCase =
    KoinPlatform.getKoin().get()

fun getManagePlaylistUseCase(): ManagePlaylistUseCase =
    KoinPlatform.getKoin().get()

fun getSettingsRepository(): SettingsRepository =
    KoinPlatform.getKoin().get()
