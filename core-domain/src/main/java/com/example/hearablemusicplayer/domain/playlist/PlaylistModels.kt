package com.example.hearablemusicplayer.domain.playlist

import com.example.hearablemusicplayer.domain.enum.LabelCategory
import com.example.hearablemusicplayer.domain.enum.LabelName

/**
 * 播放列表生成相关数据模型集合
 */

/**
 * 播放列表生成算法类型枚举
 */
enum class AlgorithmType {
    /**
     * 优化相似推荐算法
     * 基于多维度标签权重的智能相似度匹配
     */
    OPTIMIZED_SIMILARITY,
    
    /**
     * 链式相似推荐算法
     * DFS风格的连续相似音乐发现
     */
    CHAIN_SIMILARITY
}

/**
 * 标签优先级配置数据模型
 * 用于标签优先级推荐算法的配置
 */
data class PriorityTag(
    /**
     * 标签类别
     */
    val category: LabelCategory,
    
    /**
     * 具体标签名称（可选）
     * 如果为null，则表示该类别下的所有标签
     */
    val specificLabel: LabelName? = null,
    
    /**
     * 优先级权重
     * 数值越大优先级越高
     */
    val priorityWeight: Int
) {
    
    companion object {
        /**
         * 创建类别级别的优先级配置
         */
        fun categoryPriority(category: LabelCategory, weight: Int): PriorityTag {
            return PriorityTag(category = category, priorityWeight = weight)
        }
        
        /**
         * 创建具体标签的优先级配置
         */
        fun specificTagPriority(category: LabelCategory, label: LabelName, weight: Int): PriorityTag {
            return PriorityTag(category = category, specificLabel = label, priorityWeight = weight)
        }
    }
}

/**
 * 自适应扩展配置数据模型
 * 控制播放列表自适应截断的行为参数
 */
data class ExtensionConfig(
    /**
     * 最小推荐数量
     * 保证至少返回的音乐数量
     */
    val minLength: Int = 10,
    
    /**
     * 质量下降阈值
     * 当相似度下降超过此比例时考虑截断
     */
    val qualityDropThreshold: Double = 0.25,
    
    /**
     * 最大扩展倍数
     * 相对于最小长度的最大扩展倍数
     */
    val maxExtensionFactor: Double = 2.5
) {
    
    init {
        require(minLength > 0) { "最小长度必须大于0" }
        require(qualityDropThreshold in 0.0..1.0) { "质量下降阈值必须在0.0-1.0之间" }
        require(maxExtensionFactor >= 1.0) { "最大扩展倍数必须大于等于1.0" }
    }
    
    /**
     * 计算最大扩展长度
     */
    fun getMaxExtendedLength(): Int {
        return (minLength * maxExtensionFactor).toInt()
    }
    
    companion object {
        /**
         * 保守配置 - 较短但高质量的推荐列表
         */
        val CONSERVATIVE = ExtensionConfig(
            minLength = 8,
            qualityDropThreshold = 0.2,
            maxExtensionFactor = 1.8
        )
        
        /**
         * 平衡配置 - 默认推荐设置
         */
        val BALANCED = ExtensionConfig(
            minLength = 10,
            qualityDropThreshold = 0.25,
            maxExtensionFactor = 2.5
        )
        
        /**
         * 激进配置 - 更长的推荐列表
         */
        val AGGRESSIVE = ExtensionConfig(
            minLength = 15,
            qualityDropThreshold = 0.3,
            maxExtensionFactor = 3.0
        )
    }
}

/**
 * 推荐质量分析指标数据类
 */
data class QualityMetrics(
    val totalCandidates: Int,
    val baselineScore: Double,
    val qualityDropPoint: Int,
    val maxRecommendedLength: Int,
    val averageScoreInExtendedRange: Double
) {
    companion object {
        val EMPTY = QualityMetrics(0, 0.0, 0, 0, 0.0)
    }
    
    /**
     * 扩展率
     */
    val extensionRate: Double
        get() = if (totalCandidates > 0) {
            qualityDropPoint.toDouble() / totalCandidates
        } else 0.0
    
    /**
     * 质量保持率
     */
    val qualityRetentionRate: Double
        get() = if (baselineScore > 0 && averageScoreInExtendedRange > 0) {
            averageScoreInExtendedRange / baselineScore
        } else 0.0
}

data class Playlist(
    val id: Long = 0,
    val name: String
)

data class PlaylistItem(
    val songUrl: String,
    val songId: Long,
    val playlistId: Long,
)