package com.example.hearablemusicplayer.data.network

import android.util.Log
import com.example.hearablemusicplayer.domain.model.AiProviderConfig
import com.example.hearablemusicplayer.domain.model.enum.AiProviderType
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 多服务商 API 适配器
 * 统一处理不同 AI 服务商的 API 调用
 */
@Singleton
class MultiProviderApiAdapter @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "MultiProviderApiAdapter"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * 调用 AI API 获取聊天响应
     */
    suspend fun callChatApi(
        config: AiProviderConfig,
        prompt: String
    ): AiApiResult<String> = withContext(Dispatchers.IO) {
        try {
            when (config.type) {
                AiProviderType.DEEPSEEK -> callDeepSeekApi(config, prompt)
                AiProviderType.OPENAI -> callOpenAiApi(config, prompt)
                AiProviderType.CLAUDE -> callClaudeApi(config, prompt)
                AiProviderType.QWEN -> callQwenApi(config, prompt)
                AiProviderType.ERNIE -> callErnieApi(config, prompt)
            }
        } catch (e: Exception) {
            Log.e(TAG, "API call failed: ${e.message}", e)
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "Unknown network error"))
        }
    }

    /**
     * 测试服务商连接
     * 使用简单的测试请求，不要求 JSON 格式响应
     */
    suspend fun testConnection(config: AiProviderConfig): AiApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            when (config.type) {
                AiProviderType.DEEPSEEK -> testDeepSeekConnection(config)
                AiProviderType.OPENAI -> testOpenAiConnection(config)
                AiProviderType.CLAUDE -> testClaudeConnection(config)
                AiProviderType.QWEN -> testQwenConnection(config)
                AiProviderType.ERNIE -> testErnieConnection(config)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed: ${e.message}", e)
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "Unknown network error"))
        }
    }

    private fun testDeepSeekConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        val requestBody = OpenAiStyleRequest(
            model = config.getEffectiveModel(),
            messages = listOf(OpenAiMessage(role = "user", content = "Hi")),
            temperature = 0.7f,
            responseFormat = null // 测试时不要求 JSON 格式
        )

        val request = Request.Builder()
            .url(config.type.defaultEndpoint)
            .addHeader("Authorization", formatAuthToken(config.apiKey))
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return executeTestRequest(request)
    }

    private fun testOpenAiConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        val requestBody = OpenAiStyleRequest(
            model = config.getEffectiveModel(),
            messages = listOf(OpenAiMessage(role = "user", content = "Hi")),
            temperature = 0.7f,
            responseFormat = null
        )

        val request = Request.Builder()
            .url(config.type.defaultEndpoint)
            .addHeader("Authorization", formatAuthToken(config.apiKey))
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return executeTestRequest(request)
    }

    private fun testClaudeConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        val requestBody = ClaudeRequest(
            model = config.getEffectiveModel(),
            maxTokens = 10,
            messages = listOf(ClaudeMessage(role = "user", content = "Hi"))
        )

        val request = Request.Builder()
            .url(config.type.defaultEndpoint)
            .addHeader("x-api-key", config.apiKey.removePrefix("Bearer ").trim())
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return executeTestRequest(request)
    }

    private fun testQwenConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        val requestBody = QwenRequest(
            model = config.getEffectiveModel(),
            input = QwenInput(
                messages = listOf(QwenMessage(role = "user", content = "Hi"))
            )
        )

        val request = Request.Builder()
            .url(config.type.defaultEndpoint)
            .addHeader("Authorization", formatAuthToken(config.apiKey))
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return executeTestRequest(request)
    }

    private fun testErnieConnection(config: AiProviderConfig): AiApiResult<Boolean> {
        val requestBody = ErnieRequest(
            messages = listOf(ErnieMessage(role = "user", content = "Hi"))
        )

        val urlWithToken = "${config.type.defaultEndpoint}?access_token=${config.apiKey.removePrefix("Bearer ").trim()}"

        val request = Request.Builder()
            .url(urlWithToken)
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return executeTestRequest(request)
    }

    private fun executeTestRequest(request: Request): AiApiResult<Boolean> {
        return try {
            okHttpClient.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> AiApiResult.Success(true)
                    response.code == 401 || response.code == 403 -> {
                        AiApiResult.Error(AiApiError.AuthError("认证失败: ${response.message}"))
                    }
                    response.code == 429 -> {
                        AiApiResult.Error(AiApiError.RateLimitError("请求过于频繁"))
                    }
                    response.code in 500..599 -> {
                        AiApiResult.Error(AiApiError.ServerError(response.code, response.message))
                    }
                    else -> {
                        val body = response.body?.string() ?: ""
                        AiApiResult.Error(AiApiError.UnknownError("HTTP ${response.code}: $body"))
                    }
                }
            }
        } catch (e: Exception) {
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "网络错误"))
        }
    }

    // ==================== DeepSeek API ====================
    private suspend fun callDeepSeekApi(config: AiProviderConfig, prompt: String): AiApiResult<String> {
        val requestBody = OpenAiStyleRequest(
            model = config.getEffectiveModel(),
            messages = listOf(OpenAiMessage(role = "user", content = prompt)),
            temperature = 1.3f,
            responseFormat = mapOf("type" to "json_object")
        )

        val request = Request.Builder()
            .url(config.type.defaultEndpoint)
            .addHeader("Authorization", formatAuthToken(config.apiKey))
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return executeOpenAiStyleRequest(request)
    }

    // ==================== OpenAI API ====================
    private suspend fun callOpenAiApi(config: AiProviderConfig, prompt: String): AiApiResult<String> {
        val requestBody = OpenAiStyleRequest(
            model = config.getEffectiveModel(),
            messages = listOf(OpenAiMessage(role = "user", content = prompt)),
            temperature = 0.7f,
            responseFormat = mapOf("type" to "json_object")
        )

        val request = Request.Builder()
            .url(config.type.defaultEndpoint)
            .addHeader("Authorization", formatAuthToken(config.apiKey))
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return executeOpenAiStyleRequest(request)
    }

    // ==================== Claude API ====================
    private suspend fun callClaudeApi(config: AiProviderConfig, prompt: String): AiApiResult<String> {
        val requestBody = ClaudeRequest(
            model = config.getEffectiveModel(),
            maxTokens = 2048,
            messages = listOf(ClaudeMessage(role = "user", content = prompt))
        )

        val request = Request.Builder()
            .url(config.type.defaultEndpoint)
            .addHeader("x-api-key", config.apiKey.removePrefix("Bearer ").trim())
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return executeClaudeRequest(request)
    }

    // ==================== 通义千问 API ====================
    private suspend fun callQwenApi(config: AiProviderConfig, prompt: String): AiApiResult<String> {
        val requestBody = QwenRequest(
            model = config.getEffectiveModel(),
            input = QwenInput(
                messages = listOf(QwenMessage(role = "user", content = prompt))
            )
        )

        val request = Request.Builder()
            .url(config.type.defaultEndpoint)
            .addHeader("Authorization", formatAuthToken(config.apiKey))
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return executeQwenRequest(request)
    }

    // ==================== 文心一言 API ====================
    private suspend fun callErnieApi(config: AiProviderConfig, prompt: String): AiApiResult<String> {
        // 文心一言需要先获取 access_token，这里简化处理
        // 实际使用时可能需要额外的 token 管理
        val requestBody = ErnieRequest(
            messages = listOf(ErnieMessage(role = "user", content = prompt))
        )

        val urlWithToken = "${config.type.defaultEndpoint}?access_token=${config.apiKey.removePrefix("Bearer ").trim()}"
        
        val request = Request.Builder()
            .url(urlWithToken)
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return executeErnieRequest(request)
    }

    // ==================== 请求执行方法 ====================
    
    private fun executeOpenAiStyleRequest(request: Request): AiApiResult<String> {
        return try {
            okHttpClient.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        val body = response.body?.string() ?: return AiApiResult.Error(
                            AiApiError.ParseError("Empty response body")
                        )
                        val result = gson.fromJson(body, OpenAiStyleResponse::class.java)
                        val content = result.choices?.firstOrNull()?.message?.content
                        if (content != null) {
                            AiApiResult.Success(content)
                        } else {
                            AiApiResult.Error(AiApiError.ParseError("No content in response"))
                        }
                    }
                    response.code == 401 || response.code == 403 -> {
                        AiApiResult.Error(AiApiError.AuthError("Authentication failed: ${response.message}"))
                    }
                    response.code == 429 -> {
                        AiApiResult.Error(AiApiError.RateLimitError("Rate limit exceeded"))
                    }
                    response.code in 500..599 -> {
                        AiApiResult.Error(AiApiError.ServerError(response.code, response.message))
                    }
                    else -> {
                        AiApiResult.Error(AiApiError.UnknownError("HTTP ${response.code}: ${response.message}"))
                    }
                }
            }
        } catch (e: Exception) {
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "Network error"))
        }
    }

    private fun executeClaudeRequest(request: Request): AiApiResult<String> {
        return try {
            okHttpClient.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        val body = response.body?.string() ?: return AiApiResult.Error(
                            AiApiError.ParseError("Empty response body")
                        )
                        val result = gson.fromJson(body, ClaudeResponse::class.java)
                        val content = result.content?.firstOrNull()?.text
                        if (content != null) {
                            AiApiResult.Success(content)
                        } else {
                            AiApiResult.Error(AiApiError.ParseError("No content in response"))
                        }
                    }
                    response.code == 401 || response.code == 403 -> {
                        AiApiResult.Error(AiApiError.AuthError("Authentication failed: ${response.message}"))
                    }
                    response.code == 429 -> {
                        AiApiResult.Error(AiApiError.RateLimitError("Rate limit exceeded"))
                    }
                    else -> {
                        AiApiResult.Error(AiApiError.UnknownError("HTTP ${response.code}: ${response.message}"))
                    }
                }
            }
        } catch (e: Exception) {
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "Network error"))
        }
    }

    private fun executeQwenRequest(request: Request): AiApiResult<String> {
        return try {
            okHttpClient.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        val body = response.body?.string() ?: return AiApiResult.Error(
                            AiApiError.ParseError("Empty response body")
                        )
                        val result = gson.fromJson(body, QwenResponse::class.java)
                        val content = result.output?.choices?.firstOrNull()?.message?.content
                        if (content != null) {
                            AiApiResult.Success(content)
                        } else {
                            AiApiResult.Error(AiApiError.ParseError("No content in response"))
                        }
                    }
                    response.code == 401 || response.code == 403 -> {
                        AiApiResult.Error(AiApiError.AuthError("Authentication failed: ${response.message}"))
                    }
                    response.code == 429 -> {
                        AiApiResult.Error(AiApiError.RateLimitError("Rate limit exceeded"))
                    }
                    else -> {
                        AiApiResult.Error(AiApiError.UnknownError("HTTP ${response.code}: ${response.message}"))
                    }
                }
            }
        } catch (e: Exception) {
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "Network error"))
        }
    }

    private fun executeErnieRequest(request: Request): AiApiResult<String> {
        return try {
            okHttpClient.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        val body = response.body?.string() ?: return AiApiResult.Error(
                            AiApiError.ParseError("Empty response body")
                        )
                        val result = gson.fromJson(body, ErnieResponse::class.java)
                        val content = result.result
                        if (content != null) {
                            AiApiResult.Success(content)
                        } else {
                            AiApiResult.Error(AiApiError.ParseError("No content in response"))
                        }
                    }
                    response.code == 401 || response.code == 403 -> {
                        AiApiResult.Error(AiApiError.AuthError("Authentication failed: ${response.message}"))
                    }
                    response.code == 429 -> {
                        AiApiResult.Error(AiApiError.RateLimitError("Rate limit exceeded"))
                    }
                    else -> {
                        AiApiResult.Error(AiApiError.UnknownError("HTTP ${response.code}: ${response.message}"))
                    }
                }
            }
        } catch (e: Exception) {
            AiApiResult.Error(AiApiError.NetworkError(e.message ?: "Network error"))
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

// ==================== API 结果类型 ====================

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

// ==================== OpenAI 风格请求/响应（DeepSeek、OpenAI 通用）====================

data class OpenAiStyleRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Float = 0.7f,
    @SerializedName("response_format")
    val responseFormat: Map<String, String>? = null
)

