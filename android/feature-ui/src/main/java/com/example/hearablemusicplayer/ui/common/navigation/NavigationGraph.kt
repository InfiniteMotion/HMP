package com.example.hearablemusicplayer.ui.common.navigation

import androidx.annotation.OptIn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.example.hearablemusicplayer.ui.settings.pages.AIScreen
import com.example.hearablemusicplayer.ui.library.pages.AlbumScreen
import com.example.hearablemusicplayer.ui.library.pages.ArtistScreen
import com.example.hearablemusicplayer.ui.settings.pages.AudioEffectsScreen
import com.example.hearablemusicplayer.ui.library.pages.CustomScreen
import com.example.hearablemusicplayer.ui.library.pages.SearchScreen
import com.example.hearablemusicplayer.ui.library.pages.SongDetailScreen
import com.example.hearablemusicplayer.ui.settings.pages.UserUsageDataScreen
import com.example.hearablemusicplayer.ui.common.pages.TabsHost
import com.example.hearablemusicplayer.ui.player.pages.LyricsScreen
import com.example.hearablemusicplayer.ui.player.pages.PlayerScreen
import com.example.hearablemusicplayer.ui.playlist.pages.PlaylistManageScreen
import com.example.hearablemusicplayer.ui.playlist.pages.PlaylistScreen
import com.example.hearablemusicplayer.ui.settings.pages.BackupSettingsScreen
import com.example.hearablemusicplayer.ui.settings.pages.LibrarySettingsScreen
import com.example.hearablemusicplayer.ui.settings.pages.ProfileSettingsScreen
import com.example.hearablemusicplayer.ui.settings.pages.SettingScreen
import com.example.hearablemusicplayer.ui.settings.pages.LyricsSettingsPage
import com.example.hearablemusicplayer.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.example.hearablemusicplayer.ui.common.dialogs.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.library.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.player.viewmodel.PlaybackViewModel
import com.example.hearablemusicplayer.ui.player.viewmodel.PlaylistQueueViewModel
import com.example.hearablemusicplayer.ui.playlist.viewmodel.PlaylistViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.SettingsViewModel
import com.example.hearablemusicplayer.ui.common.viewmodel.ThemeViewModel
import com.hmp.domain.setting.usecase.LyricsSettingsUseCase
import org.koin.compose.koinInject

/**
 * 导航图定义
 * 集中管理所有页面的导航配置，包含动画和参数传递
 *
 * 本文件使用 Navigation3 的声明式 API (entryProvider + entry) 定义路由与页面的映射关系，
 * 每个路由可以独立配置转场动画和深层链接。这种集中式管理便于维护和确保类型安全。
 *
 * 使用模式：
 * 1. 使用 `entry<路由类型> { ... }` 注册页面，其中 lambda 接收路由参数（如果是 data class）
 * 2. 可以通过 `deepLinks` 参数为路由添加深层链接支持
 * 3. 可以通过 `metadata` 参数配置转场动画（进入、退出、预测性返回）
 * 4. 所有页面所需的依赖应通过参数传入，避免在 NavigationGraph 内部直接使用 koinViewModel()
 *
 * @param navController 导航控制器
 * @param pagerState 标签页的Pager状态（用于TabsHost）
 * @param libraryViewModel 音乐库ViewModel
 * @param recommendationViewModel 推荐ViewModel
 * @param settingsViewModel 设置ViewModel
 * @param playbackViewModel 播放控制ViewModel
 * @param playlistQueueViewModel 播放队列ViewModel
 * @param playlistViewModel 播放列表ViewModel
 * @param themeViewModel 主题ViewModel
 * @param dialogManagerViewModel 对话框管理ViewModel
 * @param dialogViewModel 对话框ViewModel
 * @param tabHeader 标签页头部内容，默认为空
 */
