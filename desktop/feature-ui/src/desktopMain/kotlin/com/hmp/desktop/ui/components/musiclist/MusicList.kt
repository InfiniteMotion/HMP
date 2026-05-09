package com.hmp.desktop.ui.components.musiclist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.hmp.domain.music.MusicInfo

@Composable
fun MusicList(
    musicInfoList: List<MusicInfo>,
    config: MusicListConfig,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    isPlaying: Boolean = false,
) {
    val state = rememberMusicListState()
    val density = LocalDensity.current
    val currentIndex = config.currentPlaying.index
    val isCurrentPlaying: (Int) -> Boolean = { index -> currentIndex == index }

    LaunchedEffect(currentIndex, listState) {
        if (currentIndex != null &&
            currentIndex in musicInfoList.indices &&
            config.currentPlaying.autoScrollToCurrent
        ) {
            val offsetPx = config.currentPlaying.scrollOffsetForCenter?.let { dp ->
                with(density) { dp.toPx() }.toInt()
            } ?: 0
            listState.animateScrollToItem(
                index = currentIndex,
                scrollOffset = offsetPx,
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        val showEditToolbar = config.edit.enabled && state.isEditMode && config.edit.showToolbar
        AnimatedContent(
            targetState = showEditToolbar,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "editMode",
        ) { isEdit ->
            if (isEdit) {
                MusicListEditToolbar(
                    config = config.edit,
                    state = state,
                    allIds = musicInfoList.map { it.music.id }.toSet(),
                    callbacks = config.callbacks,
                    onExitEditMode = {
                        state.exitEditMode()
                        config.callbacks.onExitEditMode()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                MusicListHeader(
                    config = config.header,
                    modifier = Modifier.fillMaxWidth(),
                    showEditButton = config.edit.enabled,
                    onEditClick = {
                        state.enterEditMode()
                        config.callbacks.onEnterEditMode()
                    },
                    listCount = musicInfoList.size.takeIf { config.header !is HeaderConfig.None },
                )
            }
        }
        if (config.header !is HeaderConfig.None) {
            Spacer(modifier = Modifier.height(12.dp))
        }
        val indexStripWidth = 24.dp
        val showIndexStrip = config.indexJump.enabled && musicInfoList.size > 10
        val showScrollbar = config.scrollbar.enabled && !showIndexStrip

        Box(modifier = Modifier.fillMaxSize()) {
            MusicListContent(
                musicInfoList = musicInfoList,
                config = config,
                listState = listState,
                state = state,
                isCurrentPlaying = isCurrentPlaying,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (showIndexStrip) Modifier.padding(end = indexStripWidth)
                        else Modifier
                    ),
            )
            if (showScrollbar) {
                MusicListScrollbar(
                    listState = listState,
                    config = config.scrollbar,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxSize(),
                )
            }
            if (showIndexStrip) {
                MusicListIndexStrip(
                    musicInfoList = musicInfoList,
                    listState = listState,
                    config = config.indexJump,
                    currentPlayingIndex = config.currentPlaying.index,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(indexStripWidth)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
fun FixedMusicList(
    musicInfoList: List<MusicInfo>,
    config: MusicListConfig,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
) {
    val state = rememberMusicListState()
    val currentIndex = config.currentPlaying.index
    val isCurrentPlaying: (Int) -> Boolean = { index -> currentIndex == index }

    Column(modifier = modifier.fillMaxWidth()) {
        val showEditToolbar = config.edit.enabled && state.isEditMode && config.edit.showToolbar
        AnimatedContent(
            targetState = showEditToolbar,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "editMode",
        ) { isEdit ->
            if (isEdit) {
                MusicListEditToolbar(
                    config = config.edit,
                    state = state,
                    allIds = musicInfoList.map { it.music.id }.toSet(),
                    callbacks = config.callbacks,
                    onExitEditMode = {
                        state.exitEditMode()
                        config.callbacks.onExitEditMode()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                MusicListHeader(
                    config = config.header,
                    modifier = Modifier.fillMaxWidth(),
                    showEditButton = config.edit.enabled,
                    onEditClick = {
                        state.enterEditMode()
                        config.callbacks.onEnterEditMode()
                    },
                    listCount = musicInfoList.size.takeIf { config.header !is HeaderConfig.None },
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            FixedMusicListContent(
                musicInfoList = musicInfoList,
                config = config,
                state = state,
                isCurrentPlaying = isCurrentPlaying,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
