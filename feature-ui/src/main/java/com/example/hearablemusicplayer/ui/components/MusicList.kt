package com.example.hearablemusicplayer.ui.components

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun MusicList(
    musicInfoList: List<MusicInfo>,
    onItemClick: suspend (MusicInfo) -> Unit,
    onAddToPlaylist: (MusicInfo) -> Unit,
    onMenuClick: (MusicInfo) -> Unit,
    showAddButton: Boolean,
    showMenuButton: Boolean,
    isPlaying: Boolean,
    playlistId: Long? = null,
    onRemoveFromPlaylist: (MusicInfo) -> Unit = {},
    showReorderButtons: Boolean = false,
    onMoveUp: (Int) -> Unit = {},
    onMoveDown: (Int) -> Unit = {}
) {
    val haptic = rememberHapticFeedback()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = musicInfoList,
            key = { index, musicInfo -> "${musicInfo.music.id}_$index" }
        ) { index, musicInfo ->
            val itemExtraMenuItems = if (playlistId != null) {
                listOf(
                    stringResource(R.string.remove_from_playlist) to { onRemoveFromPlaylist(musicInfo) }
                )
            } else emptyList()
            MusicItem(
                musicInfo = musicInfo,
                onItemClick = {
                    haptic.performClick()
                    coroutineScope.launch {
                        onItemClick(musicInfo)
                    }
                },
                onAddToPlaylist = { onAddToPlaylist(musicInfo) },
                onMenuClick = { onMenuClick(musicInfo) },
                showAddButton = showAddButton,
                showMenuButton = showMenuButton,
                isPlaying = isPlaying,
                modifier = Modifier,
                extraMenuItems = itemExtraMenuItems,
                showMoveUp = showReorderButtons && index > 0,
                showMoveDown = showReorderButtons && index < musicInfoList.size - 1,
                onMoveUp = { onMoveUp(index) },
                onMoveDown = { onMoveDown(index) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun FixedMusicList(
    musicInfoList: List<MusicInfo>,
    onItemClick: suspend (MusicInfo) -> Unit,
    onAddToPlaylist: (MusicInfo) -> Unit,
    onMenuClick: (MusicInfo) -> Unit,
    showAddButton: Boolean,
    showMenuButton: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        musicInfoList.forEach { musicInfo ->
            MusicItem(
                musicInfo = musicInfo,
                onItemClick = {
                    haptic.performClick()
                    coroutineScope.launch {
                        onItemClick(musicInfo)
                    }
                },
                onAddToPlaylist = { onAddToPlaylist(musicInfo) },
                onMenuClick = { onMenuClick(musicInfo) },
                showAddButton = showAddButton,
                showMenuButton = showMenuButton,
                isPlaying = isPlaying,
                modifier = Modifier
            )
        }
    }
}

// 播放列表区域组件
@Composable
fun PlaylistArea(
    expanded: Boolean,
    playlist: List<MusicInfo>,
    currentIndex: Int,
    scrollState: ScrollState,
    onClearPlaylist: () -> Unit,
    onPlayItem: suspend (MusicInfo) -> Unit,
    onMoveToTop: (MusicInfo) -> Unit,
    onRemoveFromPlaylist: (MusicInfo) -> Unit
) {
    val haptic = rememberHapticFeedback()
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // 预加载：提前准备好播放列表的初始状态，避免首次展开卡顿
    LaunchedEffect(playlist.size, currentIndex) {
        if (playlist.isNotEmpty() && !expanded) {
            listState.scrollToItem(currentIndex.coerceIn(0, playlist.lastIndex))
        }
    }

    // 当播放列表展开时，滚动页面使播放列表底部与屏幕底部对齐
    LaunchedEffect(expanded) {
        if (expanded) {
            delay(320)
            val playlistHeightPx = with(density) { 560.dp.toPx() }
            val targetScroll = (scrollState.value + playlistHeightPx).toInt()
            scrollState.animateScrollTo(
                value = targetScroll.coerceAtMost(scrollState.maxValue),
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )

            if (playlist.isNotEmpty()) {
                delay(150)
                listState.animateScrollToItem(
                    index = currentIndex.coerceIn(0, playlist.lastIndex),
                )
            }
        }
    }

    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 32.dp, top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        haptic.performLightClick()
                        onClearPlaylist()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.clear),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "${currentIndex + 1}/${playlist.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(horizontal = 16.dp)
                    .nestedScroll(remember {
                        object : NestedScrollConnection {
                            override fun onPostScroll(
                                consumed: Offset,
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                return available
                            }
                        }
                    })
            ) {
                itemsIndexed(
                    items = playlist,
                    key = { index, item -> "${item.music.id}_$index" }
                ) { index, musicInfo ->
                    PlaylistItem(
                        musicInfo = musicInfo,
                        isCurrentPlaying = index == currentIndex,
                        index = index + 1,
                        onItemClick = {
                            haptic.performClick()
                            coroutineScope.launch {
                                onPlayItem(musicInfo)
                            }
                        },
                        onPinClick = {
                            haptic.performConfirm()
                            onMoveToTop(musicInfo)
                        },
                        onRemoveClick = {
                            haptic.performLightClick()
                            onRemoveFromPlaylist(musicInfo)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }
}

// Gallery 列表组件 - 专门用于 Gallery 页面的音乐列表
@OptIn(UnstableApi::class)
@Composable
fun GalleryList(
    musicInfoList: List<MusicInfo>,
    onItemClick: (MusicInfo) -> Unit,
    onMenuClick: (MusicInfo) -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = musicInfoList,
            key = { musicInfo -> musicInfo.music.id }
        ) { musicInfo ->
            GalleryItem(
                musicInfo = musicInfo,
                onItemClick = {
                    haptic.performClick()
                    onItemClick(musicInfo)
                },
                onMenuClick = {
                    haptic.performLightClick()
                    onMenuClick(musicInfo)
                },
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}