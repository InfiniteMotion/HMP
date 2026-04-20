package com.hmp.domain.playlist.algorithm.core

import com.hmp.domain.music.MusicLabel
import com.hmp.domain.enum.LabelCategory


/**
 * 核心相似度计算器
 * 负责统一的标签相似度计算逻辑
 */
object SimilarityCalculator {
    
    /**
     * 计算加权标签相似度
     * @param seedLabels 种子音乐标签列表
     * @param candidateLabels 候选音乐标签列表
     * @param weights 权重配置
     * @return 标准化相似度分数 (0.0 - 1.0)
     */
    fun calculateWeightedSimilarity(
        seedLabels: List<MusicLabel>,
        candidateLabels: List<MusicLabel>,
        weights: Map<LabelCategory, Double>
    ): Double {
        if (seedLabels.isEmpty() || candidateLabels.isEmpty()) return 0.0
        
        // 直接使用传入的权重配置
        val effectiveWeights = weights
        
        var totalWeightedMatches = 0.0
        var totalPossibleWeight = 0.0
        
        // 按类别分组标签
        val seedLabelsByCategory = seedLabels.groupBy { it.type }
        val candidateLabelsByCategory = candidateLabels.groupBy { it.type }
        
        // 计算每个类别的相似度贡献
        effectiveWeights.forEach { (category, weight) ->
            val seedCategoryLabels = seedLabelsByCategory[category] ?: emptyList()
            val candidateCategoryLabels = candidateLabelsByCategory[category] ?: emptyList()
            
            if (seedCategoryLabels.isNotEmpty()) {
                totalPossibleWeight += weight * seedCategoryLabels.size
                
                // 计算匹配的标签数量
                val matchedLabels = seedCategoryLabels.count { seedLabel ->
                    candidateCategoryLabels.any { candidateLabel ->
                        seedLabel.label == candidateLabel.label
                    }
                }
                
                totalWeightedMatches += weight * matchedLabels
            }
        }
        
        // 返回标准化的相似度分数
        return if (totalPossibleWeight > 0) {
            totalWeightedMatches / totalPossibleWeight
        } else 0.0
    }
}