data class OpenAiMessage(
    val role: String,
    val content: String
)

data class OpenAiStyleResponse(
    val id: String?,
    val choices: List<OpenAiChoice>?
)

data class OpenAiChoice(
    val message: OpenAiMessage?
)

// ==================== Claude 请求/响应 ====================

data class ClaudeRequest(
    val model: String,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2048,
    val messages: List<ClaudeMessage>
)

data class ClaudeMessage(
    val role: String,
    val content: String
)

data class ClaudeResponse(
    val id: String?,
    val content: List<ClaudeContent>?
)

data class ClaudeContent(
    val type: String?,
    val text: String?
)

// ==================== 通义千问请求/响应 ====================

data class QwenRequest(
    val model: String,
    val input: QwenInput
)

data class QwenInput(
    val messages: List<QwenMessage>
)

data class QwenMessage(
    val role: String,
    val content: String
)

data class QwenResponse(
    val output: QwenOutput?
)

data class QwenOutput(
    val choices: List<QwenChoice>?
)

data class QwenChoice(
    val message: QwenMessage?
)

// ==================== 文心一言请求/响应 ====================

data class ErnieRequest(
    val messages: List<ErnieMessage>
)

data class ErnieMessage(
    val role: String,
    val content: String
)

data class ErnieResponse(
    val result: String?,
    @SerializedName("error_code")
    val errorCode: Int?,
    @SerializedName("error_msg")
    val errorMsg: String?
)
