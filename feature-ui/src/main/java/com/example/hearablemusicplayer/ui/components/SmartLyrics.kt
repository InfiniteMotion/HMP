package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearablemusicplayer.ui.R
import kotlin.math.max

/**
 * 智能歌词组件 - 一体化解决方案
 * 可直接替换原有的 Lyrics 组件
 */
@Composable
fun SmartLyrics(
    lyrics: String?,
    currentPosition: Long,
    modifier: Modifier = Modifier,
    onSeek: (Long) -> Unit = {}
) {
    var displayMode by remember { mutableStateOf(DisplayMode.DUAL) } // LANG1, LANG2, DUAL
    
    if (lyrics == null) {
        EmptyLyricsView(modifier)
        return
    }

    val parsedLyrics = remember(lyrics) { parseLyrics(lyrics) }
    val scrollState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    
    // 找到当前播放位置对应的歌词行
    val currentIndex by remember(parsedLyrics, currentPosition) {
        derivedStateOf {
            if (parsedLyrics.isEmpty()) {
                0
            } else {
                parsedLyrics.binarySearch { it.timestamp.compareTo(currentPosition) }.let { index ->
                    if (index >= 0) index else max(0, -index - 2)
                }.coerceIn(0, parsedLyrics.size - 1)
            }
        }
    }
    
    // 自动滚动到当前行
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0 && currentIndex < parsedLyrics.size) {
            scrollState.animateScrollToItem(
                index = currentIndex,
                scrollOffset = -300
            )
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 歌词区域
        Box(
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 120.dp)
            ) {
                itemsIndexed(
                    items = parsedLyrics,
                    key = { index, item -> "${item.timestamp}_${index}" }
                ) { index, lyricLine ->
                    val isCurrent = index == currentIndex

                    SmartLyricItem(
                        lyricLine = lyricLine,
                        isCurrent = isCurrent,
                        displayMode = displayMode,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSeek(lyricLine.timestamp)
                        }
                    )
                }
            }
            
            // 圆形语言切换按钮（仅在双语时显示）
            if (parsedLyrics.any { it.translatedText != null }) {
                FloatingActionButton(
                    onClick = { 
                        val nextMode = when (displayMode) {
                            DisplayMode.LANG1 -> DisplayMode.LANG2
                            DisplayMode.LANG2 -> DisplayMode.DUAL
                            DisplayMode.DUAL -> DisplayMode.LANG1
                        }
                        displayMode = nextMode
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 12.dp)
                        .size(40.dp),
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gallery_search_things),
                        contentDescription = "切换语言显示",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * 智能歌词项
 */
@Composable
private fun SmartLyricItem(
    lyricLine: LyricLineData,
    isCurrent: Boolean,
    displayMode: DisplayMode,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isCurrent) 1.05f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "LyricScale"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0.7f,
        animationSpec = tween(durationMillis = 200),
        label = "LyricAlpha"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrent) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "BackgroundColor"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedAlpha
            }
            .clickable { onClick() },
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (displayMode) {
                DisplayMode.LANG1 -> {
                    Text(
                        text = lyricLine.originalText,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = if (isCurrent) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DisplayMode.LANG2 -> {
                    if (lyricLine.translatedText != null) {
                        Text(
                            text = lyricLine.translatedText,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = if (isCurrent) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = lyricLine.originalText,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                DisplayMode.DUAL -> {
                    // 处理单行双语情况
                    if (lyricLine.translatedText == null) {
                        Text(
                            text = lyricLine.originalText,
                            textAlign = TextAlign.Center,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            style = if (isCurrent) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // 分开展示原文和译文
                        Text(
                            text = lyricLine.originalText,
                            textAlign = TextAlign.Center,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            style = if (isCurrent) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = lyricLine.translatedText,
                            textAlign = TextAlign.Center,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = if (isCurrent) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (isCurrent) 16.sp else 14.sp,
                            modifier = Modifier.fillMaxWidth()
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

// ==================== 内部数据结构和工具函数 ====================

/**
 * 显示模式枚举
 */
enum class DisplayMode {
    LANG1,  // 只显示语言一
    LANG2,  // 只显示语言二
    DUAL    // 双语显示
}

/**
 * 歌词行数据类
 */
data class LyricLineData(
    val timestamp: Long,
    val originalText: String,
    val translatedText: String? = null
)

/**
 * 智能歌词解析函数
 */
private fun parseLyrics(lrcText: String): List<LyricLineData> {
    if (lrcText.isBlank()) return emptyList()
    
    val timeRegex = """^\[(\d{2}):(\d{2})\.(\d{2,3})]""".toRegex()
    val lines = lrcText.lines()
    val parsedLines = mutableListOf<LyricLineData>()
    
    // 按时间戳分组
    val groupedLines = mutableMapOf<Long, MutableList<String>>()
    
    lines.forEach { line ->
        val match = timeRegex.find(line)
        if (match != null) {
            val (minutes, seconds, millis) = match.destructured
            val timestamp = minutes.toLong() * 60000 + 
                           seconds.toLong() * 1000 + 
                           millis.padEnd(3, '0').take(3).toLong()
            
            val content = line.substring(match.range.last + 1).trim()
            if (content.isNotEmpty()) {
                groupedLines.getOrPut(timestamp) { mutableListOf() }.add(content)
            }
        }
    }
    
    // 智能解析每组歌词
    groupedLines.toSortedMap().forEach { (timestamp, lineGroup) ->
        when (lineGroup.size) {
            1 -> {
                // 单行歌词 - 直接显示，不做双语处理
                parsedLines.add(LyricLineData(timestamp, lineGroup[0], null))
            }
            2 -> {
                // 两行歌词 - 智能判断是否为双语
                val (original, translated) = analyzeDualLineLyrics(lineGroup[0], lineGroup[1])
                parsedLines.add(LyricLineData(timestamp, original, translated))
            }
            else -> {
                // 多行歌词 - 选择最佳配对
                val (original, translated) = extractBestPair(lineGroup)
                parsedLines.add(LyricLineData(timestamp, original, translated))
            }
        }
    }
    
    return parsedLines
}

/**
 * 智能识别语言并配对
 */
private fun analyzeDualLineLyrics(text1: String, text2: String): Pair<String, String?> {
    detectLanguage(text1)
    detectLanguage(text2)
    
    // 统计两行歌词的整体语言分布
    val combinedText = "$text1 $text2"
    val chineseChars = combinedText.count { it.code in 0x4E00..0x9FFF }
    val englishChars = combinedText.count { it.isLetter() && it.code < 128 }
    val japaneseChars = combinedText.count { it.code in 0x3040..0x309F || it.code in 0x30A0..0x30FF }
    val koreanChars = combinedText.count { it.code in 0xAC00..0xD7AF }
    
    val totalLetters = chineseChars + englishChars + japaneseChars + koreanChars
    if (totalLetters == 0) {
        return Pair("$text1 $text2", null)
    }
    
    // 计算各语言占比
    val chineseRatio = chineseChars.toFloat() / totalLetters
    val englishRatio = englishChars.toFloat() / totalLetters
    val japaneseRatio = japaneseChars.toFloat() / totalLetters
    val koreanRatio = koreanChars.toFloat() / totalLetters
    
    // 设置双语判断阈值
    val threshold = 0.25f
    
    // 统计达到阈值的语言种类数
    var languageCount = 0
    if (chineseRatio >= threshold) languageCount++
    if (englishRatio >= threshold) languageCount++
    if (japaneseRatio >= threshold) languageCount++
    if (koreanRatio >= threshold) languageCount++
    
    // 如果存在两种及以上语言且占比合理，认为是双语
    if (languageCount >= 2) {
        // 确定主次语言
        val languages = listOf(
            "zh" to chineseRatio,
            "en" to englishRatio,
            "ja" to japaneseRatio,
            "ko" to koreanRatio
        ).filter { it.second >= threshold }.sortedByDescending { it.second }
        
        if (languages.size >= 2) {
            // 英文优先作为原文
            val (primaryLang, _) = languages.find { it.first == "en" } ?: languages[0]
            val (_, _) = if (primaryLang == "en") languages[1] else languages[0]
            
            return when (primaryLang) {
                "en" -> Pair(text1, text2)
                else -> Pair(text2, text1)
            }
        }
    }
    
    // 不满足双语条件，合并为单行
    return Pair("$text1 $text2", null)
}

/**
 * 从多行中提取最佳的原文-译文对
 */
private fun extractBestPair(lines: List<String>): Pair<String, String?> {
    if (lines.size < 2) return Pair(lines.firstOrNull() ?: "", null)
    
    // 简单策略：第一行原文，最后一行译文
    val firstLine = lines.first()
    val lastLine = lines.last()
    
    return if (firstLine != lastLine) {
        val (original, translated) = analyzeDualLineLyrics(firstLine, lastLine)
        Pair(original, translated)
    } else {
        Pair(firstLine, null)
    }
}

/**
 * 语言检测枚举
 */
private enum class Language {
    ENGLISH, CHINESE, JAPANESE, KOREAN, MIXED, UNKNOWN
}

/**
 * 简单语言检测
 */
private fun detectLanguage(text: String): Language {
    val chineseChars = text.count { it.code in 0x4E00..0x9FFF }
    val englishChars = text.count { it.isLetter() && it.code < 128 }
    val japaneseChars = text.count { it.code in 0x3040..0x309F || it.code in 0x30A0..0x30FF }
    val koreanChars = text.count { it.code in 0xAC00..0xD7AF }
    
    val totalLetters = chineseChars + englishChars + japaneseChars + koreanChars
    if (totalLetters == 0) return Language.UNKNOWN
    
    // 计算各语言字符占比
    val chineseRatio = chineseChars.toFloat() / totalLetters
    val englishRatio = englishChars.toFloat() / totalLetters
    val japaneseRatio = japaneseChars.toFloat() / totalLetters
    val koreanRatio = koreanChars.toFloat() / totalLetters
    
    // 设置识别阈值
    val threshold = 0.3f
    
    return when {
        chineseRatio >= threshold && chineseRatio > englishRatio && chineseRatio > japaneseRatio && chineseRatio > koreanRatio -> Language.CHINESE
        englishRatio >= threshold && englishRatio > chineseRatio && englishRatio > japaneseRatio && englishRatio > koreanRatio -> Language.ENGLISH
        japaneseRatio >= threshold && japaneseRatio > chineseRatio && japaneseRatio > englishRatio && japaneseRatio > koreanRatio -> Language.JAPANESE
        koreanRatio >= threshold && koreanRatio > chineseRatio && koreanRatio > englishRatio && koreanRatio > japaneseRatio -> Language.KOREAN
        else -> Language.MIXED
    }
}