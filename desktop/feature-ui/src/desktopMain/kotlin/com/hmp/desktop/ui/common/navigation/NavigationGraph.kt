package com.hmp.desktop.ui.common.navigation

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import org.koin.compose.koinInject



import com.hmp.desktop.ui.settings.pages.AIScreen
import com.hmp.desktop.ui.library.pages.AlbumScreen
import com.hmp.desktop.ui.library.pages.ArtistScreen
import com.hmp.desktop.ui.settings.pages.AudioEffectsScreen
import com.hmp.desktop.ui.library.pages.CustomScreen
import com.hmp.desktop.ui.library.pages.SearchScreen
import com.hmp.desktop.ui.library.pages.SongDetailScreen
import com.hmp.desktop.ui.settings.pages.UserUsageDataScreen
import com.hmp.desktop.ui.common.pages.TabsHost
import com.hmp.desktop.ui.player.pages.LyricsScreen
import com.hmp.desktop.ui.player.pages.PlayerScreen
import com.hmp.desktop.ui.playlist.pages.PlaylistManageScreen
import com.hmp.desktop.ui.playlist.pages.PlaylistScreen
import com.hmp.desktop.ui.settings.pages.BackupSettingsScreen
import com.hmp.desktop.ui.settings.pages.LibrarySettingsScreen
import com.hmp.desktop.ui.settings.pages.ProfileSettingsScreen
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.library.viewmodel.LibraryViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.playlist.viewmodel.PlaylistViewModel
import com.hmp.desktop.ui.settings.viewmodel.RecommendationViewModel
import com.hmp.desktop.ui.settings.viewmodel.SettingsViewModel
import com.hmp.desktop.ui.common.viewmodel.ThemeViewModel

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
 *
 * @param navController 导航控制器
 * @param pagerState 标签页的Pager状态（用于TabsHost）
 * @param recommendationViewModel 推荐ViewModel
 * @param settingsViewModel 设置ViewModel
 * @param playbackViewModel 播放控制ViewModel
 * @param playlistQueueViewModel 播放队列ViewModel
 * @param themeViewModel 主题ViewModel
 * @param dialogManagerViewModel 对话框管理ViewModel
 * @param dialogViewModel 对话框ViewModel
 * @param tabHeader 标签页头部内容，默认为空
 */
@Composable
fun navigationGraph(
    navController: NavController,
    pagerState: PagerState,
    recommendationViewModel: RecommendationViewModel,
    settingsViewModel: SettingsViewModel,
    playbackViewModel: PlaybackViewModel,
    playlistQueueViewModel: PlaylistQueueViewModel,
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
        val playlistViewModel: PlaylistViewModel = koinInject()
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
    entry<Routes.Settings.ProfileSettings> {
        ProfileSettingsScreen(navController = navController)
    }
    
    entry<Routes.Settings.BackupSettings> {
        BackupSettingsScreen(navController = navController)
    }
    
    entry<Routes.Settings.LibrarySettings> {
        LibrarySettingsScreen(navController = navController)
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
        val playlistViewModel: PlaylistViewModel = koinInject()
        PlaylistScreen(
            navController = navController,
            playlistName = route.name,
            playbackViewModel = playbackViewModel,
            playlistQueueViewModel = playlistQueueViewModel,
            dialogViewModel = dialogViewModel,
        )
    }

    entry<Routes.Playlist.CustomPlaylist> { route ->
        val playlistViewModel: PlaylistViewModel = koinInject()
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
        LyricsScreen(playbackViewModel = playbackViewModel, playlistQueueViewModel = playlistQueueViewModel, navController = navController)
    }
    
    // AI 模块
    entry<Routes.AI.AI> {
        val libraryViewModel: LibraryViewModel = koinInject()
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