package com.hmp.desktop.ui.components.musiclist

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
internal fun MusicListScrollbar(
    listState: LazyListState,
    config: ScrollbarConfig,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val widthPx = with(density) { config.width.toPx() }
    val thumbMinPx = with(density) { config.thumbMinHeight.toPx() }
    val cornerRadiusPx = with(density) { config.cornerRadius.toPx() }
    val trackColor = config.trackColor ?: Color.Black.copy(alpha = 0.1f)
    val thumbColor = config.thumbColor ?: Color.Black.copy(alpha = 0.4f)
    val scope = rememberCoroutineScope()
    var lastScrollJob by remember { mutableStateOf<Job?>(null) }

    val layoutInfo = listState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount.coerceAtLeast(1)
    val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
    val visibleCount = layoutInfo.visibleItemsInfo.size
    val thumbTopRatio = firstVisible.toFloat() / totalItems
    val thumbHeightRatio = (visibleCount.toFloat() / totalItems).coerceIn(0.1f, 1f)

    Box(
        modifier = modifier
            .width(config.width + 8.dp)
            .fillMaxHeight()
            .pointerInput(listState) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val trackHeight = size.height
                    if (trackHeight <= 0f) return@detectDragGestures
                    val total = listState.layoutInfo.totalItemsCount.coerceAtLeast(1)
                    val currentFirst = listState.firstVisibleItemIndex
                    val moveRatio = dragAmount.y / trackHeight
                    val deltaIndex = (moveRatio * total).toInt()
                    val targetIndex = (currentFirst + deltaIndex).coerceIn(0, total - 1)
                    if (targetIndex != currentFirst) {
                        lastScrollJob?.cancel()
                        lastScrollJob = scope.launch {
                            listState.animateScrollToItem(targetIndex)
                            lastScrollJob = null
                        }
                    }
                }
            },
        contentAlignment = Alignment.CenterEnd,
    ) {
        Canvas(
            modifier = Modifier
                .width(config.width)
                .fillMaxHeight(),
        ) {
            val h = size.height
            val w = size.width
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerRadiusPx),
            )
            val thumbH = (h * thumbHeightRatio).coerceAtLeast(thumbMinPx)
            val thumbY = (h - thumbH) * thumbTopRatio.coerceIn(0f, 1f)
            val thumbCornerPx = minOf(w, thumbH) / 2f
            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(0f, thumbY),
                size = Size(w, thumbH),
                cornerRadius = CornerRadius(thumbCornerPx),
            )
        }
    }
}
