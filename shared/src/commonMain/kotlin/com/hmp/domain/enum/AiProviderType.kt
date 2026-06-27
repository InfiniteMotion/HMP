package com.hmp.domain.enum

/**
 * AI 预设端点（OpenAI 兼容格式）
 * 仅作为快捷填入的预设值，实际调用统一走 OpenAI 兼容接口
 */
object AiPresetEndpoints {
    data class Preset(
        val displayName: String,
        val endpoint: String,
        val defaultModel: String
    )

    val DEEPSEEK = Preset(
        displayName = "DeepSeek",
        endpoint = "https://api.deepseek.com/v1",
        defaultModel = "deepseek-chat"
    )

    val OPENAI = Preset(
        displayName = "OpenAI",
        endpoint = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini"
    )

    val ALL = listOf(DEEPSEEK, OPENAI)
}
