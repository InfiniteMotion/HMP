package com.hmp.desktop.ui.common.pages
import com.hmp.desktop.ui.common.navigation.NavController

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
import org.koin.compose.koinInject


import com.hmp.desktop.ui.library.pages.GalleryScreen
import com.hmp.desktop.ui.library.pages.HomeScreen
import com.hmp.desktop.ui.library.pages.ListScreen
import com.hmp.desktop.ui.settings.pages.UserScreen
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.common.pages.base.LocalTabHeaderContent
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.settings.viewmodel.RecommendationViewModel
import com.hmp.desktop.ui.settings.viewmodel.SettingsViewModel

@Composable
fun rememberTabsPagerState(
    pageCount: Int,
    initialPage: Int = 0
): PagerState = rememberPagerState(initialPage = initialPage) { pageCount }

@Composable
fun TabsHost(
    navController: NavController,
    pagerState: PagerState,
    tabHeader: @Composable () -> Unit,
    recommendationViewModel: RecommendationViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject(),
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

