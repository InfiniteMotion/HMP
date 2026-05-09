package com.hmp.data.network

import com.hmp.data.network.dto.*
import com.hmp.domain.enum.AiProviderType
import com.hmp.domain.setting.model.AiProviderConfig
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

class MultiProviderApiAdapter(
    private val httpClient: HttpClient,
    private val json: Json
) {
    suspend fun callChatApi(
        config: AiProviderConfig,
        prompt: String
    ): AiApiResult<String> {
        return try {
            when (config.type) {
                AiProviderType.DEEPSEEK -> callDeepSeekApi(config, prompt)
                AiProviderType.OPENAI -> callOpenAiApi(config, prompt)
                AiProviderType.CLAUDE -> callClaudeApi(config, prompt)
                AiProviderType.QWEN -> callQwenApi(config, prompt)
                AiProviderType.ERNIE -> callErnieApi(config, prompt)
            }
        } catch (e: Exception) {
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "Unknown network error"))
        }
    }

    suspend fun testConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        return try {
            when (config.type) {
                AiProviderType.DEEPSEEK -> testDeepSeekConnection(config)
                AiProviderType.OPENAI -> testOpenAiConnection(config)
                AiProviderType.CLAUDE -> testClaudeConnection(config)
                AiProviderType.QWEN -> testQwenConnection(config)
                AiProviderType.ERNIE -> testErnieConnection(config)
            }
        } catch (e: Exception) {
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "Unknown network error"))
        }
    }

    private suspend fun testDeepSeekConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        val requestBody = OpenAiStyleRequest(
            model = config.getEffectiveModel(),
            messages = listOf(OpenAiMessage(role = "user", content = "Hi")),
            temperature = 0.7f,
            responseFormat = null
        )

        return executeTestRequest(config.type.defaultEndpoint, requestBody, config.apiKey)
    }

    private suspend fun testOpenAiConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        val requestBody = OpenAiStyleRequest(
            model = config.getEffectiveModel(),
            messages = listOf(OpenAiMessage(role = "user", content = "Hi")),
            temperature = 0.7f,
            responseFormat = null
        )

        return executeTestRequest(config.type.defaultEndpoint, requestBody, config.apiKey)
    }

    private suspend fun testClaudeConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        val requestBody = ClaudeRequest(
            model = config.getEffectiveModel(),
            maxTokens = 10,
            messages = listOf(ClaudeMessage(role = "user", content = "Hi"))
        )

        return executeClaudeTestRequest(config.type.defaultEndpoint, requestBody, config.apiKey)
    }

    private suspend fun testQwenConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        val requestBody = QwenRequest(
            model = config.getEffectiveModel(),
            input = QwenInput(
                messages = listOf(QwenMessage(role = "user", content = "Hi"))
            )
        )

        return executeQwenTestRequest(config.type.defaultEndpoint, requestBody, config.apiKey)
    }

    private suspend fun testErnieConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        val requestBody = ErnieRequest(
            messages = listOf(ErnieMessage(role = "user", content = "Hi"))
        )

        val urlWithToken = "${config.type.defaultEndpoint}?access_token=${config.apiKey.removePrefix("Bearer ").trim()}"
        return executeErnieTestRequest(urlWithToken, requestBody)
    }

    private suspend fun executeTestRequest(url: String, requestBody: OpenAiStyleRequest, apiKey: String): AiApiResult<Boolean> {
        return try {
            val response = httpClient.post(url) {
                headers {
                    append("Authorization", formatAuthToken(apiKey))
                    append("Content-Type", "application/json")
                }
                setBody(requestBody)
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

    private suspend fun executeClaudeTestRequest(url: String, requestBody: ClaudeRequest, apiKey: String): AiApiResult<Boolean> {
        return try {
            val response = httpClient.post(url) {
                headers {
                    append("x-api-key", apiKey.removePrefix("Bearer ").trim())
                    append("anthropic-version", "2023-06-01")
                    append("Content-Type", "application/json")
                }
                setBody(requestBody)
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

    private suspend fun executeQwenTestRequest(url: String, requestBody: QwenRequest, apiKey: String): AiApiResult<Boolean> {
        return try {
            val response = httpClient.post(url) {
                headers {
                    append("Authorization", formatAuthToken(apiKey))
                    append("Content-Type", "application/json")
                }
                setBody(requestBody)
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

    private suspend fun executeErnieTestRequest(url: String, requestBody: ErnieRequest): AiApiResult<Boolean> {
        return try {
            val response = httpClient.post(url) {
                headers {
                    append("Content-Type", "application/json")
                }
                setBody(requestBody)
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

    private suspend fun callDeepSeekApi(config: AiProviderConfig, prompt: String): AiApiResult<String> {
        val requestBody = OpenAiStyleRequest(
            model = config.getEffectiveModel(),
            messages = listOf(OpenAiMessage(role = "user", content = prompt)),
            temperature = 1.3f,
            responseFormat = mapOf("type" to "json_object")
        )

        return executeOpenAiStyleRequest(config.type.defaultEndpoint, requestBody, config.apiKey)
    }

    private suspend fun callOpenAiApi(config: AiProviderConfig, prompt: String): AiApiResult<String> {
        val requestBody = OpenAiStyleRequest(
            model = config.getEffectiveModel(),
            messages = listOf(OpenAiMessage(role = "user", content = prompt)),
            temperature = 0.7f,
            responseFormat = mapOf("type" to "json_object")
        )

        return executeOpenAiStyleRequest(config.type.defaultEndpoint, requestBody, config.apiKey)
    }

    private suspend fun callClaudeApi(config: AiProviderConfig, prompt: String): AiApiResult<String> {
        val requestBody = ClaudeRequest(
            model = config.getEffectiveModel(),
            maxTokens = 2048,
            messages = listOf(ClaudeMessage(role = "user", content = prompt))
        )

        return executeClaudeRequest(config.type.defaultEndpoint, requestBody, config.apiKey)
    }

    private suspend fun callQwenApi(config: AiProviderConfig, prompt: String): AiApiResult<String> {
        val requestBody = QwenRequest(
            model = config.getEffectiveModel(),
            input = QwenInput(
                messages = listOf(QwenMessage(role = "user", content = prompt))
            )
        )

        return executeQwenRequest(config.type.defaultEndpoint, requestBody, config.apiKey)
    }

    private suspend fun callErnieApi(config: AiProviderConfig, prompt: String): AiApiResult<String> {
        val requestBody = ErnieRequest(
            messages = listOf(ErnieMessage(role = "user", content = prompt))
        )

        val urlWithToken = "${config.type.defaultEndpoint}?access_token=${config.apiKey.removePrefix("Bearer ").trim()}"
        return executeErnieRequest(urlWithToken, requestBody)
    }

    private suspend fun executeOpenAiStyleRequest(url: String, requestBody: OpenAiStyleRequest, apiKey: String): AiApiResult<String> {
        return try {
            val response = httpClient.post(url) {
                headers {
                    append("Authorization", formatAuthToken(apiKey))
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

    private suspend fun executeClaudeRequest(url: String, requestBody: ClaudeRequest, apiKey: String): AiApiResult<String> {
        return try {
            val response = httpClient.post(url) {
                headers {
                    append("x-api-key", apiKey.removePrefix("Bearer ").trim())
                    append("anthropic-version", "2023-06-01")
                    append("Content-Type", "application/json")
                }
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                val responseBody: ClaudeResponse = response.body()
                val content = responseBody.content?.firstOrNull()?.text
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

    private suspend fun executeQwenRequest(url: String, requestBody: QwenRequest, apiKey: String): AiApiResult<String> {
        return try {
            val response = httpClient.post(url) {
                headers {
                    append("Authorization", formatAuthToken(apiKey))
                    append("Content-Type", "application/json")
                }
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                val responseBody: QwenResponse = response.body()
                val content = responseBody.output?.choices?.firstOrNull()?.message?.content
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

    private suspend fun executeErnieRequest(url: String, requestBody: ErnieRequest): AiApiResult<String> {
        return try {
            val response = httpClient.post(url) {
                headers {
                    append("Content-Type", "application/json")
                }
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                val responseBody: ErnieResponse = response.body()
                val content = responseBody.result
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
