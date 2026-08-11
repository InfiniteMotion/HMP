package com.hearablemusic.player.ui.di

import android.app.Application

import com.hearablemusic.player.ui.common.dialogs.controller.DialogManager
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogViewModel
import com.hearablemusic.player.ui.common.viewmodel.ThemeViewModel
import com.hearablemusic.player.ui.library.viewmodel.LibraryViewModel
import com.hearablemusic.player.ui.library.viewmodel.SearchViewModel
import com.hearablemusic.player.ui.library.viewmodel.SongDetailViewModel
import com.hearablemusic.player.ui.player.viewmodel.PlaybackViewModel
import com.hearablemusic.player.ui.player.viewmodel.PlaylistQueueViewModel
import com.hearablemusic.player.ui.playlist.viewmodel.PlaylistViewModel
import com.hearablemusic.player.ui.playlist.viewmodel.ArtistAlbumViewModel
import com.hearablemusic.player.ui.settings.viewmodel.AudioEffectViewModel
import com.hearablemusic.player.ui.settings.viewmodel.AiSettingsViewModel
import com.hearablemusic.player.ui.settings.viewmodel.BackupViewModel
import com.hearablemusic.player.ui.settings.viewmodel.LyricsSettingsViewModel
import com.hearablemusic.player.ui.settings.viewmodel.RecommendationViewModel
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel
import com.hearablemusic.player.ui.settings.viewmodel.UserUsageDataViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    single { DialogManager() }
    viewModel { DialogManagerViewModel(get()) }
    viewModel { DialogViewModel(androidApplication(), get(), get(), get(), get(), get(), get()) }

    viewModel { LibraryViewModel(androidApplication(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SearchViewModel(androidApplication(), get()) }
    viewModel { SongDetailViewModel(androidApplication(), get(), get()) }
    viewModel { PlaybackViewModel(get()) }
    viewModel { PlaylistQueueViewModel(androidApplication(), get(), get(), get(), get()) }
    viewModel { PlaylistViewModel(androidApplication(), get(), get(), get(), get()) }
    viewModel { ArtistAlbumViewModel(get()) }
    viewModel { SettingsViewModel(androidApplication(), get()) }
    viewModel { AudioEffectViewModel(get()) }
    viewModel { BackupViewModel(androidApplication(), get(), get(), get(), get()) }
    viewModel { AiSettingsViewModel(androidApplication(), get(), get()) }
    viewModel { LyricsSettingsViewModel(androidApplication(), get()) }
    viewModel { RecommendationViewModel(get(), get(), get(), get()) }
    viewModel { UserUsageDataViewModel(androidApplication(), get()) }
    viewModel { ThemeViewModel(get(), get()) }
}
