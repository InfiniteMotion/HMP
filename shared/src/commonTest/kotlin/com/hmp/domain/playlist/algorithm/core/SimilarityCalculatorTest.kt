package com.hmp.domain.playlist.algorithm.core

import com.hmp.domain.music.MusicLabel
import com.hmp.domain.enum.LabelCategory
import com.hmp.domain.enum.LabelName
import kotlin.test.Test
import kotlin.test.assertEquals

class SimilarityCalculatorTest {

    private val weights = mapOf(
        LabelCategory.GENRE to 3.0,
        LabelCategory.MOOD to 4.0,
        LabelCategory.SCENARIO to 2.0,
        LabelCategory.ERA to 1.0
    )

    @Test
    fun calculateWeightedSimilarity_emptySeed_returnsZero() {
        val result = SimilarityCalculator.calculateWeightedSimilarity(
            emptyList(),
            listOf(MusicLabel(1, LabelCategory.GENRE, LabelName.ROCK)),
            weights
        )
        assertEquals(0.0, result)
    }

    @Test
    fun calculateWeightedSimilarity_emptyCandidate_returnsZero() {
        val result = SimilarityCalculator.calculateWeightedSimilarity(
            listOf(MusicLabel(1, LabelCategory.GENRE, LabelName.ROCK)),
            emptyList(),
            weights
        )
        assertEquals(0.0, result)
    }

    @Test
    fun calculateWeightedSimilarity_bothEmpty_returnsZero() {
        val result = SimilarityCalculator.calculateWeightedSimilarity(
            emptyList(), emptyList(), weights
        )
        assertEquals(0.0, result)
    }

    @Test
    fun calculateWeightedSimilarity_identicalLabels_returnsOne() {
        val labels = listOf(
            MusicLabel(1, LabelCategory.GENRE, LabelName.ROCK),
            MusicLabel(1, LabelCategory.MOOD, LabelName.ENERGETIC)
        )
        val result = SimilarityCalculator.calculateWeightedSimilarity(labels, labels, weights)
        assertEquals(1.0, result, 0.001)
    }

    @Test
    fun calculateWeightedSimilarity_noOverlap_returnsZero() {
        val seed = listOf(
            MusicLabel(1, LabelCategory.GENRE, LabelName.ROCK),
            MusicLabel(1, LabelCategory.MOOD, LabelName.ENERGETIC)
        )
        val candidate = listOf(
            MusicLabel(2, LabelCategory.GENRE, LabelName.JAZZ),
            MusicLabel(2, LabelCategory.MOOD, LabelName.CALM)
        )
        val result = SimilarityCalculator.calculateWeightedSimilarity(seed, candidate, weights)
        assertEquals(0.0, result)
    }

    @Test
    fun calculateWeightedSimilarity_partialOverlap_returnsCorrectScore() {
        val seed = listOf(
            MusicLabel(1, LabelCategory.GENRE, LabelName.ROCK),
            MusicLabel(1, LabelCategory.MOOD, LabelName.ENERGETIC)
        )
        val candidate = listOf(
            MusicLabel(2, LabelCategory.GENRE, LabelName.ROCK),
            MusicLabel(2, LabelCategory.MOOD, LabelName.CALM)
        )
        val result = SimilarityCalculator.calculateWeightedSimilarity(seed, candidate, weights)
        // GENRE matches (weight 3.0), MOOD doesn't match (weight 4.0)
        // totalPossibleWeight = 3.0*1 + 4.0*1 = 7.0
        // totalWeightedMatches = 3.0*1 + 4.0*0 = 3.0
        // result = 3.0/7.0 ≈ 0.4286
        assertEquals(3.0 / 7.0, result, 0.001)
    }

    @Test
    fun calculateWeightedSimilarity_multipleLabelsInCategory_countedCorrectly() {
        val seed = listOf(
            MusicLabel(1, LabelCategory.GENRE, LabelName.ROCK),
            MusicLabel(1, LabelCategory.GENRE, LabelName.POP),
            MusicLabel(1, LabelCategory.MOOD, LabelName.HAPPY)
        )
        val candidate = listOf(
            MusicLabel(2, LabelCategory.GENRE, LabelName.ROCK),
            MusicLabel(2, LabelCategory.MOOD, LabelName.HAPPY)
        )
        val result = SimilarityCalculator.calculateWeightedSimilarity(seed, candidate, weights)
        // GENRE: seed has 2, candidate has 1 match → weight 3.0 * 1 match = 3.0, possible = 3.0 * 2 = 6.0
        // MOOD: seed has 1, candidate has 1 match → weight 4.0 * 1 = 4.0, possible = 4.0 * 1 = 4.0
        // total = 7.0 / 10.0 = 0.7
        assertEquals(7.0 / 10.0, result, 0.001)
    }

    @Test
    fun calculateWeightedSimilarity_emptyWeights_returnsZero() {
        val labels = listOf(MusicLabel(1, LabelCategory.GENRE, LabelName.ROCK))
        val result = SimilarityCalculator.calculateWeightedSimilarity(labels, labels, emptyMap())
        assertEquals(0.0, result)
    }
}

class WeightManagerTest {

    @Test
    fun getDefaultWeights_returnsBalancedWeights() {
        val weights = WeightManager.getDefaultWeights()
        assertEquals(4, weights.size)
        assertEquals(3.0, weights[LabelCategory.GENRE])
        assertEquals(4.0, weights[LabelCategory.MOOD])
        assertEquals(2.0, weights[LabelCategory.SCENARIO])
        assertEquals(1.0, weights[LabelCategory.ERA])
    }

    @Test
    fun convertWeightTemplate_balanced_matchesDefault() {
        val weights = WeightManager.convertWeightTemplate(
            com.hmp.domain.playlist.WeightTemplate.BALANCED
        )
        assertEquals(WeightManager.getDefaultWeights(), weights)
    }

    @Test
    fun convertWeightTemplate_genreFocus_genreHighest() {
        val weights = WeightManager.convertWeightTemplate(
            com.hmp.domain.playlist.WeightTemplate.GENRE_FOCUS
        )
        assertEquals(3.0, weights[LabelCategory.GENRE])
        assertTrue(weights[LabelCategory.GENRE]!! > weights[LabelCategory.MOOD]!!)
    }

    @Test
    fun convertWeightTemplate_moodFocus_moodHighest() {
        val weights = WeightManager.convertWeightTemplate(
            com.hmp.domain.playlist.WeightTemplate.MOOD_FOCUS
        )
        assertEquals(3.0, weights[LabelCategory.MOOD])
        assertTrue(weights[LabelCategory.MOOD]!! > weights[LabelCategory.GENRE]!!)
    }

    @Test
    fun convertWeightTemplate_scenarioFocus_scenarioHighest() {
        val weights = WeightManager.convertWeightTemplate(
            com.hmp.domain.playlist.WeightTemplate.SCENARIO_FOCUS
        )
        assertEquals(3.0, weights[LabelCategory.SCENARIO])
        assertTrue(weights[LabelCategory.SCENARIO]!! > weights[LabelCategory.GENRE]!!)
    }

    @Test
    fun convertWeightTemplate_eraFocus_eraHighest() {
        val weights = WeightManager.convertWeightTemplate(
            com.hmp.domain.playlist.WeightTemplate.ERA_FOCUS
        )
        assertEquals(3.0, weights[LabelCategory.ERA])
        assertTrue(weights[LabelCategory.ERA]!! > weights[LabelCategory.GENRE]!!)
    }

    private fun assertTrue(condition: Boolean) {
        kotlin.test.assertTrue(condition)
    }
}