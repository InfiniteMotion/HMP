package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.ui.util.AnimationConfig
import kotlin.math.max

fun parseLrcTime(timeStr: String): Long {
    val (min, sec) = timeStr.split(":").map { it.toDouble() }
    return (min * 60_000 + sec * 1_000).toLong()
}

@Composable
fun Lyrics(
    lyrics: String?,
    currentPosition: Long,
    modifier: Modifier = Modifier,
    onSeek: (Long) -> Unit,
){
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (lyrics==null) {
            Text("未识别到歌词", color = MaterialTheme.colorScheme.onBackground)
        }else{
            val timeLyricRegex = """^\[(\d{2}:\d{2}\.\d{2})](.*)$""".toRegex(RegexOption.MULTILINE)
            val lyricListWithTimestamp = timeLyricRegex.findAll(lyrics)
                .map { matchResult ->
                    val (timeStr, lyric) = matchResult.destructured
                    parseLrcTime(timeStr) to lyric.trim()
                }
                .filter { it.second.isNotEmpty() } // 过滤空行
                .sortedBy { it.first } // 按时间排序
                .toList()

            val scrollState = rememberLazyListState()
            val hapticFeedback = LocalHapticFeedback.current
            
            // 计算当前行索引
            val currentIndex = remember(lyrics, currentPosition) {
                if (lyricListWithTimestamp.isEmpty()) {
                    0
                } else {
                    val index = lyricListWithTimestamp.binarySearch { it.first.compareTo(currentPosition) }
                    if (index >= 0) {
                        index
                    } else {
                        val insertionPoint = -index - 1
                        max(0, insertionPoint - 1)
                    }.coerceIn(0, lyricListWithTimestamp.size - 1)
                }
            }
            
            // 自动滚动到当前行，居中显示
            LaunchedEffect(currentIndex) {
                if (lyricListWithTimestamp.isNotEmpty()) {
                    // 平滑滚动到当前行，使其居中显示
                    scrollState.animateScrollToItem(
                        index = currentIndex,
                        scrollOffset = -scrollState.layoutInfo.viewportSize.height / 2
                    )
                }
            }
            
            // 恢复LazyColumn默认滚动行为，确保有明确的高度限制
            LazyColumn(
                state = scrollState,
                modifier = modifier
                    .fillMaxWidth()
                    .height(320.dp), // 设置固定高度，确保可以滚动
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(lyricListWithTimestamp) { index, (time, text) ->
                    val isCurrent = index == currentIndex
                    val itemModifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSeek(time)
                        }
                    
                    // 为歌词行添加动画效果
                    val animatedScale = remember {
                        Animatable(if (isCurrent) 1.1f else 1f)
                    }
                    
                    val animatedOpacity = remember {
                        Animatable(if (isCurrent) 1f else 0.7f)
                    }
                    
                    // 根据是否为当前行更新动画
                    LaunchedEffect(isCurrent) {
                        animatedScale.animateTo(
                            if (isCurrent) 1.1f else 1f,
                            animationSpec = AnimationConfig.SPRING_GENTLE
                        )
                        animatedOpacity.animateTo(
                            if (isCurrent) 1f else 0.7f,
                            animationSpec = AnimationConfig.SPRING_GENTLE
                        )
                    }
                    
                    Text(
                        text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isCurrent) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        },
                        style = if (isCurrent) {
                            MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        modifier = itemModifier
                            .graphicsLayer {
                                scaleX = animatedScale.value
                                scaleY = animatedScale.value
                                alpha = animatedOpacity.value
                            }
                    )
                }
            }
        }
    }
}
