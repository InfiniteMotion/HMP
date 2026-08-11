package com.hmp.desktop.ui.player.pages

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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.lyrics.CharTiming
import com.hmp.domain.lyrics.LrcParser
import com.hmp.domain.lyrics.LyricLineData
import com.hmp.domain.lyrics.LyricsTimingGenerator
import com.hmp.domain.lyrics.findCurrentLyricIndex
import com.hmp.domain.lyrics.findKaraokeProgress
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource

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
    totalDurationMs: Long = 0L,
    karaokeEnabled: Boolean = true,
    displayMode: DisplayMode = DisplayMode.DUAL,
    alignment: LyricsAlignment = LyricsAlignment.CENTER
) {
    if (lyrics == null) {
        EmptyLyricsView(modifier)
        return
    }

    val parsedLyrics = remember(lyrics) { LrcParser.parse(lyrics) }
    // 为逐字显示生成/规整片段时间（增强 LRC 优先，普通 LRC 走 S2 行尾对齐等分）
    val timedLyrics = remember(parsedLyrics, totalDurationMs) {
        LyricsTimingGenerator.resolve(parsedLyrics, totalDurationMs.takeIf { it > 0 })
    }
    val scrollState = rememberLazyListState()
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
        // 自动滚动到当前行（居中）
        LaunchedEffect(currentIndex, containerHeightPx) {
            if (currentIndex >= 0 && currentIndex < timedLyrics.size && containerHeightPx > 0) {
                scrollState.animateScrollToItem(index = currentIndex)
            }
        }

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = lazyColumnHorizontalAlignment,
            contentPadding = PaddingValues(vertical = with(density) { (containerHeightPx * 0.3f).toInt().let { if(it > 0) it.toDp() else 200.dp } })
        ) {
            itemsIndexed(
                items = timedLyrics,
                key = { index, item -> "${item.timestamp}_${index}" }
            ) { index, lyricLine ->
                    val isCurrent = index == currentIndex && currentIndex >= 0
                val lineEndMs = LyricsTimingGenerator.lineEndMs(
                    timedLyrics,
                    index,
                    totalDurationMs.takeIf { it > 0 }
                )
                AdvancedLyricItem(
                    lyricLine = lyricLine,
                    isCurrent = isCurrent,
                    karaokeEnabled = karaokeEnabled,
                    charTimings = lyricLine.charTimings,
                    translatedCharTimings = lyricLine.translatedCharTimings,
                    lineStartMs = lyricLine.timestamp,
                    lineEndMs = lineEndMs,
                    currentPosition = currentPosition,
                    displayMode = displayMode,
                    originalTextSize = originalTextSize,
                    translatedTextSize = translatedTextSize,
                    currentTimeTextSize = currentTimeTextSize,
                    lineSpacing = lineSpacing,
                    alignment = alignment,
                    onClick = {
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
    karaokeEnabled: Boolean = true,
    charTimings: List<CharTiming> = emptyList(),
    translatedCharTimings: List<CharTiming> = emptyList(),
    lineStartMs: Long = 0L,
    lineEndMs: Long = 0L,
    currentPosition: Long = 0L,
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
                    LyricText(
                        text = lyricLine.originalText,
                        isCurrent = isCurrent,
                        karaokeEnabled = karaokeEnabled,
                        charTimings = charTimings,
                        lineStartMs = lineStartMs,
                        lineEndMs = lineEndMs,
                        currentPosition = currentPosition,
                        textAlign = textAlign,
                        fontSize = originalTextSize.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                }
                DisplayMode.LANG2 -> {
                    if (translated != null) {
                        LyricText(
                            text = translated,
                            isCurrent = isCurrent,
                            karaokeEnabled = karaokeEnabled,
                            charTimings = translatedCharTimings,
                            lineStartMs = lineStartMs,
                            lineEndMs = lineEndMs,
                            currentPosition = currentPosition,
                            textAlign = textAlign,
                            fontSize = translatedTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    } else {
                        LyricText(
                            text = lyricLine.originalText,
                            isCurrent = isCurrent,
                            karaokeEnabled = karaokeEnabled,
                            charTimings = charTimings,
                            lineStartMs = lineStartMs,
                            lineEndMs = lineEndMs,
                            currentPosition = currentPosition,
                            textAlign = textAlign,
                            fontSize = originalTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                DisplayMode.DUAL -> {
                    if (translated == null) {
                        LyricText(
                            text = lyricLine.originalText,
                            isCurrent = isCurrent,
                            karaokeEnabled = karaokeEnabled,
                            charTimings = charTimings,
                            lineStartMs = lineStartMs,
                            lineEndMs = lineEndMs,
                            currentPosition = currentPosition,
                            textAlign = textAlign,
                            fontSize = if (isCurrent) currentTimeTextSize.sp else originalTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    } else {
                        LyricText(
                            text = lyricLine.originalText,
                            isCurrent = isCurrent,
                            karaokeEnabled = karaokeEnabled,
                            charTimings = charTimings,
                            lineStartMs = lineStartMs,
                            lineEndMs = lineEndMs,
                            currentPosition = currentPosition,
                            textAlign = textAlign,
                            fontSize = if (isCurrent) currentTimeTextSize.sp else originalTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        LyricText(
                            text = translated,
                            isCurrent = isCurrent,
                            karaokeEnabled = karaokeEnabled,
                            charTimings = translatedCharTimings,
                            lineStartMs = lineStartMs,
                            lineEndMs = lineEndMs,
                            currentPosition = currentPosition,
                            textAlign = textAlign,
                            fontSize = translatedTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

/**
 * 歌词文本：当前行且开启逐字时使用卡拉 OK 渐变，否则保持原有整行样式。
 */
@Composable
private fun LyricText(
    text: String,
    isCurrent: Boolean,
    karaokeEnabled: Boolean,
    charTimings: List<CharTiming>,
    lineStartMs: Long,
    lineEndMs: Long,
    currentPosition: Long,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    if (isCurrent && karaokeEnabled && text.isNotBlank()) {
        val progress = findKaraokeProgress(charTimings, lineStartMs, lineEndMs, currentPosition)
        KaraokeLyricText(
            text = text,
            progress = progress,
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            modifier = modifier
        )
    } else {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            modifier = modifier
        )
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
            painter = painterResource(Res.drawable.ic_gallery_search_things),
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
