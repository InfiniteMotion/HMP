package com.hearablemusic.player.ui.player.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.lyrics.LrcParser
import com.hmp.domain.lyrics.LyricLineData
import com.hmp.domain.lyrics.findCurrentLyricIndex
import com.hearablemusic.player.ui.R

/**
 * 高级歌词组件 - 全新重构版本
 * 基于SmartLyrics样式，但采用更完善的解析和显示逻辑
 */
@Composable
fun AdvancedLyrics(
    modifier: Modifier = Modifier,
    lyrics: String?,
    currentPosition: Long,
    onSeek: (Long) -> Unit = {},
    originalTextSize: Int = 14,
    translatedTextSize: Int = 14,
    currentTimeTextSize: Int = 16,
    lineSpacing: Int = 6,
    displayMode: DisplayMode = DisplayMode.DUAL,
    alignment: LyricsAlignment = LyricsAlignment.CENTER
) {
    if (lyrics == null) {
        EmptyLyricsView(modifier)
        return
    }

    val parsedLyrics = remember(lyrics) { LrcParser.parse(lyrics) }
    val scrollState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    var containerHeightPx by remember { mutableIntStateOf(0) }

    val lazyColumnHorizontalAlignment = when (alignment) {
        LyricsAlignment.LEFT -> Alignment.Start
        LyricsAlignment.CENTER -> Alignment.CenterHorizontally
        LyricsAlignment.RIGHT -> Alignment.End
    }

    // 找到当前播放位置对应的歌词行
    val currentIndex by remember(parsedLyrics, currentPosition) {
        derivedStateOf { findCurrentLyricIndex(parsedLyrics, currentPosition) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerHeightPx = it.height }
    ) {
        // 自动滚动到当前行，居中显示（contentPadding 已提供上下对称空间）
        LaunchedEffect(currentIndex, containerHeightPx) {
            if (currentIndex >= 0 && currentIndex < parsedLyrics.size && containerHeightPx > 0) {
                scrollState.animateScrollToItem(
                    index = currentIndex,
                    scrollOffset = 0
                )
            }
        }

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = lazyColumnHorizontalAlignment,
            contentPadding = PaddingValues(vertical = with(density) { (containerHeightPx / 2).let { if(it > 0) it.toDp() else 300.dp } })
        ) {
            itemsIndexed(
                items = parsedLyrics,
                key = { index, item -> "${item.timestamp}_${index}" }
            ) { index, lyricLine ->
                    val isCurrent = index == currentIndex && currentIndex >= 0
                AdvancedLyricItem(
                    lyricLine = lyricLine,
                    isCurrent = isCurrent,
                    displayMode = displayMode,
                    originalTextSize = originalTextSize,
                    translatedTextSize = translatedTextSize,
                    currentTimeTextSize = currentTimeTextSize,
                    lineSpacing = lineSpacing,
                    alignment = alignment,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSeek(lyricLine.timestamp)
                    }
                )
            }
        }
    }
}

/**
 * 高级歌词项 - 优化的渲染逻辑
 */
@Composable
private fun AdvancedLyricItem(
    lyricLine: LyricLineData,
    isCurrent: Boolean,
    displayMode: DisplayMode,
    originalTextSize: Int = 14,
    translatedTextSize: Int = 14,
    currentTimeTextSize: Int = 16,
    lineSpacing: Int = 6,
    alignment: LyricsAlignment = LyricsAlignment.CENTER,
    onClick: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0.7f,
        animationSpec = tween(durationMillis = 200),
        label = "LyricAlpha"
    )

    val translated = lyricLine.translatedText
    val lineSpacing = ((if (translated != null) 2 else 1) * lineSpacing).dp

    val textAlign = when (alignment) {
        LyricsAlignment.LEFT -> TextAlign.Start
        LyricsAlignment.CENTER -> TextAlign.Center
        LyricsAlignment.RIGHT -> TextAlign.End
    }

    val horizontalAlignment = when (alignment) {
        LyricsAlignment.LEFT -> Alignment.Start
        LyricsAlignment.CENTER -> Alignment.CenterHorizontally
        LyricsAlignment.RIGHT -> Alignment.End
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = lineSpacing)
            .graphicsLayer {
                scaleX = if (isCurrent) 1.05f else 1f
                scaleY = if (isCurrent) 1.05f else 1f
                alpha = animatedAlpha
            }
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = Transparent,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = horizontalAlignment,
        ) {
            when (displayMode) {
                DisplayMode.LANG1 -> {
                    Text(
                        text = lyricLine.originalText,
                        textAlign = textAlign,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = originalTextSize.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                    )
                }
                DisplayMode.LANG2 -> {
                    if (translated != null) {
                        Text(
                            text = translated,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = translatedTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                        )
                    } else {
                        Text(
                            text = lyricLine.originalText,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = originalTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                        )
                    }
                }
                DisplayMode.DUAL -> {
                    if (translated == null) {
                        Text(
                            text = lyricLine.originalText,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = if (isCurrent) currentTimeTextSize.sp else originalTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                        )
                    } else {
                        Text(
                            text = lyricLine.originalText,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = if (isCurrent) currentTimeTextSize.sp else originalTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = translated,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = translatedTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}

/**
 * 空歌词视图
 */
@Composable
private fun EmptyLyricsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_gallery_search_things),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "暂无歌词",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
