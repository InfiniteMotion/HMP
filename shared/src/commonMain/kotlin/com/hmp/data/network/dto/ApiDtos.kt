package com.hmp.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ========== OpenAI Compatible Request/Response ==========

@Serializable
data class OpenAiStyleRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Float = 0.7f,
    @SerialName("response_format")
    val responseFormat: Map<String, String>? = null
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAiStyleResponse(
    val id: String? = null,
    val choices: List<OpenAiChoice>? = null,
    val usage: OpenAiUsage? = null
)

@Serializable
data class OpenAiChoice(
    val message: OpenAiMessage? = null
)

@Serializable
data class OpenAiUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0
)

// ========== Models List Response ==========

@Serializable
data class ModelsResponse(
    val data: List<ModelItem>? = null
)

@Serializable
data class ModelItem(
    val id: String,
    @SerialName("owned_by")
    val ownedBy: String? = null
)

// ========== Music Info Response ==========

@Serializable
data class MusicInfoResponse(
    val genre: List<String> = emptyList(),
    val mood: List<String> = emptyList(),
    val scenario: List<String> = emptyList(),
    val language: String = "",
    val era: String = "",
    val rewards: String = "",
    val lyric: String = "",
    val singerIntroduce: String = "",
    val backgroundIntroduce: String = "",
    val description: String = "",
    val relevantMusic: String = "",
    val errorInfo: String = ""
)
