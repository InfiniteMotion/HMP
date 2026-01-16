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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.hearablemusicplayer.ui.components.CustomBottomNavBar
import com.example.hearablemusicplayer.ui.components.DynamicBackground
import com.example.hearablemusicplayer.ui.theme.generateDynamicColorScheme
import com.example.hearablemusicplayer.ui.theme.getPresetColorScheme
import com.example.hearablemusicplayer.ui.util.AnimationConfig
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel
import kotlin.math.abs

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(UnstableApi::class)
@Composable
fun MainScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    recommendationViewModel: RecommendationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel()
) {
    val haptic = rememberHapticFeedback()

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

    val defaultScreen = Routes.Home
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    val swipePages = listOf(Routes.Home, Routes.Gallery, Routes.List, Routes.User)
    val currentIndex = swipePages.indexOfFirst { route ->
        navBackStackEntry?.destination?.hasRoute(route::class) == true
    }
    val currentRoute = swipePages.getOrNull(currentIndex) ?: Routes.Home

    // 只在 swipePages 页启用手势
    val enableSwipe = currentIndex != -1

    val swipeModifier = Modifier.pointerInput(enableSwipe, currentIndex) {
        if (!enableSwipe) return@pointerInput
        detectHorizontalDragGestures { _, dragAmount ->
            if (abs(dragAmount) > 50f) {
                val targetIndex = if (dragAmount > 0) currentIndex - 1 else currentIndex + 1
                if (targetIndex in swipePages.indices) {
                    val targetRoute = swipePages[targetIndex]
                    if (targetIndex != currentIndex) {
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
                val contentModifier = if (navBackStackEntry?.destination?.hasRoute<Routes.Player>() == true) {
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
                        startDestination = defaultScreen,
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

                        composable<Routes.Home>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            HomeScreen(
                                navController = navController,
                                recommendationViewModel = recommendationViewModel
                            )
                        }
                        composable<Routes.SongDetail>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            SongDetailScreen(
                                navController = navController
                            )
                        }
                        composable<Routes.Gallery>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            GalleryScreen(navController = navController)
                        }
                        composable<Routes.Player>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            PlayerScreen(navController = navController)
                        }
                        composable<Routes.List>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            ListScreen(navController = navController)
                        }
                        composable<Routes.User>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            UserScreen(settingsViewModel, recommendationViewModel, navController)
                        }
                        composable<Routes.Setting>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            SettingScreen(settingsViewModel, libraryViewModel, navController)
                        }
                        composable<Routes.Search>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            SearchScreen(navController = navController)
                        }
                        composable<Routes.Playlist>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            PlaylistScreen(navController = navController)
                        }
                        composable<Routes.Artist>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            ArtistScreen(navController = navController)
                        }
                        composable<Routes.AudioEffects>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            AudioEffectsScreen(navController = navController)
                        }
                        composable<Routes.AI>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            AIScreen(settingsViewModel, recommendationViewModel, libraryViewModel, navController)
                        }
                        composable<Routes.Custom>(
                            enterTransition = { pageEnterTransition },
                            exitTransition = { pageExitTransition }
                        ) {
                            CustomScreen(settingsViewModel, navController)
                        }
                    }
                }
                if (navBackStackEntry?.destination?.hasRoute<Routes.Player>() == false) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .background(if(isPlaying) Transparent else MaterialTheme.colorScheme.surface)
                    ) {
                        CustomBottomNavBar(
                            isPlaying = isPlaying,
                            currentRoute = currentRoute,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}


