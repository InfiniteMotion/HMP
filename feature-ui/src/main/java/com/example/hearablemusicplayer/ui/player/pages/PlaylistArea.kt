package com.example.hearablemusicplayer.ui.player.pages

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.CurrentPlayingConfig
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.EditConfig
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.FullItemOptions
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.HeaderConfig
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.ItemConfig
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.ItemVariant
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.MusicList
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.MusicListCallbacksAdapter
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.defaultMusicListConfig
import com.example.hearablemusicplayer.ui.common.util.rememberHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 播放器内嵌的播放列表区域：使用新版 musiclist，外观与行为与原来一致。
 * 展开/收起动画、展开时外层滚动、头部（清空 + 当前/总数）、固定高度列表。
 */
@Composable
fun PlaylistArea(
    expanded: Boolean,
    playlist: List<MusicInfo>,
    currentIndex: Int,
    scrollState: ScrollState,
    onClearPlaylist: () -> Unit,
    onPlayItem: suspend (MusicInfo) -> Unit,
    onMoveToTop: (MusicInfo) -> Unit,
    onRemoveFromPlaylist: (MusicInfo) -> Unit,
) {
    val haptic = rememberHapticFeedback()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(expanded) {
        if (expanded) {
            delay(320)
            val playlistHeightPx = with(density) { 560.dp.toPx() }
            val targetScroll = (scrollState.value + playlistHeightPx).toInt()
            scrollState.animateScrollTo(
                value = targetScroll.coerceAtMost(scrollState.maxValue),
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    val callbacks = object : MusicListCallbacksAdapter() {
        override fun onItemClick(musicInfo: MusicInfo, index: Int) {
            haptic.performClick()
            scope.launch { onPlayItem(musicInfo) }
        }
        override fun onPinToTop(musicInfo: MusicInfo) {
            haptic.performConfirm()
            onMoveToTop(musicInfo)
        }
        override fun onRemove(musicInfo: MusicInfo) {
            haptic.performLightClick()
            onRemoveFromPlaylist(musicInfo)
        }
    }
    val config = defaultMusicListConfig(callbacks).copy(
        header = HeaderConfig.None,
        item = ItemConfig(
            showIndex = true,
            variant = ItemVariant.Full,
            fullOptions = FullItemOptions(
                showPinButton = true,
                showRemoveButton = true,
                showMenuButton = false,
            ),
        ),
        edit = EditConfig(enabled = false),
        currentPlaying = CurrentPlayingConfig(
            index = currentIndex.takeIf { it in playlist.indices },
            autoScrollToCurrent = true,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    )

    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 32.dp, top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        haptic.performLightClick()
                        onClearPlaylist()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.clear),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "${currentIndex + 1}/${playlist.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            MusicList(
                musicInfoList = playlist,
                config = config,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                isPlaying = true,
            )
        }
    }
}
