package com.example.hearablemusicplayer.ui.components.musiclist

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import kotlinx.coroutines.launch

/** 索引条上下留白 */
private val IndexStripVerticalPadding = 32.dp

/**
 * 索引条：全高度，上下各 [IndexStripVerticalPadding]；字母等距分布，小号字体。
 * 支持点击与滑动：点击或滑动到某字母时列表滚动至对应项，当前选中项随滑动高亮。
 */
@Composable
internal fun MusicListIndexStrip(
    musicInfoList: List<MusicInfo>,
    listState: LazyListState,
    config: IndexJumpConfig,
    currentPlayingIndex: Int?,
    modifier: Modifier = Modifier,
) {
    val letterToIndexMap = remember(musicInfoList) { config.letterToIndex(musicInfoList) }
    val scope = rememberCoroutineScope()
    val letters = config.letters
    // 用 snapshotFlow 订阅首项可见索引，使手动滑动列表时高亮也能随动
    var firstVisibleIndex by remember { mutableStateOf(listState.firstVisibleItemIndex) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisibleIndex = it }
    }
    val currentLetter = remember(letterToIndexMap, firstVisibleIndex, letters) {
        letters
            .mapNotNull { letter -> letterToIndexMap[letter]?.let { letter to it } }
            .filter { it.second <= firstVisibleIndex }
            .maxByOrNull { it.second }
            ?.first
    }
    var dragLetter by remember { mutableStateOf<Char?>(null) }
    var stripSize by remember { mutableStateOf(IntSize.Zero) }
    var lastScrollJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val haptic = rememberHapticFeedback()
    val density = LocalDensity.current
    val verticalPaddingPx = with(density) { IndexStripVerticalPadding.roundToPx() }

    val highlightLetter = dragLetter ?: currentLetter

    fun scrollToIndex(idx: Int) {
        lastScrollJob?.cancel()
        lastScrollJob = scope.launch {
            listState.animateScrollToItem(idx)
            lastScrollJob = null
        }
    }

    Box(
        modifier = modifier
            .width(24.dp)
            .fillMaxHeight()
            .onSizeChanged { stripSize = it }
            .pointerInput(letters, letterToIndexMap, stripSize, verticalPaddingPx) {
                if (stripSize.height <= 0 || letters.isEmpty()) return@pointerInput
                val contentTop = verticalPaddingPx.toFloat()
                val contentHeight = (stripSize.height - 2 * verticalPaddingPx).toFloat().coerceAtLeast(1f)
                fun letterIndexForY(y: Float): Int {
                    val t = ((y - contentTop) / contentHeight * letters.size).toInt()
                    return t.coerceIn(0, letters.lastIndex)
                }
                detectDragGestures(
                    onDragStart = { offset ->
                        val letter = letters[letterIndexForY(offset.y)]
                        dragLetter = letter
                        letterToIndexMap[letter]?.let { idx ->
                            haptic.performLightClick()
                            scrollToIndex(idx)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val letter = letters[letterIndexForY(change.position.y)]
                        if (letter != dragLetter) {
                            dragLetter = letter
                            letterToIndexMap[letter]?.let { idx ->
                                haptic.performLightClick()
                                scrollToIndex(idx)
                            }
                        }
                    },
                    onDragEnd = { dragLetter = null },
                    onDragCancel = { dragLetter = null },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = IndexStripVerticalPadding)
                .width(24.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            letters.forEach { letter ->
                val index = letterToIndexMap[letter]
                val isCurrent = letter == highlightLetter
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    CircleShape,
                                ),
                        )
                    }
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        },
                        modifier = Modifier
                            .clickable(enabled = index != null) {
                                index?.let { idx ->
                                    haptic.performLightClick()
                                    scrollToIndex(idx)
                                }
                            }
                            .semantics {
                                contentDescription = "Jump to $letter"
                            },
                    )
                }
            }
        }
    }
}
