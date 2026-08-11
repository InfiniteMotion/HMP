package com.hearablemusic.player.ui.player.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

/**
 * 卡拉 OK 渐变歌词文本：底层绘制暗色全文，上层主色文本按 [progress]（0..1）
 * 从左到右裁剪，形成逐字点亮效果。不做逐字符测量，进度按整行宽度映射。
 */
@Composable
fun KaraokeLyricText(
    text: String,
    progress: Float,
    activeColor: Color,
    inactiveColor: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    val clippedProgress = progress.coerceIn(0f, 1f)
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
                clipRect(right = size.width * clippedProgress) {
                    this@drawWithContent.drawContent()
                }
            }
        )
    }
}
