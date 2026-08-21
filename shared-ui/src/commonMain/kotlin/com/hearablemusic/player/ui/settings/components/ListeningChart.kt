package com.hearablemusic.player.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.hearablemusic.player.ui.common.design.dimens.LocalHMPDimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListeningChart(
    data: List<Int>,
) {
    val dimens = LocalHMPDimens.current
    val (gridData, weekLabels) = remember(data) {
        val totalDays = 35 // 5周 x 7天

        val paddedData = if (data.size < totalDays) {
            List(totalDays - data.size) { 0 } + data
        } else {
            data.takeLast(totalDays)
        }

        // 生成星期标签 (Mon, Wed, Fri, Sun) 或 (S M T W T F S)
        val labels = listOf("M", "T", "W", "T", "F", "S", "S")

        paddedData to labels
    }

    val maxValue = (data.maxOrNull() ?: 1).toFloat().coerceAtLeast(1f)

    @Composable
    fun getThemeColorForValue(value: Int, max: Float): Color {
        if (value == 0) return MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        val ratio = (value / max).coerceIn(0.2f, 1f)
        return MaterialTheme.colorScheme.primary.copy(alpha = ratio)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.spacing.md),
        verticalArrangement = Arrangement.spacedBy(dimens.spacing.xs)
    ) {
        // 星期表头
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = dimens.type.xs,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(dimens.spacing.xs))

        // 数据网格: 5行 x 7列
        val rows = 5
        val cols = 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spacing.xs)
            ) {
                for (col in 0 until cols) {
                    val index = row * cols + col
                    val weightModifier = Modifier.weight(1f).aspectRatio(1.5f)

                    if (index < gridData.size) {
                        val value = gridData[index]
                        val color = getThemeColorForValue(value, maxValue)

                        Box(
                            modifier = weightModifier
                                .clip(RoundedCornerShape(dimens.corner.xs))
                                .background(color)
                        )
                    } else {
                        Spacer(modifier = weightModifier)
                    }
                }
            }
        }
    }
}


