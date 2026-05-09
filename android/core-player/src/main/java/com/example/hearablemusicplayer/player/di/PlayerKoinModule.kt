package com.example.hearablemusicplayer.player.di

import com.example.hearablemusicplayer.player.controller.MusicController
import com.hmp.domain.playlist.usecase.ManagePlaylistUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hmp.domain.setting.usecase.CurrentPlaybackUseCase
import com.hmp.domain.setting.usecase.PlaybackHistoryUseCase
import com.hmp.domain.setting.usecase.TimerUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val playerModule = module {
    single { MusicController(androidContext(), get(), get(), get(), get(), get()) }
}
