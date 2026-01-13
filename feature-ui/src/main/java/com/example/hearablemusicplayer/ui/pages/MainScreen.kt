package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hearablemusicplayer.ui.components.CustomBottomNavBar
import com.example.hearablemusicplayer.ui.components.DynamicBackground
import com.example.hearablemusicplayer.ui.theme.generateDynamicColorScheme
import com.example.hearablemusicplayer.ui.theme.getPresetColorScheme
import com.example.hearablemusicplayer.ui.util.AnimationConfig
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.dialogs.MusicScanDialog
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SearchViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel
import kotlin.math.abs

@OptIn(UnstableApi::class)
@Composable
fun MainScreen(
    libraryViewModel: LibraryViewModel,
    playlistViewModel: PlaylistViewModel,
    searchViewModel: SearchViewModel,
    recommendationViewModel: RecommendationViewModel,
    settingsViewModel: SettingsViewModel,
    playControlViewModel: PlayControlViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME
    val haptic = rememberHapticFeedback()

    val swipePages = listOf(Routes.HOME, Routes.GALLERY, Routes.LIST, Routes.USER)
    val currentIndex = swipePages.indexOf(currentRoute)

    // 订阅调色板、当前曲目与播放状态
    val currentMusic by playControlViewModel.currentPlayingMusic.collectAsState()
    val paletteColors by playControlViewModel.paletteColors.collectAsState()
    val isPlaying by playControlViewModel.isPlaying.collectAsState()

    // 根据customMode确定主题模式
    val customMode by settingsViewModel.customMode.collectAsState("default")
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

    // 只在 swipePages 页启用手势
    val enableSwipe = currentIndex != -1

    val swipeModifier = Modifier.pointerInput(enableSwipe, currentIndex) {
        if (!enableSwipe) return@pointerInput
        detectHorizontalDragGestures { _, dragAmount ->
            if (abs(dragAmount) > 50f) {
                val targetIndex = if (dragAmount > 0) currentIndex - 1 else currentIndex + 1
                if (targetIndex in swipePages.indices) {
                    val targetRoute = swipePages[targetIndex]
                    if (targetRoute != currentRoute) {
                        // 翻页时给予触觉反馈
                        haptic.performLightClick()
                        navController.navigate(route = targetRoute) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }

    // 应用主题(根据播放状态切换)
    MaterialTheme(
        colorScheme = colorScheme
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 全局动态背景层（仅在音乐播放时显示，带过渡动画）
            AnimatedVisibility(
                visible = isPlaying && currentMusic != null,
                enter = scaleIn(initialScale = 0.95f, animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)) +
                        fadeIn(animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)),
                exit = scaleOut(targetScale = 0.95f, animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)) +
                        fadeOut(animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT))
            ) {
                DynamicBackground(
                    albumArtUri = currentMusic?.music?.albumArtUri,
                    paletteColors = paletteColors,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                )
            }
            
            // 音乐未播放时显示纯色背景（带过渡动画）
            AnimatedVisibility(
                visible = !(isPlaying && currentMusic != null),
                enter = scaleIn(initialScale = 0.95f, animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)) +
                        fadeIn(animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)),
                exit = scaleOut(targetScale = 0.95f, animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)) +
                        fadeOut(animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            // 前景页面内容
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                containerColor = Transparent,
                bottomBar = {}
            ) {
                val contentModifier = if (currentRoute == "player") {
                    Modifier.padding(it)
                } else {
                    Modifier
                        .padding(it)
                        .statusBarsPadding()
                }
                Box(
                    modifier = contentModifier
                        .then(swipeModifier)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.GALLERY,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 为所有页面添加统一的过渡动画
                        val pageEnterTransition = scaleIn(
                            initialScale = 0.95f,
                            animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)
                        )

                        val pageExitTransition = scaleOut(
                            targetScale = 0.95f,
                            animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)
                        )

                        composable(route = Routes.HOME,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            HomeScreen(recommendationViewModel, playlistViewModel, playControlViewModel, navController)
                        }
                        composable(route = Routes.GALLERY,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            GalleryScreen(libraryViewModel, playControlViewModel, navController)
                        }
                        composable(route = Routes.PLAYER,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            PlayerScreen(playControlViewModel, playlistViewModel, navController)
                        }
                        composable(route = Routes.LIST,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            ListScreen(playlistViewModel, navController)
                        }
                        composable(route = Routes.USER,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            UserScreen(settingsViewModel, recommendationViewModel, navController)
                        }
                        composable(route = Routes.SETTING,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            SettingScreen(settingsViewModel, libraryViewModel, navController)
                        }
                        composable(route = Routes.SEARCH,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            SearchScreen(searchViewModel, playControlViewModel, navController)
                        }
                        composable(route = Routes.PLAYLIST,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            PlaylistScreen(playlistViewModel, playControlViewModel, navController)
                        }
                        composable(route = Routes.ARTIST,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            ArtistScreen(playlistViewModel, playControlViewModel, navController)
                        }
                        composable(route = Routes.AUDIO_EFFECTS,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            AudioEffectsScreen(playControlViewModel, navController)
                        }
                        composable(route = Routes.AI,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            AIScreen(settingsViewModel, recommendationViewModel, libraryViewModel, navController)
                        }
                        composable(route = Routes.CUSTOM,
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            CustomScreen(settingsViewModel, navController)
                        }
                    }
                }
                if (currentRoute != Routes.PLAYER) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .background(if(isPlaying) Transparent else MaterialTheme.colorScheme.surface)
                    ) {
                        CustomBottomNavBar(
                            isPlaying = isPlaying,
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                navController.navigate(route = route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


