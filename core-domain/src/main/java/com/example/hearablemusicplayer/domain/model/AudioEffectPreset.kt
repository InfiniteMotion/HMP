package com.example.hearablemusicplayer.domain.model

/**
 * 音效预设数据类
 * @param id 预设ID
 * @param name 预设名称
 * @param description 预设描述
 */
data class AudioEffectPreset(
    val id: Int,
    val name: String,
    val description: String
)
