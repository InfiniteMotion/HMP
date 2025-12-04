package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.unit.dp

// 音效预设选择器组件
@Composable
fun EqualizerPresetSelector(
    presets: List<String>,
    currentPreset: Int,
    onPresetSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 将预设列表分成两行
        val rows = presets.chunked(2)
        rows.forEach { rowPresets ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowPresets.forEachIndexed { rowIndex, preset ->
                    val globalIndex = rows.indexOf(rowPresets) * 2 + rowIndex
                    val isSelected = globalIndex == currentPreset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onPresetSelected(globalIndex)
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// 低音增强滑块组件
@Composable
fun BassBoostSlider(
    currentLevel: Int,
    onLevelChanged: (Int) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(currentLevel.toFloat()) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "低音增强",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${currentLevel}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue
                onLevelChanged(newValue.toInt())
            },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 环绕声开关组件
@Composable
fun SurroundSoundToggle(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onToggle(!isEnabled)
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "环绕声",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Checkbox(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}

// 混响设置组件
@Composable
fun ReverbSettings(
    currentPreset: Int,
    onPresetChanged: (Int) -> Unit
) {
    val reverbPresets = listOf("关闭", "小房间", "大房间", "大厅", "教堂")
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "混响",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 将混响预设分成两行，第一行3个，第二行2个
            val firstRow = reverbPresets.take(3)
            val secondRow = reverbPresets.drop(3)
            
            // 第一行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                firstRow.forEachIndexed { index, preset ->
                    val isSelected = index == currentPreset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onPresetChanged(index)
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // 第二行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                secondRow.forEachIndexed { index, preset ->
                    val globalIndex = 3 + index
                    val isSelected = globalIndex == currentPreset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onPresetChanged(globalIndex)
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// 自定义均衡器调节组件
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEqualizer(
    bandCount: Int,
    bandLevelRange: Pair<Int, Int>,
    currentBandLevels: FloatArray,
    onBandLevelChanged: (Int, Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            for (i in 0 until bandCount) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val currentLevel = currentBandLevels.getOrElse(i) { 0f }
                    var sliderValue by remember { mutableFloatStateOf(currentLevel) }
                    
                    Slider(
                        value = sliderValue,
                        onValueChange = { newValue ->
                            sliderValue = newValue
                            onBandLevelChanged(i, newValue)
                        },
                        valueRange = bandLevelRange.first.toFloat()..bandLevelRange.second.toFloat(),
                        modifier = Modifier
                            .size(24.dp, 150.dp)
                            .rotate(-90f),
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    )
                    Text(
                        text = "${i + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
