package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (lyrics == null) {
            Text(
                text = "未识别到歌词",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        } else {
            val timeLyricRegex = """^\[(\d{2}:\d{2}\.\d{2})](.*)$""".toRegex(RegexOption.MULTILINE)
            val lyricListWithTimestamp = remember(lyrics) {
                timeLyricRegex.findAll(lyrics)
                    .map { matchResult ->
                        val (timeStr, lyric) = matchResult.destructured
                        parseLrcTime(timeStr) to lyric.trim()
                    }
                    .filter { it.second.isNotEmpty() }
                    .sortedBy { it.first }
                    .toList()
            }

            val scrollState = rememberLazyListState()
            val hapticFeedback = LocalHapticFeedback.current
            
            val currentIndex = remember(lyricListWithTimestamp, currentPosition) {
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
            
            LaunchedEffect(currentIndex) {
                if (lyricListWithTimestamp.isNotEmpty()) {
                    scrollState.animateScrollToItem(
                        index = currentIndex,
                        scrollOffset = -300 // Use a fixed offset for better centering
                    )
                }
            }
            
            LazyColumn(
                state = scrollState,
                modifier = modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 120.dp) // Add padding to allow centering first/last lines
            ) {
                itemsIndexed(
                    items = lyricListWithTimestamp,
                    key = { index, item -> "${item.first}_$index" } // Combine timestamp and index for uniqueness
                ) { index, (time, text) ->
                    val isCurrent = index == currentIndex
                    
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isCurrent) 1.15f else 1f,
                        animationSpec = AnimationConfig.SPRING_MEDIUM,
                        label = "LyricScale"
                    )
                    
                    val animatedOpacity by animateFloatAsState(
                        targetValue = if (isCurrent) 1f else 0.4f,
                        animationSpec = AnimationConfig.SPRING_MEDIUM,
                        label = "LyricOpacity"
                    )

                    val animatedColor by animateColorAsState(
                        targetValue = if (isCurrent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        },
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        label = "LyricColor"
                    )
                    
                    Text(
                        text = text,
                        textAlign = TextAlign.Center,
                        color = animatedColor,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                            lineHeight = 32.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                                alpha = animatedOpacity
                            }
                            .clickable {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSeek(time)
                            }
                    )
                }
            }
        }
    }
}
