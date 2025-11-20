package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hearablemusicplayer.ui.components.CustomBottomNavBar
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import kotlin.math.abs

@Composable
fun MainScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val swipePages = listOf("home", "gallery", "list", "user")
    val currentIndex = swipePages.indexOf(currentRoute)

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
                        navController.navigate(targetRoute) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }

    Scaffold(
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .then(swipeModifier)
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
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
                composable("list") {
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


