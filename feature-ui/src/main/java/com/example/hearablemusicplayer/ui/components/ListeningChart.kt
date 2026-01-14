package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListeningChart(
    text: String,
    data: List<Int>,
    days: List<String>
) {

    val (gridData, weekLabels) = remember(data) {
        val totalDays = 35 // 5周 x 7天
        val today = LocalDate.now()

        val allDates = (0 until totalDays).map { i ->
            today.minusDays((totalDays - 1 - i).toLong())
        }

        val paddedData = if (data.size < totalDays) {
            List(totalDays - data.size) { 0 } + data
        } else {
            data.takeLast(totalDays)
        }
        
        val dataWithDate = allDates.zip(paddedData)
        
        // 生成星期标签 (Mon, Wed, Fri, Sun) 或 (S M T W T F S)
        val labels = listOf("M", "T", "W", "T", "F", "S", "S")
        
        dataWithDate to labels
    }

    val maxValue = (data.maxOrNull() ?: 1).toFloat().coerceAtLeast(1f)
    
    // 颜色计算函数
    fun getColorForValue(value: Int, max: Float): Color {
        if (value == 0) return Color.Gray.copy(alpha = 0.2f)
        val ratio = value / max
        return when {
            ratio < 0.25f -> Color(0xFF9BE9A8) // GitHub Level 1
            ratio < 0.5f -> Color(0xFF40C463)  // GitHub Level 2
            ratio < 0.75f -> Color(0xFF30A14E) // GitHub Level 3
            else -> Color(0xFF216E39)          // GitHub Level 4
        }
    }
    
    @Composable
    fun getThemeColorForValue(value: Int, max: Float): Color {
        if (value == 0) return MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        val ratio = (value / max).coerceIn(0.2f, 1f)
        return MaterialTheme.colorScheme.primary.copy(alpha = ratio)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Transparent
        ),
        border = BorderStroke(2.dp, color = MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text, // e.g. "Monthly Activity"
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Last 35 Days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 绘制热力图网格
            // 7列 (Mon-Sun)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // 数据网格: 5行 x 7列
                val rows = 5
                val cols = 7
                
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until cols) {
                            val index = row * cols + col
                            // 调整宽高比，稍微扁一点点或者保持正方形但整体变小
                            val weightModifier = Modifier.weight(1f).aspectRatio(1.5f)
                            
                            if (index < gridData.size) {
                                val (date, value) = gridData[index]
                                val color = getThemeColorForValue(value, maxValue)
                                
                                Box(
                                    modifier = weightModifier
                                        .clip(RoundedCornerShape(5.dp)) // 减小圆角 4.dp -> 3.dp
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
    }
}


