package com.hearablemusic.player.ui.common.pages

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.hearablemusicplayer.ui.player.floating.FloatingLyricsService
import com.hearablemusic.player.ui.common.components.BottomFusionBar
import com.hearablemusic.player.ui.common.components.FusionSidebar
import com.hearablemusic.player.ui.common.components.TabPageIndicator
import com.hearablemusic.player.ui.common.design.animation.AnimationTokens
import com.hearablemusic.player.ui.common.design.dimens.LocalHMPDimens
import com.hearablemusic.player.ui.common.design.dimens.rememberHMPDimens
import com.hearablemusic.player.ui.common.design.theme.ThemeExtensionManager
import com.hearablemusic.player.ui.common.dialogs.CreatePlaylistDialog
import com.hearablemusic.player.ui.common.dialogs.MusicDetailDialog
import com.hearablemusic.player.ui.common.dialogs.MusicPickerDialog
import com.hearablemusic.player.ui.common.dialogs.PlaylistPickerDialog
import com.hearablemusic.player.ui.common.dialogs.TimerDialog
import com.hearablemusic.player.ui.common.dialogs.base.MessageToast
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogEvent
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogViewModel
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.common.layout.rememberAppWindowSizeInfo
import com.hearablemusic.player.ui.common.navigation.DeepLinkHandler
import com.hearablemusic.player.ui.common.navigation.navigationGraph
import com.hearablemusic.player.ui.common.navigation.rememberRouter
import com.hearablemusic.player.ui.common.pages.base.BackgroundStyle
import com.hearablemusic.player.ui.common.pages.base.DynamicBackground
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_BLUR_RADIUS
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_INTENSITY
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_MATERIAL_PRESET
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_MODE
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_NOISE_FACTOR
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_TINT_ALPHA
import com.hearablemusic.player.ui.common.util.HazeRenderSettings
import com.hearablemusic.player.ui.common.util.ProvideHazeRenderSettings
import com.hearablemusic.player.ui.common.viewmodel.ThemeViewModel
import com.hearablemusic.player.ui.library.viewmodel.LibraryViewModel
import com.hearablemusic.player.ui.player.viewmodel.PlaybackViewModel
import com.hearablemusic.player.ui.player.viewmodel.PlaylistQueueViewModel
import com.hearablemusic.player.ui.playlist.viewmodel.PlaylistViewModel
import com.hearablemusic.player.ui.settings.viewmodel.RecommendationViewModel
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.hmp.domain.setting.usecase.LyricsSettingsUseCase

import com.hearablemusic.player.ui.common.navigation.Routes as NavRoutes

