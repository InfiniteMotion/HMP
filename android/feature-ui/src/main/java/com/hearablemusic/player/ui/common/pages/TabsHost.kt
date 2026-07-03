package com.hearablemusic.player.ui.common.pages

import androidx.annotation.OptIn
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
import androidx.media3.common.util.UnstableApi
import org.koin.androidx.compose.koinViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hearablemusic.player.ui.library.pages.GalleryScreen
import com.hearablemusic.player.ui.library.pages.HomeScreen
import com.hearablemusic.player.ui.library.pages.ListScreen
import com.hearablemusic.player.ui.settings.pages.UserScreen
import com.hearablemusic.player.ui.common.util.rememberHapticFeedback
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogViewModel
import com.hearablemusic.player.ui.common.pages.base.LocalTabHeaderContent
import com.hearablemusic.player.ui.player.viewmodel.PlaybackViewModel
import com.hearablemusic.player.ui.player.viewmodel.PlaylistQueueViewModel
import com.hearablemusic.player.ui.settings.viewmodel.RecommendationViewModel
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel

@Composable
fun rememberTabsPagerState(
    pageCount: Int,
    initialPage: Int = 0
): PagerState = rememberPagerState(initialPage = initialPage) { pageCount }

@OptIn(UnstableApi::class)
@Composable
fun TabsHost(
    navController: NavBackStack<NavKey>,
    pagerState: PagerState,
    tabHeader: @Composable () -> Unit,
    recommendationViewModel: RecommendationViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
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
