package com.example.hearablemusicplayer.domain.playlist.algorithm.strategies

import com.example.hearablemusicplayer.domain.music.MusicLabel
import com.example.hearablemusicplayer.domain.enum.LabelCategory
import com.example.hearablemusicplayer.domain.playlist.AlgorithmType

import com.example.hearablemusicplayer.domain.playlist.algorithm.core.SimilarityCalculator
import com.example.hearablemusicplayer.domain.playlist.algorithm.core.WeightManager

/**
 * 相似度计算策略接口
 */
interface SimilarityStrategy {
    val strategyName: String
    val description: String
    val algorithmType: AlgorithmType
    val baseWeights: Map<LabelCategory, Double>
    
    fun calculateSimilarity(
        seedLabels: List<MusicLabel>,
        candidateLabels: List<MusicLabel>
    ): Double
    
    fun processParameters(vararg params: Any): Map<String, Any>
}

/**
 * 优化相似推荐策略
 */
class OptimizedStrategy : SimilarityStrategy {
    override val strategyName: String = "优化相似推荐"
    override val description: String = "基于多维度标签权重的智能相似度匹配"
    override val algorithmType: AlgorithmType = AlgorithmType.OPTIMIZED_SIMILARITY
    override val baseWeights: Map<LabelCategory, Double> = WeightManager.getDefaultWeights()
    
    override fun calculateSimilarity(
        seedLabels: List<MusicLabel>,
        candidateLabels: List<MusicLabel>
    ): Double {
        return SimilarityCalculator.calculateWeightedSimilarity(
            seedLabels, candidateLabels, baseWeights
        )
    }
    
    override fun processParameters(vararg params: Any): Map<String, Any> {
        return mapOf(
            "limit" to (params.firstOrNull() as? Int ?: 50)
        )
    }
}

/**
 * 链式相似推荐策略
 */
class ChainStrategy : SimilarityStrategy {
    override val strategyName: String = "链式相似推荐"
    override val description: String = "从种子音乐开始逐个寻找最相似的下一首，避免重复选择"
    override val algorithmType: AlgorithmType = AlgorithmType.CHAIN_SIMILARITY
    override val baseWeights: Map<LabelCategory, Double> = WeightManager.getDefaultWeights()
    
    override fun calculateSimilarity(
        seedLabels: List<MusicLabel>,
        candidateLabels: List<MusicLabel>
    ): Double {
        return SimilarityCalculator.calculateWeightedSimilarity(
            seedLabels, candidateLabels, baseWeights
        )
    }
    
    override fun processParameters(vararg params: Any): Map<String, Any> {
        return mapOf(
            "limit" to (params.firstOrNull() as? Int ?: 30),
            "maxChainLength" to (params.getOrNull(1) as? Int ?: 30),
            "excludeSeed" to (params.getOrNull(2) as? Boolean ?: false)
        )
    }
}