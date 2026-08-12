package com.hmp.domain.lyrics

import kotlin.math.max

data class LyricLineData(
    val timestamp: Long,
    val originalText: String,
    val translatedText: String? = null,
    /** 原文逐字（卡拉 OK）时间片段；普通 LRC 为空，由 LyricsTimingGenerator 生成 */
    val charTimings: List<CharTiming> = emptyList(),
    /** 译文逐字时间片段；普通 LRC 为空，由 LyricsTimingGenerator 按同一行区间生成 */
    val translatedCharTimings: List<CharTiming> = emptyList()
)

fun findCurrentLyricIndex(parsedLyrics: List<LyricLineData>, currentPosition: Long): Int {
    if (parsedLyrics.isEmpty()) return -1
    return parsedLyrics.binarySearch { it.timestamp.compareTo(currentPosition) }.let { index ->
        if (index >= 0) index else max(0, -index - 2)
    }.coerceIn(0, parsedLyrics.size - 1)
}

object LrcParser {

    private val timeRegex = """^\[(\d{2}):(\d{2})\.(\d{2,3})]""".toRegex()
    private val inlineTimeRegex =
        """<(\d{2}):(\d{2})\.(\d{2,3})(?:,(\d{2}):(\d{2})\.(\d{2,3}))?>""".toRegex()

    /** 行内原始内容：文本 + 可能携带的逐字时间片段 */
    private data class RawLine(val text: String, val charTimings: List<CharTiming>)

    fun parse(lrcText: String): List<LyricLineData> {
        if (lrcText.isBlank()) return emptyList()

        val lines = lrcText.lines()
        val groupedLines = mutableMapOf<Long, MutableList<RawLine>>()

        lines.forEach { line ->
            val match = timeRegex.find(line) ?: return@forEach
            val timestamp = parseTime(
                minutes = match.groupValues[1],
                seconds = match.groupValues[2],
                millis = match.groupValues[3]
            )
            val content = line.substring(match.range.last + 1).trim()
            if (content.isNotEmpty()) {
                groupedLines.getOrPut(timestamp) { mutableListOf() }.add(parseTimedContent(content, timestamp))
            }
        }

        val parsedLines = mutableListOf<LyricLineData>()
        groupedLines.entries.sortedBy { it.key }.forEach { (timestamp, lineGroup) ->
            if (lineGroup.any { it.charTimings.isNotEmpty() }) {
                // 增强 LRC：拼接片段文本并保留逐字时间戳，不做双语拆分
                val text = lineGroup.joinToString(" ") { it.text }.trim()
                val timings = lineGroup.flatMap { it.charTimings }
                parsedLines.add(LyricLineData(timestamp, text, null, timings))
            } else when (lineGroup.size) {
                1 -> {
                    val line = lineGroup[0]
                    val result = DualLanguageAnalyzer.analyzeSingleLine(line.text)
                    parsedLines.add(LyricLineData(timestamp, result.first, result.second))
                }
                2 -> {
                    val result = DualLanguageAnalyzer.analyzeDualLines(lineGroup[0].text, lineGroup[1].text)
                    parsedLines.add(LyricLineData(timestamp, result.first, result.second))
                }
                else -> {
                    val result = MultiLineProcessor.extractBestPair(lineGroup.map { it.text })
                    parsedLines.add(LyricLineData(timestamp, result.first, result.second))
                }
            }
        }

        return parsedLines
    }

    private fun parseTime(minutes: String, seconds: String, millis: String): Long =
        minutes.toLong() * 60000 +
                seconds.toLong() * 1000 +
                millis.padEnd(3, '0').take(3).toLong()

    /**
     * 解析可能包含逐字时间戳的内容。支持：
     *  - `[mm:ss.xx]<mm:ss.xx>片段`（网易云/QQ 导出）
     *  - `[mm:ss.xx]<mm:ss.xx,mm:ss.xx>片段`（A2 显式区间）
     *  - `[mm:ss.xx][mm:ss.xx]片段`（双时间戳）
     *  - `[mm:ss.xx]片段<mm:ss.xx>`（结尾时间戳作为上一片段结束）
     * 无任何内联时间戳时返回普通文本（charTimings 为空）。
     */
    private fun parseTimedContent(content: String, lineTimestamp: Long): RawLine {
        val tokens = tokenize(content)
        if (tokens.none { it.isMarker }) return RawLine(content, emptyList())

        val segments = mutableListOf<CharTiming>()
        val textBuffer = StringBuilder()
        var segStart: Long? = null
        var segEnd: Long? = null

        fun flush(endMs: Long) {
            if (segStart != null) {
                val text = textBuffer.toString()
                if (text.isNotEmpty()) {
                    segments.add(CharTiming(text, segStart!!, segEnd ?: endMs))
                }
                textBuffer.clear()
                segStart = null
                segEnd = null
            }
        }

        tokens.forEach { token ->
            if (token.isMarker) {
                if (segStart == null) {
                    if (textBuffer.isNotEmpty()) {
                        // 标记前的文本属于从行时间戳开始的片段，此标记作为其结束
                        segments.add(CharTiming(textBuffer.toString(), lineTimestamp, token.startMs))
                        textBuffer.clear()
                    }
                    segStart = token.startMs
                    segEnd = token.endMs
                } else {
                    flush(token.startMs)
                    segStart = token.startMs
                    segEnd = token.endMs
                }
            } else {
                textBuffer.append(token.text)
            }
        }

        if (segStart != null && textBuffer.isNotEmpty()) {
            segments.add(CharTiming(textBuffer.toString(), segStart!!, segEnd ?: -1L))
        }

        if (segments.isEmpty()) return RawLine(content, emptyList())
        return RawLine(segments.joinToString("") { it.text }.trim(), segments)
    }

