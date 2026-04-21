package com.hmp.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val choices: List<OpenAiChoice>? = null
)

@Serializable
data class OpenAiChoice(
    val message: OpenAiMessage? = null
)

@Serializable
data class ClaudeRequest(
    val model: String,
    @SerialName("max_tokens")
    val maxTokens: Int = 2048,
    val messages: List<ClaudeMessage>
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeResponse(
    val id: String? = null,
    val content: List<ClaudeContent>? = null
)

@Serializable
data class ClaudeContent(
    val type: String? = null,
    val text: String? = null
)

@Serializable
data class QwenRequest(
    val model: String,
    val input: QwenInput
)

@Serializable
data class QwenInput(
    val messages: List<QwenMessage>
)

@Serializable
data class QwenMessage(
    val role: String,
    val content: String
)

@Serializable
data class QwenResponse(
    val output: QwenOutput? = null
)

@Serializable
data class QwenOutput(
    val choices: List<QwenChoice>? = null
)

@Serializable
data class QwenChoice(
    val message: QwenMessage? = null
)

@Serializable
data class ErnieRequest(
    val messages: List<ErnieMessage>
)

@Serializable
data class ErnieMessage(
    val role: String,
    val content: String
)

@Serializable
data class ErnieResponse(
    val result: String? = null,
    @SerialName("error_code")
    val errorCode: Int? = null,
    @SerialName("error_msg")
    val errorMsg: String? = null
)

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
