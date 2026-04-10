package com.example.hearablemusicplayer.ui.pages.base

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.hearablemusicplayer.ui.pages.GalleryScreen
import com.example.hearablemusicplayer.ui.pages.HomeScreen
import com.example.hearablemusicplayer.ui.pages.ListScreen
import com.example.hearablemusicplayer.ui.pages.UserScreen
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel

@Composable
fun rememberTabsPagerState(
    pageCount: Int,
    initialPage: Int = 0
): PagerState = rememberPagerState(initialPage = initialPage) { pageCount }

@Composable
fun TabsHost(
    backStack: NavBackStack<NavKey>,
    pagerState: PagerState,
    tabHeader: @Composable () -> Unit,
    recommendationViewModel: RecommendationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    CompositionLocalProvider(
        LocalTabHeaderContent provides tabHeader
    ) {
        val haptic = rememberHapticFeedback()
        var previousPage by remember { mutableIntStateOf(pagerState.currentPage) }
        var isDragging by remember { mutableStateOf(false) }
        
        // 监听拖拽状态变化
        LaunchedEffect(pagerState.isScrollInProgress) {
            if (pagerState.isScrollInProgress && !isDragging) {
                // 开始拖拽时的轻微震动
                haptic.performGestureStart()
                isDragging = true
            } else if (!pagerState.isScrollInProgress && isDragging) {
                // 拖拽结束时重置状态
                isDragging = false
            }
        }
        
        // 监听页面变化并触发确认震动反馈
        LaunchedEffect(pagerState.currentPage) {
            if (pagerState.currentPage != previousPage) {
                // 页面切换确认时的标准震动
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
                    backStack = backStack
                )
                1 -> GalleryScreen(backStack = backStack)
                2 -> ListScreen(backStack = backStack)
                3 -> UserScreen(
                    settingsViewModel = settingsViewModel,
                    recommendationViewModel = recommendationViewModel,
                    backStack = backStack
                )
            }
        }
    }
}
