package com.example.hearablemusicplayer.ui.pages.base

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.NavDestination.Companion.hasRoute
import androidx.navigation3.compose.NavDisplay
import androidx.navigation3.compose.rememberNavManager
import androidx.navigation3.compose.rememberNavState
import com.example.hearablemusicplayer.ui.components.MiniPlayerBar
import com.example.hearablemusicplayer.ui.pages.AIScreen
import com.example.hearablemusicplayer.ui.pages.ArtistScreen
import com.example.hearablemusicplayer.ui.pages.AudioEffectsScreen
import com.example.hearablemusicplayer.ui.pages.CustomScreen
import com.example.hearablemusicplayer.ui.pages.playlist.PlaylistScreen
import com.example.hearablemusicplayer.ui.pages.SearchScreen
import com.example.hearablemusicplayer.ui.pages.settings.BackupSettingsScreen
import com.example.hearablemusicplayer.ui.pages.settings.LibrarySettingsScreen
import com.example.hearablemusicplayer.ui.pages.settings.ProfileSettingsScreen
import com.example.hearablemusicplayer.ui.pages.settings.SettingScreen
import com.example.hearablemusicplayer.ui.pages.SongDetailScreen
import com.example.hearablemusicplayer.ui.pages.playlist.PlaylistManageScreen
import com.example.hearablemusicplayer.ui.pages.UserUsageDataScreen
import com.example.hearablemusicplayer.ui.pages.player.PlayerScreen
import com.example.hearablemusicplayer.ui.pages.player.LyricsScreen
import com.example.hearablemusicplayer.ui.theme.generateDynamicColorScheme
import com.example.hearablemusicplayer.ui.theme.getPresetColorScheme
import com.example.hearablemusicplayer.ui.util.AnimationConfig
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel

