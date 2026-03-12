package com.example.hearablemusicplayer.ui.components.musiclist

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.domain.music.MusicInfo

/**
 * 增强版音乐列表根组件。根据 [config] 渲染头部、编辑栏、列表内容；内部持有 [LazyListState] 与 [MusicListState]。
 *
 * @param musicInfoList 数据源
 * @param config 统一配置（头部、单项、列表、编辑、索引、滚动条、当前播放、回调）
 * @param modifier 根 Modifier
 * @param isPlaying 是否正在播放（可用于将来在单项上显示播放图标等）
 */
@Composable
fun MusicList(
    musicInfoList: List<MusicInfo>,
    config: MusicListConfig,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
) {
    val listState = rememberLazyListState()
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
                    onExitEditMode = state::exitEditMode,
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
        // 索引条宽度，与 MusicListIndexStrip 一致，用于为列表预留右侧空间
        val indexStripWidth = 24.dp
        val showIndexStrip = config.indexJump.enabled && musicInfoList.isNotEmpty()
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
