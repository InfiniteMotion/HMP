package com.hmp.domain.lyrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LrcParserTest {

    @Test
    fun parse_emptyInput_returnsEmptyList() {
        val result = LrcParser.parse("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun parse_blankInput_returnsEmptyList() {
        val result = LrcParser.parse("   \n  \n  ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun parse_singleLine_parsesCorrectly() {
        val lrc = "[00:12.34]Hello World"
        val result = LrcParser.parse(lrc)
        assertEquals(1, result.size)
        assertEquals(12340L, result[0].timestamp)
        assertEquals("Hello World", result[0].originalText)
    }

    @Test
    fun parse_millisTwoDigits_parsesCorrectly() {
        val lrc = "[00:05.50]Test"
        val result = LrcParser.parse(lrc)
        assertEquals(5500L, result[0].timestamp)
    }

    @Test
    fun parse_millisThreeDigits_parsesCorrectly() {
        val lrc = "[00:05.500]Test"
        val result = LrcParser.parse(lrc)
        assertEquals(5500L, result[0].timestamp)
    }

    @Test
    fun parse_multipleLines_sortedByTimestamp() {
        val lrc = "[00:10.00]Second\n[00:05.00]First\n[00:15.00]Third"
        val result = LrcParser.parse(lrc)
        assertEquals(3, result.size)
        assertEquals("First", result[0].originalText)
        assertEquals("Second", result[1].originalText)
        assertEquals("Third", result[2].originalText)
    }

    @Test
    fun parse_linesWithSameTimestamp_grouped() {
        val lrc = "[00:10.00]English Line\n[00:10.00]中文歌词"
        val result = LrcParser.parse(lrc)
        assertEquals(1, result.size)
        assertEquals(10000L, result[0].timestamp)
    }

    @Test
    fun parse_translatedLine_setsTranslatedText() {
        val lrc = "[00:10.00]Hello World\n[00:10.00]你好世界"
        val result = LrcParser.parse(lrc)
        assertEquals(1, result.size)
        assertEquals("Hello World", result[0].originalText)
        assertEquals("你好世界", result[0].translatedText)
    }

    @Test
    fun parse_emptyContentLine_skipped() {
        val lrc = "[00:05.00]\n[00:10.00]Valid"
        val result = LrcParser.parse(lrc)
        assertEquals(1, result.size)
        assertEquals("Valid", result[0].originalText)
    }

    @Test
    fun parse_metadataTags_ignored() {
        val lrc = "[ti:Song Title]\n[ar:Artist]\n[00:05.00]Lyrics"
        val result = LrcParser.parse(lrc)
        assertEquals(1, result.size)
        assertEquals("Lyrics", result[0].originalText)
    }

    @Test
    fun parse_longTimestamp_parsesCorrectly() {
        val lrc = "[05:30.00]Five thirty"
        val result = LrcParser.parse(lrc)
        assertEquals(330000L, result[0].timestamp)
    }
}

class FindCurrentLyricIndexTest {

    @Test
    fun emptyList_returnsNegativeOne() {
        val result = findCurrentLyricIndex(emptyList(), 5000L)
        assertEquals(-1, result)
    }

    @Test
    fun exactMatch_returnsCorrectIndex() {
        val lyrics = listOf(
            LyricLineData(1000L, "Line 1"),
            LyricLineData(5000L, "Line 2"),
            LyricLineData(10000L, "Line 3")
        )
        val result = findCurrentLyricIndex(lyrics, 5000L)
        assertEquals(1, result)
    }

    @Test
    fun betweenTimestamps_returnsPreviousIndex() {
        val lyrics = listOf(
            LyricLineData(1000L, "Line 1"),
            LyricLineData(5000L, "Line 2"),
            LyricLineData(10000L, "Line 3")
        )
        val result = findCurrentLyricIndex(lyrics, 7000L)
        assertEquals(1, result)
    }

    @Test
    fun beforeFirstTimestamp_returnsZero() {
        val lyrics = listOf(
            LyricLineData(1000L, "Line 1"),
            LyricLineData(5000L, "Line 2")
        )
        val result = findCurrentLyricIndex(lyrics, 500L)
        assertEquals(0, result)
    }

    @Test
    fun afterLastTimestamp_returnsLastIndex() {
        val lyrics = listOf(
            LyricLineData(1000L, "Line 1"),
            LyricLineData(5000L, "Line 2")
        )
        val result = findCurrentLyricIndex(lyrics, 99999L)
        assertEquals(1, result)
    }

    @Test
    fun singleLyric_alwaysReturnsZero() {
        val lyrics = listOf(LyricLineData(5000L, "Only line"))
        assertEquals(0, findCurrentLyricIndex(lyrics, 1000L))
        assertEquals(0, findCurrentLyricIndex(lyrics, 5000L))
        assertEquals(0, findCurrentLyricIndex(lyrics, 99999L))
    }
}

class DualLanguageAnalyzerTest {

    @Test
    fun analyzeSingleLine_pureEnglish_noTranslation() {
        val result = DualLanguageAnalyzer.analyzeSingleLine("Hello World")
        assertEquals("Hello World", result.first)
        assertNull(result.second)
    }

    @Test
    fun analyzeSingleLine_pureChinese_noTranslation() {
        val result = DualLanguageAnalyzer.analyzeSingleLine("你好世界")
        assertEquals("你好世界", result.first)
        assertNull(result.second)
    }

    @Test
    fun analyzeDualLines_englishAndChinese_separates() {
        val result = DualLanguageAnalyzer.analyzeDualLines("Hello World", "你好世界")
        assertEquals("Hello World", result.first)
        assertEquals("你好世界", result.second)
    }

    @Test
    fun analyzeDualLines_chineseAndEnglish_separates() {
        val result = DualLanguageAnalyzer.analyzeDualLines("你好世界", "Hello World")
        assertEquals("Hello World", result.first)
        assertEquals("你好世界", result.second)
    }

    @Test
    fun analyzeDualLines_sameLanguage_merged() {
        val result = DualLanguageAnalyzer.analyzeDualLines("Hello", "World")
        assertEquals("Hello World", result.first)
        assertNull(result.second)
    }

    @Test
    fun detectDominantLanguage_pureEnglish_returnsEnglish() {
        val result = DualLanguageAnalyzer.detectDominantLanguage("Hello World Test")
        assertEquals(Language.ENGLISH, result)
    }

    @Test
    fun detectDominantLanguage_pureChinese_returnsChinese() {
        val result = DualLanguageAnalyzer.detectDominantLanguage("你好世界测试")
        assertEquals(Language.CHINESE, result)
    }

    @Test
    fun detectDominantLanguage_empty_returnsUnknown() {
        val result = DualLanguageAnalyzer.detectDominantLanguage("")
        assertEquals(Language.UNKNOWN, result)
    }

    @Test
    fun detectDominantLanguage_mixed_returnsMixed() {
        val result = DualLanguageAnalyzer.detectDominantLanguage("Hello 你好世界测试")
        assertEquals(Language.MIXED, result)
    }
}

class MultiLineProcessorTest {

    @Test
    fun extractBestPair_emptyList_returnsEmpty() {
        val result = MultiLineProcessor.extractBestPair(emptyList())
        assertEquals("", result.first)
        assertNull(result.second)
    }

    @Test
    fun extractBestPair_singleLine_returnsLine() {
        val result = MultiLineProcessor.extractBestPair(listOf("Hello"))
        assertEquals("Hello", result.first)
        assertNull(result.second)
    }

    @Test
    fun extractBestPair_twoDifferentLines_analyzes() {
        val result = MultiLineProcessor.extractBestPair(listOf("Hello World", "你好世界"))
        assertEquals("Hello World", result.first)
        assertEquals("你好世界", result.second)
    }

    @Test
    fun extractBestPair_threeLines_findsBestPair() {
        val lines = listOf("Hello World", "你好世界", "Additional")
        val result = MultiLineProcessor.extractBestPair(lines)
        // Should find the English/Chinese pair
        assertTrue(result.second != null || result.first.contains("Hello"))
    }
}