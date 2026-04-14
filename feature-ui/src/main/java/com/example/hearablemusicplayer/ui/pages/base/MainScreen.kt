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
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.hearablemusicplayer.ui.components.MiniPlayerBar
import com.example.hearablemusicplayer.ui.dialogs.CreatePlaylistDialog
import com.example.hearablemusicplayer.ui.dialogs.MessageToast
import com.example.hearablemusicplayer.ui.dialogs.MusicDetailDialog
import com.example.hearablemusicplayer.ui.dialogs.MusicPickerDialog
import com.example.hearablemusicplayer.ui.pages.AIScreen
import com.example.hearablemusicplayer.ui.pages.ArtistScreen
import com.example.hearablemusicplayer.ui.pages.AudioEffectsScreen
import com.example.hearablemusicplayer.ui.pages.CustomScreen
import com.example.hearablemusicplayer.ui.pages.SearchScreen
import com.example.hearablemusicplayer.ui.pages.SongDetailScreen
import com.example.hearablemusicplayer.ui.pages.UserUsageDataScreen
import com.example.hearablemusicplayer.ui.pages.player.LyricsScreen
import com.example.hearablemusicplayer.ui.pages.player.PlayerScreen
import com.example.hearablemusicplayer.ui.pages.playlist.PlaylistManageScreen
import com.example.hearablemusicplayer.ui.pages.playlist.PlaylistScreen
import com.example.hearablemusicplayer.ui.pages.settings.BackupSettingsScreen
import com.example.hearablemusicplayer.ui.pages.settings.LibrarySettingsScreen
import com.example.hearablemusicplayer.ui.pages.settings.ProfileSettingsScreen
import com.example.hearablemusicplayer.ui.pages.settings.SettingScreen
import com.example.hearablemusicplayer.ui.theme.generateDynamicColorScheme
import com.example.hearablemusicplayer.ui.theme.getPresetColorScheme
import com.example.hearablemusicplayer.ui.util.AnimationConfig
import com.example.hearablemusicplayer.ui.util.DEFAULT_HAZE_BLUR_RADIUS
import com.example.hearablemusicplayer.ui.util.DialogEvent
import com.example.hearablemusicplayer.ui.util.DEFAULT_HAZE_INTENSITY
import com.example.hearablemusicplayer.ui.util.DEFAULT_HAZE_MATERIAL_PRESET
import com.example.hearablemusicplayer.ui.util.DEFAULT_HAZE_MODE
import com.example.hearablemusicplayer.ui.util.DEFAULT_HAZE_NOISE_FACTOR
import com.example.hearablemusicplayer.ui.util.DEFAULT_HAZE_TINT_ALPHA
import com.example.hearablemusicplayer.ui.util.HazeRenderSettings
import com.example.hearablemusicplayer.ui.util.ProvideHazeRenderSettings
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.viewmodel.DialogManagerViewModel
import com.example.hearablemusicplayer.ui.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel
import dev.chrisbanes.haze.hazeSource

