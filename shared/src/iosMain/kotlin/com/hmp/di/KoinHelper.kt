package com.hmp.di

import com.hmp.domain.music.MusicRepository
import com.hmp.domain.music.usecase.GetAllMusicUseCase
import com.hmp.domain.music.usecase.LoadMusicFromDeviceUseCase
import com.hmp.domain.music.usecase.SyncMusicFromDeviceIncrementalUseCase
import org.koin.mp.KoinPlatform

fun getGetAllMusicUseCase(): GetAllMusicUseCase =
    KoinPlatform.getKoin().get()

fun getLoadMusicFromDeviceUseCase(): LoadMusicFromDeviceUseCase =
    KoinPlatform.getKoin().get()

fun getSyncMusicFromDeviceIncrementalUseCase(): SyncMusicFromDeviceIncrementalUseCase =
    KoinPlatform.getKoin().get()

fun getMusicRepository(): MusicRepository =
    KoinPlatform.getKoin().get()
