package com.example.hearablemusicplayer.data.model

/**
 * AI 服务商类型枚举
 */
enum class AiProviderType(
    val displayName: String,
    val defaultModel: String,
    val defaultEndpoint: String
) {
    DEEPSEEK(
        displayName = "DeepSeek",
        defaultModel = "deepseek-chat",
        defaultEndpoint = "https://api.deepseek.com/v1/chat/completions"
    ),
    OPENAI(
        displayName = "OpenAI",
        defaultModel = "gpt-3.5-turbo",
        defaultEndpoint = "https://api.openai.com/v1/chat/completions"
    ),
    CLAUDE(
        displayName = "Claude",
        defaultModel = "claude-3-haiku-20240307",
        defaultEndpoint = "https://api.anthropic.com/v1/messages"
    ),
    QWEN(
        displayName = "通义千问",
        defaultModel = "qwen-turbo",
        defaultEndpoint = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"
    ),
    ERNIE(
        displayName = "文心一言",
        defaultModel = "ernie-bot-4",
        defaultEndpoint = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-bot-4"
    );

    companion object {
        /**
         * 根据名称获取服务商类型，默认返回 DEEPSEEK
         */
        fun fromName(name: String): AiProviderType {
            return entries.find { it.name == name } ?: DEEPSEEK
        }
    }
}

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
