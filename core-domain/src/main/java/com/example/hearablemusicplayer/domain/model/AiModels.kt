package com.example.hearablemusicplayer.domain.model

import com.example.hearablemusicplayer.domain.model.enum.AiProviderType

/**
 * AI 服务商配置数据类
 */
data class AiProviderConfig(
    val type: AiProviderType,
    val apiKey: String = "",
    val model: String = "",
    val isConfigured: Boolean = false
) {
    /**
     * 获取实际使用的模型名称（如果用户未设置则使用默认值）
     */
    fun getEffectiveModel(): String {
        return model.ifBlank { type.defaultModel }
    }
}

data class DailyMusicInfo(
    val genre: List<String>,
    val mood: List<String>,
    val scenario:List<String>,
    val language:String,
    val era:String,
    val rewards:String,
    val lyric:String,
    val singerIntroduce:String,
    val backgroundIntroduce:String,
    val description:String,
    val relevantMusic:String,
    var errorInfo:String
)
