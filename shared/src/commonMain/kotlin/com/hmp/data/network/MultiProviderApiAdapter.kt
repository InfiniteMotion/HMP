package com.hmp.data.network

import com.hmp.data.network.dto.ModelsResponse
import com.hmp.data.network.dto.OpenAiMessage
import com.hmp.data.network.dto.OpenAiStyleRequest
import com.hmp.data.network.dto.OpenAiStyleResponse
import com.hmp.domain.setting.model.AiEndpointConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

sealed class AiApiResult<out T> {
    data class Success<T>(val data: T) : AiApiResult<T>()
    data class Error(val error: AiApiError) : AiApiResult<Nothing>()
}

sealed class AiApiError {
    data class NetworkError(val message: String) : AiApiError()
    data class AuthError(val message: String) : AiApiError()
    data class RateLimitError(val message: String) : AiApiError()
    data class ServerError(val code: Int, val message: String) : AiApiError()
    data class ParseError(val message: String) : AiApiError()
    data class UnknownError(val message: String) : AiApiError()

    fun toDisplayMessage(): String {
        return when (this) {
            is NetworkError -> "网络连接失败: $message"
            is AuthError -> "认证失败，请检查 API Key"
            is RateLimitError -> "请求过于频繁，请稍后重试"
            is ServerError -> "服务器错误 ($code)"
            is ParseError -> "响应解析失败"
            is UnknownError -> "未知错误: $message"
        }
    }
}

/**
 * 统一 OpenAI 兼容 API 适配器
 * 所有请求走 POST {endpoint}/chat/completions
 * 模型列表走 GET {endpoint}/models
 */
class OpenAiCompatibleAdapter(
    private val httpClient: HttpClient,
    private val json: Json
) {
    /**
     * 调用 Chat Completion API
     */
    suspend fun callChatApi(
        config: AiEndpointConfig,
        prompt: String
    ): AiApiResult<String> {
        return try {
            val url = "${config.endpoint.trimEnd('/')}/chat/completions"
            val requestBody = OpenAiStyleRequest(
                model = config.selectedModel,
                messages = listOf(OpenAiMessage(role = "user", content = prompt)),
                temperature = 1.3f,
                responseFormat = mapOf("type" to "json_object")
            )

            val response = httpClient.post(url) {
                headers {
                    append("Authorization", formatAuthToken(config.apiKey))
                    append("Content-Type", "application/json")
                }
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                val responseBody: OpenAiStyleResponse = response.body()
                val content = responseBody.choices?.firstOrNull()?.message?.content
                if (content != null) {
                    AiApiResult.Success(content)
                } else {
                    AiApiResult.Error(AiApiError.ParseError("No content in response"))
                }
            } else {
                handleHttpError(response.status)
            }
        } catch (e: Exception) {
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "Network error"))
        }
    }

    /**
     * 获取可用模型列表
     */
    suspend fun fetchModels(config: AiEndpointConfig): AiApiResult<List<String>> {
        return try {
            val url = "${config.endpoint.trimEnd('/')}/models"
            val response = httpClient.get(url) {
                headers {
                    append("Authorization", formatAuthToken(config.apiKey))
                }
            }

            if (response.status.isSuccess()) {
                val responseBody: ModelsResponse = response.body()
                val models = responseBody.data?.map { it.id }?.sorted() ?: emptyList()
                if (models.isNotEmpty()) {
                    AiApiResult.Success(models)
                } else {
                    AiApiResult.Error(AiApiError.ParseError("No models returned"))
                }
            } else {
                handleHttpError(response.status)
            }
        } catch (e: Exception) {
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "Network error"))
        }
    }

    /**
     * 测试连接（验证 endpoint + key 有效性）
     */
    suspend fun testConnection(config: AiEndpointConfig): AiApiResult<Boolean> {
        return try {
            val url = "${config.endpoint.trimEnd('/')}/models"
            val response = httpClient.get(url) {
                headers {
                    append("Authorization", formatAuthToken(config.apiKey))
                }
            }

            if (response.status.isSuccess()) {
                AiApiResult.Success(true)
            } else {
                handleHttpError(response.status)
            }
        } catch (e: Exception) {
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "网络错误"))
        }
    }

    private fun handleHttpError(status: HttpStatusCode): AiApiResult<Nothing> {
        return when (status.value) {
            401, 403 -> AiApiResult.Error(AiApiError.AuthError("Authentication failed: ${status.description}"))
            429 -> AiApiResult.Error(AiApiError.RateLimitError("Rate limit exceeded"))
            in 500..599 -> AiApiResult.Error(AiApiError.ServerError(status.value, status.description))
            else -> AiApiResult.Error(AiApiError.UnknownError("HTTP ${status.value}: ${status.description}"))
        }
    }

    private fun formatAuthToken(apiKey: String): String {
        return if (apiKey.startsWith("Bearer ", ignoreCase = true)) {
            apiKey
        } else {
            "Bearer $apiKey"
        }
    }
}
