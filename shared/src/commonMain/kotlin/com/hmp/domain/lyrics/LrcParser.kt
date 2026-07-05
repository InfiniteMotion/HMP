package com.hmp.domain.lyrics

import kotlin.math.max

data class LyricLineData(
    val timestamp: Long,
    val originalText: String,
    val translatedText: String? = null
)

fun findCurrentLyricIndex(parsedLyrics: List<LyricLineData>, currentPosition: Long): Int {
    if (parsedLyrics.isEmpty()) return -1
    return parsedLyrics.binarySearch { it.timestamp.compareTo(currentPosition) }.let { index ->
        if (index >= 0) index else max(0, -index - 2)
    }.coerceIn(0, parsedLyrics.size - 1)
}

object LrcParser {

    fun parse(lrcText: String): List<LyricLineData> {
        if (lrcText.isBlank()) return emptyList()

        val timeRegex = """^\[(\d{2}):(\d{2})\.(\d{2,3})]""".toRegex()
        val lines = lrcText.lines()
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

        val parsedLines = mutableListOf<LyricLineData>()
        groupedLines.entries.sortedBy { it.key }.forEach { (timestamp, lineGroup) ->
            when (lineGroup.size) {
                1 -> {
                    val line = lineGroup[0]
                    val result = DualLanguageAnalyzer.analyzeSingleLine(line)
                    parsedLines.add(LyricLineData(timestamp, result.first, result.second))
                }
                2 -> {
                    val result = DualLanguageAnalyzer.analyzeDualLines(lineGroup[0], lineGroup[1])
                    parsedLines.add(LyricLineData(timestamp, result.first, result.second))
                }
                else -> {
                    val result = MultiLineProcessor.extractBestPair(lineGroup)
                    parsedLines.add(LyricLineData(timestamp, result.first, result.second))
                }
            }
        }

        return parsedLines
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