@OptIn(UnstableApi::class)
@Composable
fun MainScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    recommendationViewModel: RecommendationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel()
) {
    // 订阅调色板、当前曲目与播放状态
    val currentMusic by playControlViewModel.currentPlayingMusic.collectAsState()
    val paletteColors by playControlViewModel.paletteColors.collectAsState()
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    val currentPosition by playControlViewModel.currentPosition.collectAsState()
    val duration by playControlViewModel.duration.collectAsState()

    // 根据customMode确定主题模式
    val customMode by settingsViewModel.customMode.collectAsState("default")
    val backgroundStyleString by settingsViewModel.backgroundStyle.collectAsState("FLUID")
    val backgroundStyle = try {
        BackgroundStyle.valueOf(backgroundStyleString)
    } catch (e: Exception) {
        BackgroundStyle.FLUID
    }
    
    val isDarkTheme = when (customMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    
    // 根据播放状态选择主题: 播放时使用动态主题,暂停时使用预置主题
    val colorScheme = if (isPlaying) {
        generateDynamicColorScheme(paletteColors, isDarkTheme)
    } else {
        getPresetColorScheme(isDarkTheme)
    }

    val defaultScreen = Routes.Tabs
    val navState = rememberNavState {
        initialDestination = defaultScreen
    }
    val navManager = rememberNavManager(navState)
    val navBackStackEntry = navState.backStack.lastOrNull()

    val tabCount = 4
    val savedTabIndex = rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberTabsPagerState(
        pageCount = tabCount,
        initialPage = savedTabIndex.intValue
    )
    LaunchedEffect(pagerState.currentPage) {
        savedTabIndex.intValue = pagerState.currentPage
    }
    val tabHeader: @Composable () -> Unit = {
        TabPageIndicator(
            currentPage = pagerState.currentPage,
            totalPages = tabCount,
            modifier = Modifier
                .fillMaxWidth()
        )
    }

    // 应用主题(根据播放状态切换)
    MaterialTheme(
        colorScheme = colorScheme
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. 静态背景层 (始终存在，确保无黑屏)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )

            // 2. 全局动态背景层 (仅在播放时覆盖在静态背景之上，带过渡动画)
            AnimatedVisibility(
                visible = isPlaying && currentMusic != null,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 800, easing = AnimationConfig.EASE_IN_OUT)
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 800, easing = AnimationConfig.EASE_IN_OUT)
                )
            ) {
                DynamicBackground(
                    albumArtUri = currentMusic?.music?.albumArtUri,
                    paletteColors = paletteColors,
                    isDarkTheme = isDarkTheme,
                    style = backgroundStyle,
                    modifier = Modifier
                )
            }

            // 前景页面内容
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                containerColor = Transparent,
                bottomBar = {}
            ) {
                val contentModifier = if (navBackStackEntry?.destination?.hasRoute<Routes.Player>() == true) {
                    Modifier.padding(it)
                } else if (navBackStackEntry?.destination?.hasRoute<Routes.Lyrics>() == true) {
                    Modifier.padding(it)
                } else {
                    Modifier
                        .padding(it)
                        .statusBarsPadding()
                }
                Box(
                    modifier = contentModifier
                ) {
                    NavDisplay(
                        navState = navState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 渲染当前目的地
                        when (val destination = it.lastOrNull()?.destination) {
                            is Routes.Tabs -> {
                                TabsHost(
                                    navController = navManager,
                                    pagerState = pagerState,
                                    // 不再传递 tabHeader，因为已经在外部显示
                                    tabHeader = {},
                                    recommendationViewModel = recommendationViewModel,
                                    settingsViewModel = settingsViewModel
                                )
                            }
                            is Routes.SongDetail -> {
                                SongDetailScreen(
                                    navController = navManager
                                )
                            }
                            is Routes.Player -> {
                                PlayerScreen(navController = navManager)
                            }
                            is Routes.Setting -> {
                                SettingScreen(navManager)
                            }
                            is Routes.ProfileSettings -> {
                                ProfileSettingsScreen(navController = navManager)
                            }
                            is Routes.BackupSettings -> {
                                BackupSettingsScreen(navController = navManager)
                            }
                            is Routes.LibrarySettings -> {
                                LibrarySettingsScreen(navController = navManager)
                            }
                            is Routes.Search -> {
                                SearchScreen(navController = navManager)
                            }
                            is Routes.Playlist -> {
                                PlaylistScreen(navController = navManager)
                            }
                            is Routes.CustomPlaylist -> {
                                PlaylistScreen(navController = navManager)
                            }
                            is Routes.UserPlaylistManage -> {
                                PlaylistManageScreen(navController = navManager)
                            }
                            is Routes.Artist -> {
                                ArtistScreen(navController = navManager)
                            }
                            is Routes.AudioEffects -> {
                                AudioEffectsScreen(navController = navManager)
                            }
                            is Routes.AI -> {
                                AIScreen(
                                    settingsViewModel,
                                    recommendationViewModel,
                                    libraryViewModel,
                                    navManager
                                )
                            }
                            is Routes.Custom -> {
                                CustomScreen(settingsViewModel, navManager)
                            }
                            is Routes.Lyrics -> {
                                LyricsScreen()
                            }
                            is Routes.UserUsageData -> {
                                UserUsageDataScreen(navController = navManager)
                            }
                        }
                    }
                    
                    // 在导航宿主之上显示固定的 TabPageIndicator
                    val isInTabs = navBackStackEntry?.destination?.hasRoute<Routes.Tabs>() == true
                    AnimatedVisibility(
                        visible = isInTabs,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = AnimationConfig.EASE_OUT
                            )
                        ) + scaleIn(
                            initialScale = 0.8f,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = AnimationConfig.EASE_OUT
                            )
                        ) + slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = AnimationConfig.EASE_OUT
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = AnimationConfig.EASE_IN
                            )
                        ) + scaleOut(
                            targetScale = 0.9f,
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = AnimationConfig.EASE_IN
                            )
                        ) + slideOutVertically(
                            targetOffsetY = { -it / 2 },
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = AnimationConfig.EASE_IN
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            tabHeader()
                        }
                    }
                }
                // 使用 AnimatedVisibility 包裹 MiniPlayerBar 实现滑入滑出动画
                val isMiniPlayerVisible by playControlViewModel.isMiniPlayerVisible.collectAsState()
                AnimatedVisibility(
                    visible = navBackStackEntry?.destination?.hasRoute<Routes.Player>() == false && isMiniPlayerVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it }, // 从底部滑入 (偏移量为自身高度)
                        animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it }, // 向底部滑出 (偏移量为自身高度)
                        animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .navigationBarsPadding()
                    ) {
                        MiniPlayerBar(
                            musicInfo = currentMusic,
                            isPlaying = isPlaying,
                            progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                            onPlayPause = {
                                if (isPlaying) {
                                    playControlViewModel.pauseMusic()
                                } else {
                                    playControlViewModel.playOrResume()
                                }
                            },
                            onNext = { playControlViewModel.playNext() },
                            onPrev = { playControlViewModel.playPrevious() },
                            onOpenPlayer = { navManager.navigate(Routes.Player) }
                        )
                    }
                }
            }
        }
    }
}


