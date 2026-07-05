package com.hearablemusic.player.player.di

import com.hearablemusic.player.player.controller.MusicController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val playerModule = module {
    single { MusicController(androidContext(), get(), get(), get(), get(), get()) }
}
