package com.example.hearablemusicplayer.domain.model

/**
 * 均衡器频段数据类
 * @param bandIndex 频段索引
 * @param frequency 频段中心频率（Hz）
 * @param level 当前增益值（dB）
 * @param minLevel 最小增益值（dB）
 * @param maxLevel 最大增益值（dB）
 */
data class EqualizerBand(
    val bandIndex: Int,
    val frequency: Int,
    val level: Float,
    val minLevel: Int,
    val maxLevel: Int
)
