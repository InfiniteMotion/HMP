package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hearablemusicplayer.ui.components.CustomBottomNavBar
import com.example.hearablemusicplayer.ui.components.DynamicBackground
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import kotlin.math.abs

@OptIn(UnstableApi::class)
@Composable
fun MainScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val haptic = rememberHapticFeedback()

    val swipePages = listOf("home", "gallery", "list", "user")
    val currentIndex = swipePages.indexOf(currentRoute)
    val dailyMusic by musicViewModel.dailyMusic.collectAsState(null)

    // 订阅调色板、当前曲目与播放状态
    val currentMusic by playControlViewModel.currentPlayingMusic.collectAsState()
    val paletteColors by playControlViewModel.paletteColors.collectAsState()
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

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
                        navController.navigate(targetRoute) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 全局动态背景层（仅在音乐播放时显示，带过渡动画）
        AnimatedVisibility(
            visible = isPlaying && currentMusic != null,
            enter = fadeIn(animationSpec = tween(durationMillis = 800)),
            exit = fadeOut(animationSpec = tween(durationMillis = 600))
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
            enter = fadeIn(animationSpec = tween(durationMillis = 800)),
            exit = fadeOut(animationSpec = tween(durationMillis = 600))
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
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                if (currentRoute != "player") {
                    CustomBottomNavBar(
                        playControlViewModel = playControlViewModel,
                        modifier = Modifier.navigationBarsPadding(),
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            val contentModifier = if (currentRoute == "player") {
                Modifier.padding(innerPadding)
            } else {
                Modifier
                    .padding(innerPadding)
                    .statusBarsPadding()
            }
            Box(
                modifier = contentModifier
                    .then(swipeModifier)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = if(dailyMusic != null) "home" else "gallery",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        HomeScreen(musicViewModel, playControlViewModel)
                    }
                    composable("gallery") {
                        GalleryScreen(musicViewModel, playControlViewModel, navController)
                    }
                    composable("player") {
                        PlayerScreen(playControlViewModel,navController)
                    }
                    composable( "list") {
                        ListScreen(musicViewModel, navController)
                    }
                    composable("user") {
                        UserScreen(musicViewModel, navController)
                    }
                    composable("setting") {
                        SettingScreen(musicViewModel, navController)
                    }
                    composable ("search") {
                        SearchScreen(musicViewModel,playControlViewModel,navController)
                    }
                    composable("playlist") {
                        PlaylistScreen(musicViewModel,playControlViewModel,navController)
                    }
                }
            }
        }
    }
}