@SuppressLint("ContextCastToActivity")
@OptIn(UnstableApi::class)
@Composable
fun MainScreen(
    libraryViewModel: LibraryViewModel = koinViewModel(),
    recommendationViewModel: RecommendationViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    playbackViewModel: PlaybackViewModel = koinViewModel(),
    playlistQueueViewModel: PlaylistQueueViewModel = koinViewModel(),
    playlistViewModel: PlaylistViewModel = koinViewModel(),
    themeViewModel: ThemeViewModel = koinViewModel(),
    dialogManagerViewModel: DialogManagerViewModel = koinViewModel(),
    dialogViewModel: DialogViewModel = koinViewModel()
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
    val activity = LocalContext.current as ComponentActivity

    // 启动时按配置启动悬浮歌词
    val lyricsSettingsUseCase: LyricsSettingsUseCase = koinInject()
    LaunchedEffect(Unit) {
        try {
            if (lyricsSettingsUseCase.floatingLyricsEnabled.first() && android.provider.Settings.canDrawOverlays(activity)) {
                activity.startService(Intent(activity, FloatingLyricsService::class.java))
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(dialogEvent) {
        when (dialogEvent) {
            is DialogEvent.Message -> {
                messageToShowState.value = dialogEvent as DialogEvent.Message
            }
            is DialogEvent.DismissTimerDialog -> {
                dialogViewModel.dismissTimerDialog()
            }
            is DialogEvent.ShareMusic -> {
                val shareEvent = dialogEvent as DialogEvent.ShareMusic
                val file = java.io.File(shareEvent.filePath)
                if (file.exists()) {
                    val fileUri: Uri = FileProvider.getUriForFile(
                        activity,
                        "${activity.packageName}.fileprovider",
                        file
                    )
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        putExtra(Intent.EXTRA_SUBJECT, shareEvent.title)
                        putExtra(Intent.EXTRA_TEXT, "${shareEvent.title} - ${shareEvent.artist}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(shareIntent, "分享音乐")
                    activity.startActivity(chooser)
                } else {
                    // 文件不存在，退化为分享文本
                    val shareText = "${shareEvent.title} - ${shareEvent.artist} (${shareEvent.album})"
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        putExtra(Intent.EXTRA_SUBJECT, shareEvent.title)
                    }
                    val chooser = Intent.createChooser(shareIntent, "分享音乐")
                    activity.startActivity(chooser)
                }
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

    val windowSizeInfo = rememberAppWindowSizeInfo()
    LaunchedEffect(windowSizeInfo) {
        Log.d("AdaptiveLayout", "widthClass=${windowSizeInfo.widthSizeClass}, useFusionSidebar=${windowSizeInfo.useFusionSidebar}")
    }

    // 应用主题(根据播放状态切换)
    MaterialTheme(
        colorScheme = colorScheme
    ) {
        ProvideHazeRenderSettings(
            settings = hazeRenderSettings
        ) {
            CompositionLocalProvider(
                LocalWindowSizeInfo provides windowSizeInfo
            ) {
                val dimens = rememberHMPDimens()
                CompositionLocalProvider(
                    LocalHMPDimens provides dimens
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

                    val coroutineScope = rememberCoroutineScope()

                    val navContent: @Composable () -> Unit = {
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

                        // TabPageIndicator
                        val isInTabs = navController.size == 1 && navController.lastOrNull() is NavRoutes.Main.Tabs
                        AnimatedVisibility(
                            visible = isInTabs && !windowSizeInfo.useFusionSidebar && !windowSizeInfo.isLandscape,
                            enter = fadeIn(
                                animationSpec = tween(300, easing = AnimationTokens.EASE_OUT)
                            ) + scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(300, easing = AnimationTokens.EASE_OUT)
                            ) + slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(300, easing = AnimationTokens.EASE_OUT)
                            ),
                            exit = fadeOut(
                                animationSpec = tween(250, easing = AnimationTokens.EASE_IN)
                            ) + scaleOut(
                                targetScale = 0.9f,
                                animationSpec = tween(250, easing = AnimationTokens.EASE_IN)
                            ) + slideOutVertically(
                                targetOffsetY = { -it / 2 },
                                animationSpec = tween(250, easing = AnimationTokens.EASE_IN)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                tabHeader()
                            }
                        }
                    }
                    val isOnTabPage = navController.size == 1 && navController.lastOrNull() is NavRoutes.Main.Tabs

                    if (windowSizeInfo.useFusionSidebar) {
                        // Medium 手机横屏：左侧融合侧边栏 + 右侧内容（侧边栏仅在Tab页面显示）
                        Row(modifier = contentModifier.displayCutoutPadding()) {
                            if (isOnTabPage) {
                                FusionSidebar(
                                    selectedTabIndex = pagerState.currentPage,
                                    currentMusic = currentMusic,
                                    isPlaying = isPlaying,
                                    onTabSelected = { index ->
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    onOpenPlayer = { navController.add(NavRoutes.Player.Player) }
                                )
                            }
                            Box(Modifier.weight(1f).fillMaxSize()) {
                                navContent()
                            }
                        }
                    } else {
                        // Compact / Expanded：单列布局
                        Box(modifier = contentModifier.then(
                            if (windowSizeInfo.isLandscape) Modifier.displayCutoutPadding() else Modifier
                        )) {
                            navContent()
                        }
                    }
                }
            }

            // BottomFusionBar 底部融合栏（导航Tab + 播放控制）
            // 非 Medium 模式始终渲染，Medium 模式仅在子页面渲染（Tab 页面由 FusionSidebar 处理）
            val bfbIsOnTabPage = navController.size == 1 && navController.lastOrNull() is NavRoutes.Main.Tabs
            if (!windowSizeInfo.useFusionSidebar || !bfbIsOnTabPage) {
                val isMiniPlayerVisible by playbackViewModel.isMiniPlayerVisible.collectAsState()
                val bfbScope = rememberCoroutineScope()
                AnimatedVisibility(
                    visible = navController.none { it is NavRoutes.Player.Player || it is NavRoutes.Player.Lyrics } && isMiniPlayerVisible,
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
                    Box {
                        BottomFusionBar(
                            musicInfo = currentMusic,
                            isPlaying = isPlaying,
                            progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                            selectedTabIndex = pagerState.currentPage,
                            onTabSelected = { index ->
                                bfbScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            hazeState = hazeState,
                            showNavText = windowSizeInfo.isLandscape,
                            showNavCapsule = bfbIsOnTabPage,
                            maxWidth = when (windowSizeInfo.widthSizeClass) {
                                com.hearablemusic.player.ui.common.layout.WindowWidthSizeClass.Compact -> 480.dp
                                com.hearablemusic.player.ui.common.layout.WindowWidthSizeClass.Medium -> 640.dp
                                com.hearablemusic.player.ui.common.layout.WindowWidthSizeClass.Expanded -> null
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
                            onOpenPlayer = { navController.add(NavRoutes.Player.Player) }
                        )
                    }
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
                } // CompositionLocalProvider HMPDimens
            } // CompositionLocalProvider
        }
    }
}


