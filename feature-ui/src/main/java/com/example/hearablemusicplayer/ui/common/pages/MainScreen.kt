package com.example.hearablemusicplayer.ui.common.pages

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.hearablemusicplayer.ui.common.components.TabPageIndicator
import com.example.hearablemusicplayer.ui.player.components.MiniPlayerBar
import com.example.hearablemusicplayer.ui.common.dialogs.CreatePlaylistDialog
import com.example.hearablemusicplayer.ui.common.dialogs.base.MessageToast
import com.example.hearablemusicplayer.ui.common.dialogs.MusicDetailDialog
import com.example.hearablemusicplayer.ui.common.dialogs.MusicPickerDialog
import com.example.hearablemusicplayer.ui.common.dialogs.PlaylistPickerDialog
import com.example.hearablemusicplayer.ui.common.dialogs.TimerDialog
import com.example.hearablemusicplayer.ui.common.navigation.DeepLinkHandler
import com.example.hearablemusicplayer.ui.common.navigation.navigationGraph
import com.example.hearablemusicplayer.ui.common.navigation.rememberRouter
import com.example.hearablemusicplayer.ui.common.design.animation.AnimationTokens
import com.example.hearablemusicplayer.ui.common.design.theme.ThemeExtensionManager
import com.example.hearablemusicplayer.ui.common.util.DEFAULT_HAZE_BLUR_RADIUS
import com.example.hearablemusicplayer.ui.common.util.DEFAULT_HAZE_INTENSITY
import com.example.hearablemusicplayer.ui.common.util.DEFAULT_HAZE_MATERIAL_PRESET
import com.example.hearablemusicplayer.ui.common.util.DEFAULT_HAZE_MODE
import com.example.hearablemusicplayer.ui.common.util.DEFAULT_HAZE_NOISE_FACTOR
import com.example.hearablemusicplayer.ui.common.util.DEFAULT_HAZE_TINT_ALPHA
import com.example.hearablemusicplayer.ui.common.dialogs.viewmodel.DialogEvent
import com.example.hearablemusicplayer.ui.common.util.HazeRenderSettings
import com.example.hearablemusicplayer.ui.common.util.ProvideHazeRenderSettings
import com.example.hearablemusicplayer.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.example.hearablemusicplayer.ui.common.dialogs.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.common.pages.base.BackgroundStyle
import com.example.hearablemusicplayer.ui.common.pages.base.DynamicBackground
import com.example.hearablemusicplayer.ui.library.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.player.viewmodel.PlaybackViewModel
import com.example.hearablemusicplayer.ui.player.viewmodel.PlaylistQueueViewModel
import com.example.hearablemusicplayer.ui.playlist.viewmodel.PlaylistViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.SettingsViewModel
import com.example.hearablemusicplayer.ui.common.viewmodel.ThemeViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import com.example.hearablemusicplayer.ui.common.navigation.Routes as NavRoutes

