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
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.max

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
    displayMode: DisplayMode = DisplayMode.DUAL,
    alignment: LyricsAlignment = LyricsAlignment.CENTER
) {
    if (lyrics == null) {
        EmptyLyricsView(modifier)
        return
    }

    val parsedLyrics = remember(lyrics) { EnhancedLyricsParser.parse(lyrics) }
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
        derivedStateOf {
            if (parsedLyrics.isEmpty()) {
                -1
            } else {
                parsedLyrics.binarySearch { it.timestamp.compareTo(currentPosition) }.let { index ->
                    if (index >= 0) index else max(0, -index - 2)
                }.coerceIn(0, parsedLyrics.size - 1)
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerHeightPx = it.height }
    ) {
        // 自动滚动到当前行（居中）
        LaunchedEffect(currentIndex, containerHeightPx) {
            if (currentIndex >= 0 && currentIndex < parsedLyrics.size && containerHeightPx > 0) {
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
                items = parsedLyrics,
                key = { index, item -> "${item.timestamp}_${index}" }
            ) { index, lyricLine ->
                    val isCurrent = index == currentIndex && currentIndex >= 0
                AdvancedLyricItem(
                    lyricLine = lyricLine,
                    isCurrent = isCurrent,
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

    val lineSpacing = ((if (lyricLine.translatedText != null) 2 else 1) * lineSpacing).dp

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
                    Text(
                        text = lyricLine.originalText,
                        textAlign = textAlign,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = originalTextSize.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                    )
                }
                DisplayMode.LANG2 -> {
                    if (lyricLine.translatedText != null) {
                        Text(
                            text = lyricLine.translatedText,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = translatedTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                        )
                    } else {
                        Text(
                            text = lyricLine.originalText,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = originalTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                        )
                    }
                }
                DisplayMode.DUAL -> {
                    if (lyricLine.translatedText == null) {
                        Text(
                            text = lyricLine.originalText,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = if (isCurrent) currentTimeTextSize.sp else originalTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                        )
                    } else {
                        Text(
                            text = lyricLine.originalText,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = if (isCurrent) currentTimeTextSize.sp else originalTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lyricLine.translatedText,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = translatedTextSize.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
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

/**
 * 歌词行数据类
 */
data class LyricLineData(
    val timestamp: Long,
    val originalText: String,
    val translatedText: String? = null
)

// ==================== 增强版歌词解析器 ====================

/**
 * 增强版歌词解析器 - 全新重构的核心逻辑
 */
object EnhancedLyricsParser {
    
    /**
     * 解析歌词文本为核心数据结构
     */
    fun parse(lrcText: String): List<LyricLineData> {
        if (lrcText.isBlank()) return emptyList()
        
        val timeRegex = """^\[(\d{2}):(\d{2})\.(\d{2,3})]""".toRegex()
        val lines = lrcText.lines()
        val parsedLines = mutableListOf<LyricLineData>()
        
        // 按时间戳分组，保持每行独立
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
        
        // 使用增强的解析逻辑处理每组歌词
        groupedLines.toSortedMap().forEach { (timestamp, lineGroup) ->
            when (lineGroup.size) {
                1 -> {
                    // 单行处理 - 使用智能双语检测
                    val line = lineGroup[0]
                    val result = DualLanguageAnalyzer.analyzeSingleLine(line)
                    parsedLines.add(LyricLineData(timestamp, result.first, result.second))
                }
                2 -> {
                    // 双行处理 - 智能语言配对
                    val result = DualLanguageAnalyzer.analyzeDualLines(lineGroup[0], lineGroup[1])
                    parsedLines.add(LyricLineData(timestamp, result.first, result.second))
                }
                else -> {
                    // 多行处理 - 最佳配对选择
                    val result = MultiLineProcessor.extractBestPair(lineGroup)
                    parsedLines.add(LyricLineData(timestamp, result.first, result.second))
                }
            }
        }
        
        return parsedLines
    }
}

/**
 * 双语分析器 - 核心智能分析逻辑
 */
object DualLanguageAnalyzer {
    
    /**
     * 分析单行内容是否包含双语并进行拆分
     */
    fun analyzeSingleLine(text: String): Pair<String, String?> {
        // 快速检测是否可能包含双语
        if (!mayContainDualLanguages(text)) {
            return Pair(text, null)
        }
        
        // 使用多层次分析策略
        return when {
            containsClearLanguageBoundaries(text) -> {
                splitByBoundaries(text)
            }
            containsMixedScriptPattern(text) -> {
                extractByScript(text)
            }
            else -> {
                Pair(text, null)
            }
        }
    }
    
    /**
     * 分析双行内容的语言配对关系
     */
    fun analyzeDualLines(text1: String, text2: String): Pair<String, String?> {
        val lang1 = detectDominantLanguage(text1)
        val lang2 = detectDominantLanguage(text2)
        
        // 如果两行是不同语言，判定为双语
        if (lang1 != lang2 && lang1 != Language.UNKNOWN && lang2 != Language.UNKNOWN) {
            // 英文优先作为原文
            return if (lang1 == Language.ENGLISH) {
                Pair(text1, text2)
            } else if (lang2 == Language.ENGLISH) {
                Pair(text2, text1)
            } else {
                // 其他语言组合，按字典序决定
                if (text1 < text2) Pair(text1, text2) else Pair(text2, text1)
            }
        }
        
        // 合并非双语内容
        return Pair("$text1 $text2", null)
    }
    
    // 私有辅助方法
    private fun mayContainDualLanguages(text: String): Boolean {
        val chineseCount = text.count { it.code in 0x4E00..0x9FFF }
        val englishCount = text.count { it.isLetter() && it.code < 128 }
        val japaneseCount = text.count { it.code in 0x3040..0x309F || it.code in 0x30A0..0x30FF }
        val koreanCount = text.count { it.code in 0xAC00..0xD7AF }
        val cyrillicCount = text.count { it.code in 0x0400..0x04FF }
        
        val languageCounts = listOf(chineseCount, englishCount, japaneseCount, koreanCount, cyrillicCount)
        val nonZeroLanguages = languageCounts.count { it > 0 }
        
        return nonZeroLanguages >= 2
    }
    
    private fun containsClearLanguageBoundaries(text: String): Boolean {
        // 检测明显的语言切换边界
        return "[\\u4e00-\\u9fff][a-zA-Z]|[a-zA-Z][\\u4e00-\\u9fff]".toRegex().containsMatchIn(text)
    }
    
    private fun splitByBoundaries(text: String): Pair<String, String?> {
        // 按Unicode脚本边界拆分
        val chinesePart = text.filter { it.code in 0x4E00..0x9FFF }
        val englishPart = "\\b[a-zA-Z]+(?:'[a-zA-Z]+)*\\b".toRegex()
            .findAll(text).joinToString(" ") { it.value }

        return if (chinesePart.isNotBlank() && englishPart.isNotBlank()) {
            Pair(englishPart, chinesePart)
        } else {
            Pair(text, null)
        }
    }
    
    private fun containsMixedScriptPattern(text: String): Boolean {
        // 检测混合脚本模式
        return text.any { it.code in 0x4E00..0x9FFF } && 
               text.any { it.isLetter() && it.code < 128 }
    }
    
    private fun extractByScript(text: String): Pair<String, String?> {
        // 基于脚本类型的提取
        val chineseChars = text.filter { it.code in 0x4E00..0x9FFF }
        val japaneseChars = text.filter { it.code in 0x3040..0x309F || it.code in 0x30A0..0x30FF }
        val koreanChars = text.filter { it.code in 0xAC00..0xD7AF }
        val cyrillicChars = text.filter { it.code in 0x0400..0x04FF }
        
        val englishWords = text.split("\\s+".toRegex())
            .filter { it.all { char -> char.isLetter() && char.code < 128 } }
            .filter { it.length > 1 }
        
        val englishPart = englishWords.joinToString(" ")
        
        // 按优先级组合非拉丁字符
        val nonLatinPart = buildString {
            append(chineseChars)
            append(japaneseChars)
            append(koreanChars)
            append(cyrillicChars)
        }
        
        return if (englishPart.isNotBlank() && nonLatinPart.isNotBlank()) {
            Pair(englishPart, nonLatinPart)
        } else {
            Pair(text, null)
        }
    }
    
    private fun detectDominantLanguage(text: String): Language {
        val chineseChars = text.count { it.code in 0x4E00..0x9FFF }
        val englishChars = text.count { it.isLetter() && it.code < 128 }
        val japaneseChars = text.count { it.code in 0x3040..0x309F || it.code in 0x30A0..0x30FF }
        val koreanChars = text.count { it.code in 0xAC00..0xD7AF }
        val cyrillicChars = text.count { it.code in 0x0400..0x04FF }
        
        val totalChars = chineseChars + englishChars + japaneseChars + koreanChars + cyrillicChars
        
        if (totalChars == 0) return Language.UNKNOWN
        
        val chineseRatio = chineseChars.toFloat() / totalChars
        val englishRatio = englishChars.toFloat() / totalChars
        val japaneseRatio = japaneseChars.toFloat() / totalChars
        val koreanRatio = koreanChars.toFloat() / totalChars
        val cyrillicRatio = cyrillicChars.toFloat() / totalChars
        
        return when {
            chineseRatio > 0.6 -> Language.CHINESE
            englishRatio > 0.6 -> Language.ENGLISH
            japaneseRatio > 0.6 -> Language.JAPANESE
            koreanRatio > 0.6 -> Language.KOREAN
            cyrillicRatio > 0.6 -> Language.RUSSIAN
            else -> Language.MIXED
        }
    }
}

/**
 * 多行处理器 - 处理三行及以上的歌词
 */
object MultiLineProcessor {
    
    /**
     * 从多行中提取最佳的原文-译文对
     */
    fun extractBestPair(lines: List<String>): Pair<String, String?> {
        if (lines.size < 2) return Pair(lines.firstOrNull() ?: "", null)
        
        // 策略1: 第一行原文，最后一行译文
        val firstLine = lines.first()
        val lastLine = lines.last()
        
        if (firstLine != lastLine) {
            val result = DualLanguageAnalyzer.analyzeDualLines(firstLine, lastLine)
            if (result.second != null) {
                return result
            }
        }
        
        // 策略2: 寻找最可能的双语配对
        for (i in lines.indices) {
            for (j in i + 1 until lines.size) {
                val result = DualLanguageAnalyzer.analyzeDualLines(lines[i], lines[j])
                if (result.second != null) {
                    return result
                }
            }
        }
        
        // 策略3: 合并所有行作为单语内容
        return Pair(lines.joinToString(" "), null)
    }
}

/**
 * 语言枚举
 */
enum class Language {
    ENGLISH, CHINESE, JAPANESE, KOREAN, RUSSIAN, MIXED, UNKNOWN
}