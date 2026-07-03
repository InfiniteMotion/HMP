package com.example.hearablemusicplayer.ui.di

import com.example.hearablemusicplayer.player.controller.MusicController
import com.example.hearablemusicplayer.ui.common.dialogs.controller.DialogManager
import com.example.hearablemusicplayer.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.example.hearablemusicplayer.ui.common.dialogs.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.common.viewmodel.ThemeViewModel
import com.example.hearablemusicplayer.ui.library.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.library.viewmodel.SearchViewModel
import com.example.hearablemusicplayer.ui.library.viewmodel.SongDetailViewModel
import com.example.hearablemusicplayer.ui.player.viewmodel.PlaybackViewModel
import com.example.hearablemusicplayer.ui.player.viewmodel.PlaylistQueueViewModel
import com.example.hearablemusicplayer.ui.playlist.viewmodel.PlaylistViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.AudioEffectViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.SettingsViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.UserUsageDataViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    single { DialogManager() }
    single { DialogManagerViewModel(get()) }
    single { DialogViewModel(get(), get(), get(), get(), get(), get()) }

    single { LibraryViewModel(get(), get(), get(), get(), get(), get(), get()) }
    single { SearchViewModel(get()) }
    single { SongDetailViewModel(get(), get()) }
    single { PlaybackViewModel(get()) }
    single { PlaylistQueueViewModel(get(), get(), get(), get()) }
    single { PlaylistViewModel(get(), get(), get(), get()) }
    single { SettingsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    single { AudioEffectViewModel(get()) }
    single { RecommendationViewModel(get(), get(), get(), get()) }
    single { UserUsageDataViewModel(get()) }
    single { ThemeViewModel(get(), get()) }
}