@OptIn(UnstableApi::class)
@Composable
fun navigationGraph(
    navController: NavBackStack<NavKey>,
    pagerState: PagerState,
    libraryViewModel: LibraryViewModel,
    recommendationViewModel: RecommendationViewModel,
    settingsViewModel: SettingsViewModel,
    playbackViewModel: PlaybackViewModel,
    playlistQueueViewModel: PlaylistQueueViewModel,
    playlistViewModel: PlaylistViewModel,
    themeViewModel: ThemeViewModel,
    dialogManagerViewModel: DialogManagerViewModel,
    dialogViewModel: DialogViewModel,
    tabHeader: @Composable () -> Unit = {}
) = entryProvider {
    // Main 模块
    entry<Routes.Main.Tabs> {
        TabsHost(
            navController = navController,
            pagerState = pagerState,
            tabHeader = tabHeader,
            recommendationViewModel = recommendationViewModel,
            settingsViewModel = settingsViewModel,
            playbackViewModel = playbackViewModel,
            playlistQueueViewModel = playlistQueueViewModel,
            dialogViewModel = dialogViewModel,
        )
    }

    entry<Routes.Library.SongDetail> { route ->
        SongDetailScreen(
            navController = navController,
            musicId = route.musicId,
        )
    }
    
    // Player 模块
    entry<Routes.Player.Player> {
        PlayerScreen(
            playbackViewModel = playbackViewModel,
            playlistQueueViewModel = playlistQueueViewModel,
            playlistViewModel = playlistViewModel,
            settingsViewModel = settingsViewModel,
            themeViewModel = themeViewModel,
            navController = navController
        )
    }
    
    // Settings 模块
    entry<Routes.Settings.Setting> {
        SettingScreen(navController)
    }
    
    entry<Routes.Settings.ProfileSettings> {
        ProfileSettingsScreen(navController = navController)
    }
    
    entry<Routes.Settings.BackupSettings> {
        BackupSettingsScreen(navController = navController)
    }
    
    entry<Routes.Settings.LibrarySettings> {
        LibrarySettingsScreen(navController = navController)
    }

    entry<Routes.Settings.LyricsSettings> {
        val useCase: LyricsSettingsUseCase = koinInject()
        LyricsSettingsPage(
            lyricsSettingsUseCase = useCase,
            onBack = { navController.removeLastOrNull() }
        )
    }
    
    // Library 模块
    entry<Routes.Library.Search> {
        SearchScreen(
            navController = navController,
            playbackViewModel = playbackViewModel,
            playlistQueueViewModel = playlistQueueViewModel,
            dialogViewModel = dialogViewModel
        )
    }
    
    entry<Routes.Playlist.Playlist> { route ->
        PlaylistScreen(
            navController = navController,
            playlistName = route.name,
            playbackViewModel = playbackViewModel,
            playlistQueueViewModel = playlistQueueViewModel,
            dialogViewModel = dialogViewModel,
        )
    }
    
    entry<Routes.Playlist.CustomPlaylist> { route ->
        PlaylistScreen(
            navController = navController,
            playlistId = route.playlistId,
            playbackViewModel = playbackViewModel,
            playlistQueueViewModel = playlistQueueViewModel,
            dialogViewModel = dialogViewModel
        )
    }
    
    entry<Routes.Playlist.UserPlaylistManage> {
        PlaylistManageScreen(navController = navController)
    }
    
    entry<Routes.Library.Artist> { route ->
        ArtistScreen(
            navController = navController,
            artistName = route.name,
            playbackViewModel = playbackViewModel,
            playlistQueueViewModel = playlistQueueViewModel,
            dialogViewModel = dialogViewModel
        )
    }
    
    entry<Routes.Library.Album> { route ->
        AlbumScreen(
            navController = navController,
            albumName = route.name,
            playbackViewModel = playbackViewModel,
            playlistQueueViewModel = playlistQueueViewModel,
            dialogViewModel = dialogViewModel
        )
    }
    
    // Player 模块其他路由
    entry<Routes.Player.AudioEffects> {
        AudioEffectsScreen(navController = navController)
    }
    
    entry<Routes.Player.Lyrics> {
        LyricsScreen(
            playbackViewModel = playbackViewModel,
            playlistQueueViewModel = playlistQueueViewModel,
            onNavigateToSettings = { navController.add(Routes.Settings.LyricsSettings) }
        )
    }
    
    // AI 模块
    entry<Routes.AI.AI> {
        AIScreen(
            settingsViewModel,
            recommendationViewModel,
            libraryViewModel,
            dialogManagerViewModel.dialogManager,
            navController
        )
    }
    
    // Custom 模块
    entry<Routes.Custom.Custom> {
        CustomScreen(settingsViewModel, navController)
    }
    
    // UserData 模块
    entry<Routes.UserData.UserUsageData> {
        UserUsageDataScreen(navController = navController)
    }
}