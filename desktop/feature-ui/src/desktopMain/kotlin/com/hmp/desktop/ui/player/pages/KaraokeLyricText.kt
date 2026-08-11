package com.hmp.desktop.ui.player.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.hmp.domain.lyrics.CharTiming
import com.hmp.domain.lyrics.KaraokePositionInterpolator
import com.hmp.domain.lyrics.findKaraokeProgress
import kotlinx.coroutines.isActive

/**
 * 卡拉 OK 渐变歌词文本：底层绘制暗色全文，上层主色文本按 [progress]（0..1）
 * 从左到右裁剪，形成逐字点亮效果。不做逐字符测量，进度按整行宽度映射。
 *
 * 平滑策略：在两次底层位置采样之间用单调时钟外推播放位置（[KaraokePositionInterpolator]），
 * 使渐变以渲染帧率连续推进；进度以 [mutableFloatStateOf] 保存在绘制阶段读取，
 * 避免逐帧触发文本重组。
 */
@Composable
fun KaraokeLyricText(
    text: String,
    currentPosition: Long,
    isPlaying: Boolean,
    charTimings: List<CharTiming>,
    lineStartMs: Long,
    lineEndMs: Long,
    activeColor: Color,
    inactiveColor: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    val progress = remember(charTimings, lineStartMs, lineEndMs) {
        mutableFloatStateOf(findKaraokeProgress(charTimings, lineStartMs, lineEndMs, currentPosition))
    }

    // 每次采样 / 播放状态 / 行变化时重置外推基准并启动帧循环
    LaunchedEffect(charTimings, lineStartMs, lineEndMs, currentPosition, isPlaying) {
        var lastPosition = currentPosition
        progress.floatValue = findKaraokeProgress(charTimings, lineStartMs, lineEndMs, lastPosition)
        var lastNanos = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                lastPosition = KaraokePositionInterpolator.interpolatePosition(
                    lastPositionMs = lastPosition,
                    lastTimestampNanos = lastNanos,
                    nowNanos = now,
                    isPlaying = isPlaying
                )
                lastNanos = now
                progress.floatValue = findKaraokeProgress(charTimings, lineStartMs, lineEndMs, lastPosition)
            }
        }
    }

    Box(modifier = modifier) {
        Text(
            text = text,
            color = inactiveColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign
        )
        Text(
            text = text,
            color = activeColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            modifier = Modifier.drawWithContent {
                clipRect(right = size.width * progress.floatValue) {
                    this@drawWithContent.drawContent()
                }
            }
        )
    }
}
