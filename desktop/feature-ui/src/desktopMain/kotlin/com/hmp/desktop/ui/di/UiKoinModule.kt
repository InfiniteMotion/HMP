package com.hmp.desktop.ui.di

import com.hmp.desktop.player.DesktopMusicController
import com.hmp.desktop.ui.common.dialogs.controller.DialogManager
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.common.viewmodel.ThemeViewModel
import com.hmp.desktop.ui.library.viewmodel.LibraryViewModel
import com.hmp.desktop.ui.library.viewmodel.SearchViewModel
import com.hmp.desktop.ui.library.viewmodel.SongDetailViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.playlist.viewmodel.PlaylistViewModel
import com.hmp.desktop.ui.settings.viewmodel.AudioEffectViewModel
import com.hmp.desktop.ui.settings.viewmodel.RecommendationViewModel
import com.hmp.desktop.ui.settings.viewmodel.SettingsViewModel
import com.hmp.desktop.ui.settings.viewmodel.UserUsageDataViewModel
import org.koin.dsl.module

val uiModule = module {
    single { DialogManager() }
    single { DialogManagerViewModel(get()) }
    single { DialogViewModel(get(), get(), get(), get(), get(), get()) }

    single { LibraryViewModel(get(), get(), get(), get(), get(), get()) }
    single { SearchViewModel(get()) }
    single { SongDetailViewModel(get(), get()) }
    single { PlaybackViewModel(get()) }
    single { PlaylistQueueViewModel(get(), get(), get(), get()) }
    single { PlaylistViewModel(get(), get(), get(), get()) }
    single { SettingsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    single { AudioEffectViewModel(get()) }
    single { RecommendationViewModel(get(), get(), get(), get()) }
    single { UserUsageDataViewModel(get()) }
    single { ThemeViewModel(get()) }
}