@SuppressLint("ContextCastToActivity")
@OptIn(UnstableApi::class)
@Composable
fun MainScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    recommendationViewModel: RecommendationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    playbackViewModel: PlaybackViewModel = hiltViewModel(),
    playlistQueueViewModel: PlaylistQueueViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    dialogManagerViewModel: DialogManagerViewModel = hiltViewModel(),
    dialogViewModel: DialogViewModel = hiltViewModel()
) {
    val dialogManager = dialogManagerViewModel.dialogManager
    // 订阅调色板、当前曲目与播放状态
    val currentMusic by playlistQueueViewModel.currentPlayingMusic.collectAsState()
    val paletteColors by themeViewModel.paletteColors.collectAsState()
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    val currentPosition by playbackViewModel.currentPosition.collectAsState()
    val duration by playbackViewModel.duration.collectAsState()

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
        ThemeExtensionManager.generateDynamicColorScheme(paletteColors, isDarkTheme)
    } else {
        ThemeExtensionManager.getColorScheme(isDarkTheme)
    }

    // DialogHost状态
    val dialogEvent by dialogManager.dialogEvent.collectAsState(null)
    val hazeState = rememberHazeState()
    val messageToShowState = remember { mutableStateOf<DialogEvent.Message?>(null) }

    LaunchedEffect(dialogEvent) {
        when (dialogEvent) {
            is DialogEvent.Message -> {
                messageToShowState.value = dialogEvent as DialogEvent.Message
            }
            is DialogEvent.DismissTimerDialog -> {
                dialogViewModel.dismissTimerDialog()
            }
            else -> {
                // 无操作
            }
        }
    }

    val defaultScreen = NavRoutes.Main.Tabs
    val navController = rememberNavBackStack(defaultScreen)
    val router = rememberRouter(navController)
    val deepLinkHandler = remember { DeepLinkHandler(router) }
    val activity = LocalContext.current as ComponentActivity
    LaunchedEffect(activity.intent) {
        activity.intent?.data?.let { uri ->
            deepLinkHandler.handleDeepLink(uri)
        }
    }

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
                        animationSpec = tween(durationMillis = 800, easing = AnimationTokens.EASE_IN_OUT)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = 800, easing = AnimationTokens.EASE_IN_OUT)
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
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                ) + scaleIn(
                                    initialScale = 0.95f,
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                ) togetherWith fadeOut(
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                ) + scaleOut(
                                    targetScale = 1.05f,
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                )
                            },
                            popTransitionSpec = {
                                fadeIn(
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                ) + scaleIn(
                                    initialScale = 0.95f,
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                ) togetherWith fadeOut(
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                ) + scaleOut(
                                    targetScale = 1.05f,
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                )
                            },
                            predictivePopTransitionSpec = {
                                fadeIn(
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                ) + scaleIn(
                                    initialScale = 0.95f,
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                ) togetherWith fadeOut(
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                ) + scaleOut(
                                    targetScale = 1.05f,
                                    animationSpec = tween(
                                        durationMillis = AnimationTokens.TRANSITION,
                                        easing = AnimationTokens.EASE_IN_OUT
                                    )
                                )
                            },
                            entryProvider = navigationGraph(
                                navController = navController,
                                pagerState = pagerState,
                                libraryViewModel = libraryViewModel,
                                recommendationViewModel = recommendationViewModel,
                                settingsViewModel = settingsViewModel,
                                playbackViewModel = playbackViewModel,
                                playlistQueueViewModel = playlistQueueViewModel,
                                playlistViewModel = playlistViewModel,
                                themeViewModel = themeViewModel,
                                dialogManagerViewModel = dialogManagerViewModel,
                                dialogViewModel = dialogViewModel,
                                tabHeader = tabHeader
                            )
                        )
                        
                        // 在导航宿主之上显示固定的 TabPageIndicator
                        val isInTabs = navController.size == 1 && navController.lastOrNull() is NavRoutes.Main.Tabs
                        AnimatedVisibility(
                            visible = isInTabs,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = AnimationTokens.EASE_OUT
                                )
                            ) + scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = AnimationTokens.EASE_OUT
                                )
                            ) + slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = AnimationTokens.EASE_OUT
                                )
                            ),
                            exit = fadeOut(
                                animationSpec = tween(
                                    durationMillis = 250,
                                    easing = AnimationTokens.EASE_IN
                                )
                            ) + scaleOut(
                                targetScale = 0.9f,
                                animationSpec = tween(
                                    durationMillis = 250,
                                    easing = AnimationTokens.EASE_IN
                                )
                            ) + slideOutVertically(
                                targetOffsetY = { -it / 2 },
                                animationSpec = tween(
                                    durationMillis = 250,
                                    easing = AnimationTokens.EASE_IN
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
            val isMiniPlayerVisible by playbackViewModel.isMiniPlayerVisible.collectAsState()
            AnimatedVisibility(
                visible = navController.none { it is NavRoutes.Player.Player } && isMiniPlayerVisible,
                enter = slideInVertically(
                    initialOffsetY = { it }, // 从底部滑入 (偏移量为自身高度)
                    animationSpec = tween(durationMillis = AnimationTokens.TRANSITION, easing = AnimationTokens.EASE_IN_OUT)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it }, // 向底部滑出 (偏移量为自身高度)
                    animationSpec = tween(durationMillis = AnimationTokens.TRANSITION, easing = AnimationTokens.EASE_IN_OUT)
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
                                playbackViewModel.pauseMusic()
                            } else {
                                playbackViewModel.playOrResume()
                            }
                        },
                        onNext = { playbackViewModel.playNext() },
                        onPrev = { playbackViewModel.playPrevious() },
                        onOpenPlayer = { navController.add(NavRoutes.Player.Player) }
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
                        router = router,
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
                is DialogViewModel.DialogUiState.PlaylistPicker -> {
                    PlaylistPickerDialog(
                        playlists = state.state.playlists,
                        title = state.state.title,
                        onDismiss = dialogViewModel::dismissPlaylistPickerDialog,
                        onSelectPlaylist = dialogViewModel::confirmPlaylistPickerDialog,
                        hazeState = hazeState,
                        hazeRenderSettings = hazeRenderSettings
                    )
                }
                is DialogViewModel.DialogUiState.Timer -> {
                    TimerDialog(
                        onDismiss = {
                            state.state.onDismiss()
                            dialogViewModel.dismissTimerDialog()
                        },
                        onConfirm = { minutes: Int ->
                            state.state.onConfirm(minutes)
                            dialogViewModel.dismissTimerDialog()
                        },
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


