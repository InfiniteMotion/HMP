package com.hmp.domain.playlist.algorithm

import com.hmp.domain.music.Music
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.playlist.ExtensionConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveExtensionTruncationTest {

    private val truncation = AdaptiveExtensionTruncation()

    private fun musicInfo(id: Long) = MusicInfo(
        music = Music(id = id, title = "Song$id", artist = "A", album = "B", duration = 100, path = "/$id.mp3", albumArtUri = ""),
        extra = null, userInfo = null
    )

    // ===== calculateOptimalExtensionPoint =====

    @Test
    fun calculateOptimalExtensionPoint_emptyList_returnsZero() {
        val result = truncation.calculateOptimalExtensionPoint(emptyList<Pair<MusicInfo, Double>>(), ExtensionConfig.BALANCED)
        assertEquals(0, result)
    }

    @Test
    fun calculateOptimalExtensionPoint_smallerThanMinLength_returnsSize() {
        val scores = listOf(musicInfo(1) to 0.9, musicInfo(2) to 0.8)
        val config = ExtensionConfig(minLength = 5)
        val result = truncation.calculateOptimalExtensionPoint(scores, config)
        assertEquals(2, result)
    }

    @Test
    fun calculateOptimalExtensionPoint_stableScores_returnsAtLeastMinLength() {
        val scores = (1..20).map { musicInfo(it.toLong()) to 0.9 }
        val config = ExtensionConfig(minLength = 5, qualityDropThreshold = 0.5)
        val result = truncation.calculateOptimalExtensionPoint(scores, config)
        assertTrue(result >= 5)
    }

    @Test
    fun calculateOptimalExtensionPoint_droppingScores_returnsReasonableLength() {
        val scores = listOf(
            musicInfo(1) to 1.0,
            musicInfo(2) to 0.9,
            musicInfo(3) to 0.8,
            musicInfo(4) to 0.7,
            musicInfo(5) to 0.6,
            musicInfo(6) to 0.1,
            musicInfo(7) to 0.05
        )
        val config = ExtensionConfig(minLength = 3, qualityDropThreshold = 0.3)
        val result = truncation.calculateOptimalExtensionPoint(scores, config)
        assertTrue(result >= 3)
        assertTrue(result <= 7)
    }

    // ===== truncateWithAdaptiveExtension =====

    @Test
    fun truncateWithAdaptiveExtension_emptyInput_returnsEmpty() {
        val result = truncation.truncateWithAdaptiveExtension(emptyList<MusicInfo>(), emptyList<Double>(), ExtensionConfig.BALANCED)
        assertTrue(result.isEmpty())
    }

    @Test
    fun truncateWithAdaptiveExtension_mismatchedSizes_returnsEmpty() {
        val music = listOf(musicInfo(1))
        val scores = listOf(0.9, 0.8)
        val result = truncation.truncateWithAdaptiveExtension(music, scores, ExtensionConfig.BALANCED)
        assertTrue(result.isEmpty())
    }

    @Test
    fun truncateWithAdaptiveExtension_validInput_returnsNonEmpty() {
        val music = (1..10).map { musicInfo(it.toLong()) }
        val scores = (1..10).map { 1.0 - it * 0.05 }
        val result = truncation.truncateWithAdaptiveExtension(music, scores, ExtensionConfig(minLength = 3))
        assertTrue(result.isNotEmpty())
        assertTrue(result.size <= music.size)
    }

    // ===== analyzeQualityMetrics =====

    @Test
    fun analyzeQualityMetrics_emptyScores_returnsEmpty() {
        val result = truncation.analyzeQualityMetrics(emptyList<Double>(), ExtensionConfig.BALANCED)
        assertEquals(QualityMetrics.EMPTY, result)
    }

    @Test
    fun analyzeQualityMetrics_withScores_returnsNonEmpty() {
        val scores = listOf(1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1)
        val result = truncation.analyzeQualityMetrics(scores, ExtensionConfig.BALANCED)
        assertEquals(10, result.totalCandidates)
        assertTrue(result.baselineScore > 0.0)
        assertTrue(result.maxRecommendedLength > 0)
    }

    @Test
    fun qualityMetrics_extensionRate() {
        val metrics = QualityMetrics(10, 0.8, 5, 8, 0.6)
        assertEquals(0.5, metrics.extensionRate)
    }

    @Test
    fun qualityMetrics_qualityRetentionRate() {
        val metrics = QualityMetrics(10, 0.8, 5, 8, 0.6)
        assertEquals(0.75, metrics.qualityRetentionRate, 0.01)
    }

    @Test
    fun qualityMetrics_empty_baselineScoreZero() {
        val metrics = QualityMetrics.EMPTY
        assertEquals(0.0, metrics.baselineScore)
        assertEquals(0, metrics.totalCandidates)
    }
}
