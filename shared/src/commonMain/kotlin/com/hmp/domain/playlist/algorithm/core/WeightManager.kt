package com.hmp.domain.playlist.algorithm.core

import com.hmp.domain.enum.LabelCategory

import com.hmp.domain.playlist.WeightTemplate

/**
 * 权重管理器
 * 负责权重配置的统一管理和调整
 */
object WeightManager {

    fun convertWeightTemplate(template: WeightTemplate): Map<LabelCategory, Double> {
        return when (template) {
            WeightTemplate.BALANCED -> mapOf(
                LabelCategory.GENRE to 3.0,
                LabelCategory.MOOD to 4.0,
                LabelCategory.SCENARIO to 2.0,
                LabelCategory.ERA to 1.0
            )
            WeightTemplate.GENRE_FOCUS -> mapOf(
                LabelCategory.GENRE to 3.0,
                LabelCategory.MOOD to 1.0,
                LabelCategory.SCENARIO to 1.0,
                LabelCategory.ERA to 0.5
            )
            WeightTemplate.MOOD_FOCUS -> mapOf(
                LabelCategory.GENRE to 1.0,
                LabelCategory.MOOD to 3.0,
                LabelCategory.SCENARIO to 1.0,
                LabelCategory.ERA to 0.5
            )
            WeightTemplate.SCENARIO_FOCUS -> mapOf(
                LabelCategory.GENRE to 1.0,
                LabelCategory.MOOD to 1.0,
                LabelCategory.SCENARIO to 3.0,
                LabelCategory.ERA to 0.5
            )
            WeightTemplate.ERA_FOCUS -> mapOf(
                LabelCategory.GENRE to 1.0,
                LabelCategory.MOOD to 1.0,
                LabelCategory.SCENARIO to 1.0,
                LabelCategory.ERA to 3.0
            )
        }
    }

    fun getDefaultWeights(): Map<LabelCategory, Double> {
        return convertWeightTemplate(WeightTemplate.BALANCED)
    }
}