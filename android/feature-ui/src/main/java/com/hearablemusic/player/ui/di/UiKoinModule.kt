package com.hearablemusic.player.ui.di

import android.app.Application

import com.hearablemusic.player.ui.common.dialogs.controller.DialogManager
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogViewModel
import com.hearablemusic.player.ui.common.viewmodel.ThemeViewModel
import com.hearablemusic.player.ui.library.viewmodel.LibraryViewModel
import com.hearablemusic.player.ui.library.viewmodel.EditMusicTagsViewModel
import com.hearablemusic.player.ui.library.viewmodel.SearchViewModel
import com.hearablemusic.player.ui.library.viewmodel.SongDetailViewModel
import com.hearablemusic.player.ui.player.viewmodel.PlaybackViewModel
import com.hearablemusic.player.ui.player.viewmodel.PlaylistQueueViewModel
import com.hearablemusic.player.ui.playlist.viewmodel.PlaylistViewModel
import com.hearablemusic.player.ui.settings.viewmodel.AudioEffectViewModel
import com.hearablemusic.player.ui.settings.viewmodel.RecommendationViewModel
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel
import com.hearablemusic.player.ui.settings.viewmodel.UserUsageDataViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val uiModule = module {
    single { DialogManager() }
    single { DialogManagerViewModel(get()) }
    single { DialogViewModel(androidApplication(), get(), get(), get(), get(), get(), get()) }

    single { LibraryViewModel(androidApplication(), get(), get(), get(), get(), get(), get(), get()) }
    single { SearchViewModel(androidApplication(), get()) }
    single { SongDetailViewModel(androidApplication(), get(), get()) }
    single { EditMusicTagsViewModel(androidApplication(), get(), get()) }
    single { PlaybackViewModel(get()) }
    single { PlaylistQueueViewModel(androidApplication(), get(), get(), get(), get()) }
    single { PlaylistViewModel(androidApplication(), get(), get(), get(), get()) }
    single { SettingsViewModel(androidApplication(), get(), get(), get(), get(), get(), get(), get()) }
    single { AudioEffectViewModel(get()) }
    single { RecommendationViewModel(get(), get(), get(), get()) }
    single { UserUsageDataViewModel(androidApplication(), get()) }
    single { ThemeViewModel(get(), get()) }
}