    private data class Token(
        val isMarker: Boolean,
        val text: String = "",
        val startMs: Long = 0L,
        val endMs: Long? = null
    )

    private fun tokenize(content: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val text = StringBuilder()
        var i = 0

        fun flushText() {
            if (text.isNotEmpty()) {
                tokens.add(Token(isMarker = false, text = text.toString()))
                text.clear()
            }
        }

        while (i < content.length) {
            val c = content[i]
            if (c == '<') {
                val match = inlineTimeRegex.find(content, i)
                if (match != null && match.range.first == i) {
                    flushText()
                    val groups = match.destructured
                    val start = parseTime(groups.component1(), groups.component2(), groups.component3())
                    val end = if (groups.component4().isNotEmpty()) {
                        parseTime(groups.component4(), groups.component5(), groups.component6())
                    } else {
                        null
                    }
                    tokens.add(Token(isMarker = true, startMs = start, endMs = end))
                    i = match.range.last + 1
                    continue
                }
            } else if (c == '[') {
                val match = timeRegex.find(content, i)
                if (match != null && match.range.first == i) {
                    flushText()
                    tokens.add(
                        Token(
                            isMarker = true,
                            startMs = parseTime(
                                minutes = match.groupValues[1],
                                seconds = match.groupValues[2],
                                millis = match.groupValues[3]
                            )
                        )
                    )
                    i = match.range.last + 1
                    continue
                }
            }
            text.append(c)
            i++
        }
        flushText()
        return tokens
    }
}

object DualLanguageAnalyzer {

    fun analyzeSingleLine(text: String): Pair<String, String?> {
        if (!mayContainDualLanguages(text)) return Pair(text, null)

        return when {
            containsClearLanguageBoundaries(text) -> splitByBoundaries(text)
            containsMixedScriptPattern(text) -> extractByScript(text)
            else -> Pair(text, null)
        }
    }

    fun analyzeDualLines(text1: String, text2: String): Pair<String, String?> {
        val lang1 = detectDominantLanguage(text1)
        val lang2 = detectDominantLanguage(text2)

        if (lang1 != lang2 && lang1 != Language.UNKNOWN && lang2 != Language.UNKNOWN) {
            return if (lang1 == Language.ENGLISH) {
                Pair(text1, text2)
            } else if (lang2 == Language.ENGLISH) {
                Pair(text2, text1)
            } else {
                if (text1 < text2) Pair(text1, text2) else Pair(text2, text1)
            }
        }

        return Pair("$text1 $text2", null)
    }

    private fun mayContainDualLanguages(text: String): Boolean {
        val chineseCount = text.count { it.code in 0x4E00..0x9FFF }
        val englishCount = text.count { it.isLetter() && it.code < 128 }
        val japaneseCount = text.count { it.code in 0x3040..0x309F || it.code in 0x30A0..0x30FF }
        val koreanCount = text.count { it.code in 0xAC00..0xD7AF }
        val cyrillicCount = text.count { it.code in 0x0400..0x04FF }
        val languageCounts = listOf(chineseCount, englishCount, japaneseCount, koreanCount, cyrillicCount)
        return languageCounts.count { it > 0 } >= 2
    }

    private fun containsClearLanguageBoundaries(text: String): Boolean {
        return "[\\u4e00-\\u9fff][a-zA-Z]|[a-zA-Z][\\u4e00-\\u9fff]".toRegex().containsMatchIn(text)
    }

    private fun splitByBoundaries(text: String): Pair<String, String?> {
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
        return text.any { it.code in 0x4E00..0x9FFF } &&
                text.any { it.isLetter() && it.code < 128 }
    }

    private fun extractByScript(text: String): Pair<String, String?> {
        val chineseChars = text.filter { it.code in 0x4E00..0x9FFF }
        val englishWords = text.split("\\s+".toRegex())
            .filter { it.all { char -> char.isLetter() && char.code < 128 } }
            .filter { it.length > 1 }
        val englishPart = englishWords.joinToString(" ")
        return if (englishPart.isNotBlank() && chineseChars.isNotBlank()) {
            Pair(englishPart, chineseChars)
        } else {
            Pair(text, null)
        }
    }

    fun detectDominantLanguage(text: String): Language {
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

object MultiLineProcessor {

    fun extractBestPair(lines: List<String>): Pair<String, String?> {
        if (lines.size < 2) return Pair(lines.firstOrNull() ?: "", null)

        val firstLine = lines.first()
        val lastLine = lines.last()

        if (firstLine != lastLine) {
            val result = DualLanguageAnalyzer.analyzeDualLines(firstLine, lastLine)
            if (result.second != null) return result
        }

        for (i in lines.indices) {
            for (j in i + 1 until lines.size) {
                val result = DualLanguageAnalyzer.analyzeDualLines(lines[i], lines[j])
                if (result.second != null) return result
            }
        }

        return Pair(lines.joinToString(" "), null)
    }
}

enum class Language {
    ENGLISH, CHINESE, JAPANESE, KOREAN, RUSSIAN, MIXED, UNKNOWN
}
