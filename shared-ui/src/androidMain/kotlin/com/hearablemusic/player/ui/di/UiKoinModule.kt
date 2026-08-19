package com.hearablemusic.player.ui.di

import android.app.Application

import com.hearablemusic.player.ui.common.dialogs.controller.DialogManager
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogViewModel
import com.hearablemusic.player.ui.common.viewmodel.ThemeViewModel
import com.hearablemusic.player.ui.platform.AlbumArtPixelsLoader
import com.hearablemusic.player.ui.platform.CoilAlbumArtPixelsLoader
import com.hearablemusic.player.ui.platform.MusicControllerPlaybackAdapter
import com.hearablemusic.player.ui.platform.PlaybackController
import com.hearablemusic.player.ui.library.viewmodel.LibraryViewModel
import com.hearablemusic.player.ui.library.viewmodel.LibraryListViewModel
import com.hearablemusic.player.ui.library.viewmodel.EditMusicTagsViewModel
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
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    single { DialogManager() }
    // 第 3 步：播放控制平台服务（冻结接口 → Media3 MusicController 薄委托）
    single<PlaybackController> { MusicControllerPlaybackAdapter(get()) }
    viewModel { DialogManagerViewModel(get()) }
    // 第 2b 步：新层列表主路径（commonMain 类，Android 端注册）
    viewModel { LibraryListViewModel(get()) }
    viewModel { DialogViewModel(get(), get(), get(), get(), get(), get()) }

    viewModel { LibraryViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { SongDetailViewModel(get(), get()) }
    viewModel { EditMusicTagsViewModel(get(), get(), get()) }
    viewModel { PlaybackViewModel(get()) }
    viewModel { PlaylistQueueViewModel(get(), get(), get(), get()) }
    viewModel { PlaylistViewModel(get(), get(), get(), get()) }
    viewModel { ArtistAlbumViewModel(get()) }
    // 第 4 步批 A：settings VM 全家迁 commonMain（去 androidApplication()）
    viewModel { SettingsViewModel(get()) }
    viewModel { AudioEffectViewModel(get()) }
    viewModel { BackupViewModel(get(), get(), get(), get()) }
    viewModel { AiSettingsViewModel(get(), get()) }
    viewModel { LyricsSettingsViewModel(get()) }
    viewModel { RecommendationViewModel(get(), get(), get(), get()) }
    viewModel { UserUsageDataViewModel(get()) }
    // 第 4 步批 B：ThemeViewModel 迁 commonMain（算法下沉，像素走 AlbumArtPixelsLoader）
    single<AlbumArtPixelsLoader> { CoilAlbumArtPixelsLoader(androidContext()) }
    viewModel { ThemeViewModel(get(), get()) }
}