@OptIn(UnstableApi::class)
@Composable
fun MainScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    recommendationViewModel: RecommendationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel(),
    dialogManagerViewModel: DialogManagerViewModel = hiltViewModel(),
    dialogViewModel: DialogViewModel = hiltViewModel()
) {
    val dialogManager = dialogManagerViewModel.dialogManager
    // 订阅调色板、当前曲目与播放状态
    val currentMusic by playControlViewModel.currentPlayingMusic.collectAsState()
    val paletteColors by playControlViewModel.paletteColors.collectAsState()
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    val currentPosition by playControlViewModel.currentPosition.collectAsState()
    val duration by playControlViewModel.duration.collectAsState()

    // 根据customMode确定主题模式
    val customMode by settingsViewModel.customMode.collectAsState("default")
    val backgroundStyleString by settingsViewModel.backgroundStyle.collectAsState("FLUID")
    val hazeMode by settingsViewModel.hazeMode.collectAsState(DEFAULT_HAZE_MODE)
    val hazeMaterialPreset by settingsViewModel.hazeMaterialPreset.collectAsState(DEFAULT_HAZE_MATERIAL_PRESET)
    val hazeBlurRadius by settingsViewModel.hazeBlurRadius.collectAsState(DEFAULT_HAZE_BLUR_RADIUS)
    val hazeNoiseFactor by settingsViewModel.hazeNoiseFactor.collectAsState(DEFAULT_HAZE_NOISE_FACTOR)
    val hazeTintAlpha by settingsViewModel.hazeTintAlpha.collectAsState(DEFAULT_HAZE_TINT_ALPHA)
    val hazeIntensity by settingsViewModel.hazeIntensity.collectAsState(DEFAULT_HAZE_INTENSITY)
    val hazeRenderSettings = HazeRenderSettings(
        mode = hazeMode,
        preset = hazeMaterialPreset,
        intensity = hazeIntensity,
        blurRadius = hazeBlurRadius,
        noiseFactor = hazeNoiseFactor,
        tintAlpha = hazeTintAlpha
    )
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

    // DialogHost状态
    val dialogEvent by dialogManager.dialogEvent.collectAsState(null)
    val hazeState = dev.chrisbanes.haze.rememberHazeState()
    val messageToShowState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<DialogEvent.Message?>(null) }

    androidx.compose.runtime.LaunchedEffect(dialogEvent) {
        if (dialogEvent is DialogEvent.Message) {
            // 直接替换为新消息，不等待旧消息处理完毕
            messageToShowState.value = dialogEvent as DialogEvent.Message
        }
    }

    val defaultScreen = Routes.Tabs
    val navController = rememberNavBackStack(defaultScreen)

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
        ProvideHazeRenderSettings(
            settings = hazeRenderSettings
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            ) {
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
                    val contentModifier = Modifier
                        .padding(it)
                        .statusBarsPadding()
                    Box(
                        modifier = contentModifier
                    ) {
                        NavDisplay(
                            backStack = navController,
                            onBack = { navController.removeLastOrNull() },
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                fadeIn(
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                ) + scaleIn(
                                    initialScale = 0.95f,
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                ) togetherWith fadeOut(
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                ) + scaleOut(
                                    targetScale = 1.05f,
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                )
                            },
                            popTransitionSpec = {
                                fadeIn(
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                ) + scaleIn(
                                    initialScale = 0.95f,
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                ) togetherWith fadeOut(
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                ) + scaleOut(
                                    targetScale = 1.05f,
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                )
                            },
                            predictivePopTransitionSpec = {
                                fadeIn(
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                ) + scaleIn(
                                    initialScale = 0.95f,
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                ) togetherWith fadeOut(
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                ) + scaleOut(
                                    targetScale = 1.05f,
                                    animationSpec = tween(
                                        durationMillis = AnimationConfig.TRANSITION,
                                        easing = AnimationConfig.EASE_IN_OUT
                                    )
                                )
                            },
                            entryProvider = entryProvider {
                            entry<Routes.Tabs> {
                                TabsHost(
                                    navController = navController,
                                    pagerState = pagerState,
                                    tabHeader = {},
                                    recommendationViewModel = recommendationViewModel,
                                    settingsViewModel = settingsViewModel,
                                )
                            }
                            entry<Routes.SongDetail> { route ->
                                SongDetailScreen(
                                    navController = navController,
                                    musicId = route.musicId,
                                )
                            }
                            entry<Routes.Player>(
                                metadata = metadata {
                                    put(NavDisplay.TransitionKey) {
                                        (
                                            fadeIn(
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_OUT
                                                )
                                            ) + scaleIn(
                                                initialScale = 0.72f,
                                                transformOrigin = TransformOrigin(0.5f, 1f),
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_OUT
                                                )
                                            ) + slideInVertically(
                                                initialOffsetY = { it / 3 },
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_OUT
                                                )
                                            )
                                        ) togetherWith (
                                            fadeOut(
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_IN
                                                )
                                            ) + scaleOut(
                                                targetScale = 0.96f,
                                                transformOrigin = TransformOrigin(0.5f, 1f),
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_IN
                                                )
                                            )
                                        )
                                    }
                                    put(NavDisplay.PopTransitionKey) {
                                        (
                                            fadeIn(
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_OUT
                                                )
                                            ) + scaleIn(
                                                initialScale = 0.96f,
                                                transformOrigin = TransformOrigin(0.5f, 1f),
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_OUT
                                                )
                                            )
                                        ) togetherWith (
                                            fadeOut(
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_IN
                                                )
                                            ) + scaleOut(
                                                targetScale = 0.72f,
                                                transformOrigin = TransformOrigin(0.5f, 1f),
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_IN
                                                )
                                            ) + slideOutVertically(
                                                targetOffsetY = { it / 3 },
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_IN
                                                )
                                            )
                                        )
                                    }
                                    put(NavDisplay.PredictivePopTransitionKey) {
                                        (
                                            fadeIn(
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_OUT
                                                )
                                            ) + scaleIn(
                                                initialScale = 0.96f,
                                                transformOrigin = TransformOrigin(0.5f, 1f),
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_OUT
                                                )
                                            )
                                        ) togetherWith (
                                            fadeOut(
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_IN
                                                )
                                            ) + scaleOut(
                                                targetScale = 0.72f,
                                                transformOrigin = TransformOrigin(0.5f, 1f),
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_IN
                                                )
                                            ) + slideOutVertically(
                                                targetOffsetY = { it / 3 },
                                                animationSpec = tween(
                                                    durationMillis = AnimationConfig.TRANSITION,
                                                    easing = AnimationConfig.EASE_IN
                                                )
                                            )
                                        )
                                    }
                                }
                            ) {
                                PlayerScreen(navController = navController)
                            }
                            entry<Routes.Setting> {
                                SettingScreen(navController)
                            }
                            entry<Routes.ProfileSettings> {
                                ProfileSettingsScreen(navController = navController)
                            }
                            entry<Routes.BackupSettings> {
                                BackupSettingsScreen(navController = navController)
                            }
                            entry<Routes.LibrarySettings> {
                                LibrarySettingsScreen(navController = navController)
                            }
                            entry<Routes.Search> {
                                SearchScreen(
                                    navController = navController,
                                    dialogViewModel = dialogViewModel
                                )
                            }
                            entry<Routes.Playlist> { route ->
                                PlaylistScreen(
                                    navController = navController,
                                    playlistName = route.name,
                                    dialogViewModel = dialogViewModel,
                                )
                            }
                            entry<Routes.CustomPlaylist> { route ->
                                PlaylistScreen(
                                    navController = navController,
                                    playlistId = route.playlistId,
                                    dialogViewModel = dialogViewModel
                                )
                            }
                            entry<Routes.UserPlaylistManage> {
                                PlaylistManageScreen(navController = navController)
                            }
                            entry<Routes.Artist> { route ->
                                ArtistScreen(
                                    navController = navController,
                                    artistName = route.name,
                                    dialogViewModel = dialogViewModel
                                )
                            }
                            entry<Routes.AudioEffects> {
                                AudioEffectsScreen(navController = navController)
                            }
                            entry<Routes.AI> {
                                AIScreen(
                                    settingsViewModel,
                                    recommendationViewModel,
                                    libraryViewModel,
                                    dialogManager,
                                    navController
                                )
                            }
                            entry<Routes.Custom> {
                                CustomScreen(settingsViewModel, navController)
                            }
                            entry<Routes.Lyrics> {
                                LyricsScreen()
                            }
                            entry<Routes.UserUsageData> {
                                UserUsageDataScreen(navController = navController)
                            }
                            }
                        )
                        
                        // 在导航宿主之上显示固定的 TabPageIndicator
                        val isInTabs = navController.size == 1 && navController.lastOrNull() is Routes.Tabs
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
                }
            }

            // 使用 AnimatedVisibility 包裹 MiniPlayerBar 实现滑入滑出动画
            val isMiniPlayerVisible by playControlViewModel.isMiniPlayerVisible.collectAsState()
            AnimatedVisibility(
                visible = navController.none { it is Routes.Player } && isMiniPlayerVisible,
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
                        hazeState = hazeState,
                        onPlayPause = {
                            if (isPlaying) {
                                playControlViewModel.pauseMusic()
                            } else {
                                playControlViewModel.playOrResume()
                            }
                        },
                        onNext = { playControlViewModel.playNext() },
                        onPrev = { playControlViewModel.playPrevious() },
                        onOpenPlayer = { navController.add(Routes.Player) }
                    )
                }
            }

            val activeDialogState by dialogViewModel.activeDialog.collectAsState()
            when (val state = activeDialogState) {
                is DialogViewModel.DialogUiState.MusicDetail -> {
                    MusicDetailDialog(
                        dialogViewModel = dialogViewModel,
                        onDismiss = {
                            dialogViewModel.dismissMusicDetailDialog()
                        },
                        navController = navController,
                        hazeState = hazeState,
                        hazeRenderSettings = hazeRenderSettings
                    )
                }
                is DialogViewModel.DialogUiState.CreatePlaylist -> {
                    CreatePlaylistDialog(
                        dialogViewModel = dialogViewModel,
                        hazeState = hazeState,
                        hazeRenderSettings = hazeRenderSettings
                    )
                }
                is DialogViewModel.DialogUiState.MusicPicker -> {
                    MusicPickerDialog(
                        allMusic = state.state.allMusic,
                        selectedIds = state.state.selectedIds,
                        title = state.state.title,
                        onConfirm = dialogViewModel::confirmMusicPickerDialog,
                        onDismiss = dialogViewModel::dismissMusicPickerDialog,
                        hazeState = hazeState,
                        hazeRenderSettings = hazeRenderSettings
                    )
                }
                null -> Unit
            }

            messageToShowState.value?.let { message ->
                MessageToast(
                    message = message.message,
                    duration = message.duration,
                    id = message.id,
                    hazeState = hazeState,
                    hazeRenderSettings = hazeRenderSettings,
                    onDismiss = { messageToShowState.value = null }
                )
            }
        }
    }
}
}


