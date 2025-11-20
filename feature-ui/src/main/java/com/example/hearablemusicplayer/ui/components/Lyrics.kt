package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
){
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (lyrics==null) {
            Text("未识别到歌词")
        }else{
            val timeLyricRegex = """^\[(\d{2}:\d{2}\.\d{2})](.*)$""".toRegex(RegexOption.MULTILINE)
            val lyricListWithTimestamp = timeLyricRegex.findAll(lyrics)
                .map { matchResult ->
                    val (timeStr, lyric) = matchResult.destructured
                    parseLrcTime(timeStr) to lyric.trim()
                }
                .sortedBy { it.first } // 按时间排序
                .toList()

            val scrollState = rememberLazyListState()
            val currentIndex = remember(lyrics, currentPosition) {
                lyricListWithTimestamp.binarySearch { it.first.compareTo(currentPosition) }
                    .let { if (it >= 0) it else -it - 2 }
                    .coerceIn(0, max(0, lyricListWithTimestamp.size - 1))
            }
            // 自动滚动到当前行
            LaunchedEffect(currentIndex) {
                if (lyrics.isNotEmpty()) {
                    scrollState.animateScrollToItem(
                        index = currentIndex,
                        scrollOffset = -300 // 向上偏移，使高亮行位于中间区域
                    )
                }
            }
            LazyColumn(
                state = scrollState,
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(lyricListWithTimestamp) { (time, text) ->
                    val isCurrent = lyricListWithTimestamp[currentIndex].first == time
                    Modifier
                        .padding(vertical = 16.dp)
                    Text(
                        text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                        style = if (isCurrent) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
