package com.hmp.domain.playlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExtensionConfigTest {

    @Test
    fun defaultValues_correct() {
        val config = ExtensionConfig()
        assertEquals(10, config.minLength)
        assertEquals(0.25, config.qualityDropThreshold)
        assertEquals(2.5, config.maxExtensionFactor)
    }

    @Test
    fun getMaxExtendedLength_calculatesCorrectly() {
        val config = ExtensionConfig(minLength = 10, maxExtensionFactor = 2.5)
        assertEquals(25, config.getMaxExtendedLength())
    }

    @Test
    fun getMaxExtendedLength_conservative() {
        assertEquals(14, ExtensionConfig.CONSERVATIVE.getMaxExtendedLength())
    }

    @Test
    fun getMaxExtendedLength_balanced() {
        assertEquals(25, ExtensionConfig.BALANCED.getMaxExtendedLength())
    }

    @Test
    fun getMaxExtendedLength_aggressive() {
        assertEquals(45, ExtensionConfig.AGGRESSIVE.getMaxExtendedLength())
    }

    @Test
    fun minLength_zero_throws() {
        assertFailsWith<IllegalArgumentException> {
            ExtensionConfig(minLength = 0)
        }
    }

    @Test
    fun minLength_negative_throws() {
        assertFailsWith<IllegalArgumentException> {
            ExtensionConfig(minLength = -1)
        }
    }

    @Test
    fun qualityDropThreshold_belowZero_throws() {
        assertFailsWith<IllegalArgumentException> {
            ExtensionConfig(qualityDropThreshold = -0.1)
        }
    }

    @Test
    fun qualityDropThreshold_aboveOne_throws() {
        assertFailsWith<IllegalArgumentException> {
            ExtensionConfig(qualityDropThreshold = 1.1)
        }
    }

    @Test
    fun maxExtensionFactor_belowOne_throws() {
        assertFailsWith<IllegalArgumentException> {
            ExtensionConfig(maxExtensionFactor = 0.5)
        }
    }

    @Test
    fun toJson_serializesCorrectly() {
        val config = ExtensionConfig(minLength = 8, qualityDropThreshold = 0.2, maxExtensionFactor = 1.8)
        val json = config.toJson()
        assertEquals("""{"minLength":8,"qualityDropThreshold":0.2,"maxExtensionFactor":1.8}""", json)
    }

    @Test
    fun conservativePreset_values() {
        val c = ExtensionConfig.CONSERVATIVE
        assertEquals(8, c.minLength)
        assertEquals(0.2, c.qualityDropThreshold)
        assertEquals(1.8, c.maxExtensionFactor)
    }

    @Test
    fun aggressivePreset_values() {
        val a = ExtensionConfig.AGGRESSIVE
        assertEquals(15, a.minLength)
        assertEquals(0.3, a.qualityDropThreshold)
        assertEquals(3.0, a.maxExtensionFactor)
    }
}

class QualityMetricsTest {

    @Test
    fun empty_hasZeroValues() {
        val m = QualityMetrics.EMPTY
        assertEquals(0, m.totalCandidates)
        assertEquals(0.0, m.baselineScore)
        assertEquals(0, m.qualityDropPoint)
        assertEquals(0, m.maxRecommendedLength)
        assertEquals(0.0, m.averageScoreInExtendedRange)
    }

    @Test
    fun extensionRate_calculatesCorrectly() {
        val m = QualityMetrics(
            totalCandidates = 100,
            baselineScore = 0.8,
            qualityDropPoint = 25,
            maxRecommendedLength = 50,
            averageScoreInExtendedRange = 0.6
        )
        assertEquals(0.25, m.extensionRate)
    }

    @Test
    fun extensionRate_zeroCandidates_returnsZero() {
        val m = QualityMetrics(0, 0.0, 0, 0, 0.0)
        assertEquals(0.0, m.extensionRate)
    }

    @Test
    fun qualityRetentionRate_calculatesCorrectly() {
        val m = QualityMetrics(
            totalCandidates = 50,
            baselineScore = 0.8,
            qualityDropPoint = 20,
            maxRecommendedLength = 40,
            averageScoreInExtendedRange = 0.6
        )
        assertEquals(0.75, m.qualityRetentionRate, 0.001)
    }

    @Test
    fun qualityRetentionRate_zeroBaseline_returnsZero() {
        val m = QualityMetrics(50, 0.0, 20, 40, 0.6)
        assertEquals(0.0, m.qualityRetentionRate)
    }
}