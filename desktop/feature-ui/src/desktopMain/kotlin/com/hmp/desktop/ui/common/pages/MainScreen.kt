package com.hmp.desktop.ui.common.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

import com.hmp.desktop.ui.common.components.BottomFusionBar
import com.hmp.desktop.ui.common.components.TITLE_BAR_HEIGHT
import com.hmp.desktop.ui.common.pages.TabsHost
import com.hmp.desktop.ui.common.layout.WindowSizeInfo
import com.hmp.desktop.ui.common.layout.WindowWidthSizeClass
import com.hmp.desktop.ui.common.layout.heightSizeClass
import com.hmp.desktop.ui.common.layout.widthSizeClass
import kotlinx.coroutines.launch
import com.hmp.desktop.ui.common.dialogs.CreatePlaylistDialog
import com.hmp.desktop.ui.common.dialogs.base.MessageToast
import com.hmp.desktop.ui.common.dialogs.MusicDetailDialog
import com.hmp.desktop.ui.common.dialogs.MusicPickerDialog
import com.hmp.desktop.ui.common.dialogs.PlaylistPickerDialog
import com.hmp.desktop.ui.common.dialogs.TimerDialog
import com.hmp.desktop.ui.common.navigation.DeepLinkHandler
import com.hmp.desktop.ui.common.navigation.NavHost
import com.hmp.desktop.ui.common.navigation.Routes
import com.hmp.desktop.ui.common.navigation.navigationGraph
import com.hmp.desktop.ui.common.navigation.rememberNavController
import com.hmp.desktop.ui.common.design.animation.AnimationTokens
import com.hmp.desktop.ui.common.design.theme.ThemeExtensionManager
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_BLUR_RADIUS
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_INTENSITY
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_MATERIAL_PRESET
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_MODE
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_NOISE_FACTOR
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_TINT_ALPHA
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogEvent
import com.hmp.desktop.ui.common.util.HazeRenderSettings
import com.hmp.desktop.ui.common.util.ProvideHazeRenderSettings
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.common.pages.base.BackgroundStyle
import com.hmp.desktop.ui.common.pages.base.DynamicBackground
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.settings.viewmodel.RecommendationViewModel
import com.hmp.desktop.ui.settings.viewmodel.SettingsViewModel
import com.hmp.desktop.ui.common.viewmodel.ThemeViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun MainScreen(
    recommendationViewModel: RecommendationViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject(),
    playbackViewModel: PlaybackViewModel = koinInject(),
    playlistQueueViewModel: PlaylistQueueViewModel = koinInject(),
    themeViewModel: ThemeViewModel = koinInject(),
    dialogManagerViewModel: DialogManagerViewModel = koinInject(),
    dialogViewModel: DialogViewModel = koinInject(),
    onBackHandlerReady: ((() -> Unit) -> Unit)? = null,
    systemIsDark: Boolean = isSystemInDarkTheme()
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
        else -> systemIsDark
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
            is DialogEvent.ShareMusic -> {
                // Desktop: sharing via Intent is not available; show info message instead
                val shareEvent = dialogEvent as DialogEvent.ShareMusic
                messageToShowState.value = DialogEvent.Message(
                    message = "${shareEvent.title} - ${shareEvent.artist}"
                )
            }
            else -> {
                // 无操作
            }
        }
    }

    val navController = rememberNavController(Routes.Main.Tabs)
    val deepLinkHandler = remember { DeepLinkHandler(navController) }

    // 注册 Escape 键返回处理
    LaunchedEffect(navController) {
        onBackHandlerReady?.invoke {
            if (navController.canPop()) {
                navController.popBackStack()
            }
        }
    }

    // 窗口尺寸检测
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val windowWidthDp = with(density) { windowInfo.containerSize.width.toDp() }
    val windowHeightDp = with(density) { windowInfo.containerSize.height.toDp() }
    val windowSizeInfo = WindowSizeInfo(
        widthSizeClass = widthSizeClass(windowWidthDp),
        heightSizeClass = heightSizeClass(windowHeightDp),
        widthDp = windowWidthDp,
        heightDp = windowHeightDp
    )

    val tabCount = 4
    val savedTabIndex = rememberSaveable { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberTabsPagerState(
        pageCount = tabCount,
        initialPage = savedTabIndex.intValue
    )
    LaunchedEffect(pagerState.currentPage) {
        savedTabIndex.intValue = pagerState.currentPage
    }
    val tabHeader: @Composable () -> Unit = {}

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

                // 2. 全局动态背景层 (仅在播放时覆盖在静态背景之上)
                //    使用 animateFloatAsState 与标题栏背景共享完全相同的动画机制
                val dynamicBgAlpha by animateFloatAsState(
                    targetValue = if (isPlaying && currentMusic != null) 1f else 0f,
                    animationSpec = tween(durationMillis = 800, easing = AnimationTokens.EASE_IN_OUT)
                )
                if (dynamicBgAlpha > 0f) {
                    Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = dynamicBgAlpha)) {
                        DynamicBackground(
                            albumArtUri = currentMusic?.music?.albumArtUri,
                            paletteColors = paletteColors,
                            isDarkTheme = isDarkTheme,
                            style = backgroundStyle,
                            modifier = Modifier
                        )
                    }
                }

                // 前景页面内容
                Scaffold(
                    contentWindowInsets = WindowInsets(0),
                    containerColor = Transparent,
                    bottomBar = {}
                ) {
                    val contentModifier = Modifier
                        .padding(it)
                        .padding(top = TITLE_BAR_HEIGHT)

                    // 导航图定义（composable 上下文中创建一次）
                    val navEntryProvider = navigationGraph(
                        navController = navController,
                        pagerState = pagerState,
                        recommendationViewModel = recommendationViewModel,
                        settingsViewModel = settingsViewModel,
                        playbackViewModel = playbackViewModel,
                        playlistQueueViewModel = playlistQueueViewModel,
                        themeViewModel = themeViewModel,
                        dialogManagerViewModel = dialogManagerViewModel,
                        dialogViewModel = dialogViewModel,
                        tabHeader = tabHeader
                    )

                    if (windowSizeInfo.isExpanded) {
                        val isInTabs = navController.currentRoute is Routes.Main.Tabs
                        // 扩展布局：全宽内容 + 底部融合栏
                        if (isInTabs) {
                            Box(modifier = contentModifier.fillMaxSize()) {
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
                        } else {
                            // 子页面：直接渲染 NavHost
                            Box(modifier = contentModifier.fillMaxSize()) {
                                NavHost(
                                    navController = navController,
                                    entryProvider = navEntryProvider
                                )
                            }
                        }
                    } else {
                        // 紧凑/中等布局
                        Box(modifier = contentModifier) {
                            NavHost(
                                navController = navController,
                                entryProvider = navEntryProvider
                            )
                        }
                    }
                }
            }

            // 底部融合栏（导航 + 播放控制）— 所有布局模式共用
            val isMiniPlayerVisible by playbackViewModel.isMiniPlayerVisible.collectAsState()
            val isOnTabPage = navController.currentRoute is Routes.Main.Tabs
            AnimatedVisibility(
                visible = navController.none { it is Routes.Player.Player } && isMiniPlayerVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = AnimationTokens.TRANSITION, easing = AnimationTokens.EASE_IN_OUT)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = AnimationTokens.TRANSITION, easing = AnimationTokens.EASE_IN_OUT)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    BottomFusionBar(
                        musicInfo = currentMusic,
                        isPlaying = isPlaying,
                        progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                        selectedTabIndex = pagerState.currentPage,
                        onTabSelected = { index ->
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                            }
                        },
                        onPlayPause = {
                            if (isPlaying) {
                                playbackViewModel.pauseMusic()
                            } else {
                                playbackViewModel.playOrResume()
                            }
                        },
                        onNext = { playbackViewModel.playNext() },
                        onPrev = { playbackViewModel.playPrevious() },
                        onOpenPlayer = { navController.navigate(Routes.Player.Player) },
                        hazeState = hazeState,
                        showNavText = windowSizeInfo.isExpanded,
                        forceExpanded = windowSizeInfo.isExpanded,
                        showNavCapsule = isOnTabPage,
                        maxWidth = when (windowSizeInfo.widthSizeClass) {
                            WindowWidthSizeClass.Compact -> 480.dp
                            WindowWidthSizeClass.Medium -> 640.dp
                            WindowWidthSizeClass.Expanded -> null
                        }
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


