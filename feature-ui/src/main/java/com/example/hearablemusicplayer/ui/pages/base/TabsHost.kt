package com.example.hearablemusicplayer.ui.pages.base

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.hearablemusicplayer.ui.pages.GalleryScreen
import com.example.hearablemusicplayer.ui.pages.HomeScreen
import com.example.hearablemusicplayer.ui.pages.ListScreen
import com.example.hearablemusicplayer.ui.pages.UserScreen
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaybackViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistQueueViewModel
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel

@Composable
fun rememberTabsPagerState(
    pageCount: Int,
    initialPage: Int = 0
): PagerState = rememberPagerState(initialPage = initialPage) { pageCount }

@Composable
fun TabsHost(
    navController: NavBackStack<NavKey>,
    pagerState: PagerState,
    tabHeader: @Composable () -> Unit,
    recommendationViewModel: RecommendationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    playbackViewModel: PlaybackViewModel,
    playlistQueueViewModel: PlaylistQueueViewModel,
    dialogViewModel: DialogViewModel,
) {
    CompositionLocalProvider(
        LocalTabHeaderContent provides tabHeader
    ) {
        val haptic = rememberHapticFeedback()
        var previousPage by remember { mutableIntStateOf(pagerState.currentPage) }
        
        LaunchedEffect(pagerState.currentPage) {
            if (pagerState.currentPage != previousPage) {
                haptic.performClick()
                previousPage = pagerState.currentPage
            }
        }
        
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 3
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    recommendationViewModel = recommendationViewModel,
                    playbackViewModel = playbackViewModel,
                    playlistQueueViewModel = playlistQueueViewModel,
                    dialogViewModel = dialogViewModel,
                    navController = navController
                )
                1 -> GalleryScreen(
                    playbackViewModel = playbackViewModel,
                    playlistQueueViewModel = playlistQueueViewModel,
                    dialogViewModel = dialogViewModel,
                    navController = navController
                )
                2 -> ListScreen(
                    navController = navController
                )
                3 -> UserScreen(
                    settingsViewModel = settingsViewModel,
                    recommendationViewModel = recommendationViewModel,
                    navController = navController
                )
            }
        }
    }
}
