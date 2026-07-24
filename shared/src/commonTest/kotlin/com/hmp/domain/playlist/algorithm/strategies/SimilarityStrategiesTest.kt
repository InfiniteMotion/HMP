package com.hmp.domain.playlist.algorithm.strategies

import com.hmp.domain.music.MusicLabel
import com.hmp.domain.enum.LabelCategory
import com.hmp.domain.enum.LabelName
import com.hmp.domain.playlist.AlgorithmType
import kotlin.test.Test
import kotlin.test.assertEquals

class OptimizedStrategyTest {

    private val strategy = OptimizedStrategy()

    @Test
    fun strategyName_isCorrect() {
        assertEquals("优化相似推荐", strategy.strategyName)
    }

    @Test
    fun algorithmType_isOptimizedSimilarity() {
        assertEquals(AlgorithmType.OPTIMIZED_SIMILARITY, strategy.algorithmType)
    }

    @Test
    fun calculateSimilarity_identicalLabels_returnsOne() {
        val labels = listOf(
            MusicLabel(1, LabelCategory.GENRE, LabelName.ROCK),
            MusicLabel(1, LabelCategory.MOOD, LabelName.ENERGETIC)
        )
        val result = strategy.calculateSimilarity(labels, labels)
        assertEquals(1.0, result, 0.001)
    }

    @Test
    fun calculateSimilarity_noOverlap_returnsZero() {
        val seed = listOf(MusicLabel(1, LabelCategory.GENRE, LabelName.ROCK))
        val candidate = listOf(MusicLabel(2, LabelCategory.GENRE, LabelName.JAZZ))
        val result = strategy.calculateSimilarity(seed, candidate)
        assertEquals(0.0, result)
    }

    @Test
    fun processParameters_noParams_defaultLimit() {
        val result = strategy.processParameters()
        assertEquals(50, result["limit"])
    }

    @Test
    fun processParameters_customLimit() {
        val result = strategy.processParameters(20)
        assertEquals(20, result["limit"])
    }
}

class ChainStrategyTest {

    private val strategy = ChainStrategy()

    @Test
    fun strategyName_isCorrect() {
        assertEquals("链式相似推荐", strategy.strategyName)
    }

    @Test
    fun algorithmType_isChainSimilarity() {
        assertEquals(AlgorithmType.CHAIN_SIMILARITY, strategy.algorithmType)
    }

    @Test
    fun calculateSimilarity_identicalLabels_returnsOne() {
        val labels = listOf(
            MusicLabel(1, LabelCategory.GENRE, LabelName.POP),
            MusicLabel(1, LabelCategory.MOOD, LabelName.HAPPY)
        )
        val result = strategy.calculateSimilarity(labels, labels)
        assertEquals(1.0, result, 0.001)
    }

    @Test
    fun processParameters_noParams_defaults() {
        val result = strategy.processParameters()
        assertEquals(30, result["limit"])
        assertEquals(30, result["maxChainLength"])
        assertEquals(false, result["excludeSeed"])
    }

    @Test
    fun processParameters_customParams() {
        val result = strategy.processParameters(10, 20, true)
        assertEquals(10, result["limit"])
        assertEquals(20, result["maxChainLength"])
        assertEquals(true, result["excludeSeed"])
    }

    @Test
    fun processParameters_partialParams_usesDefaults() {
        val result = strategy.processParameters(15)
        assertEquals(15, result["limit"])
        assertEquals(30, result["maxChainLength"])
        assertEquals(false, result["excludeSeed"])
    }
}