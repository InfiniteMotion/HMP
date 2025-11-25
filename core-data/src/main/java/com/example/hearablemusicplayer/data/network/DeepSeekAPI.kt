package com.example.hearablemusicplayer.data.network

import android.util.Log
import kotlinx.coroutines.delay
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton


interface DeepSeekAPI {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authToken: String, // 动态传入 Bearer Token
        @Body request: ChatRequest // 请求体
    ): Response<ChatResponse>
}

// 请求体
data class ChatRequest(
    val model: String = "deepseek-chat", // DeepSeek Request
    val messages: List<Message>,
    val temperature: Float = 1.3f,
    val response_format: Map<String, String> = mapOf("type" to "json_object")
)

data class Message(
    val role: String, // "user"
    val content: String
)

// 响应体
data class ChatResponse(
    val id: String,
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

/**
 * DeepSeek API 错误类型
 */
sealed class DeepSeekError {
    data class NetworkError(val message: String) : DeepSeekError()
    data class RateLimitError(val retryAfter: Long) : DeepSeekError()
    data class AuthError(val message: String) : DeepSeekError()
    data class ServerError(val code: Int, val message: String) : DeepSeekError()
    data class UnknownError(val message: String) : DeepSeekError()
}

/**
 * DeepSeek API 结果包装
 */
sealed class DeepSeekResult<out T> {
    data class Success<T>(val data: T) : DeepSeekResult<T>()
    data class Error(val error: DeepSeekError) : DeepSeekResult<Nothing>()
    data class CachedFallback<T>(val data: T) : DeepSeekResult<T>() // 降级缓存
}

/**
 * DeepSeek API 包装器 - 提供速率限制、错误分类和降级策略
 */
@Singleton
class DeepSeekAPIWrapper @Inject constructor(
    private val api: DeepSeekAPI
) {
    companion object {
        private const val TAG = "DeepSeekAPIWrapper"
        private const val MIN_REQUEST_INTERVAL_MS = 1000L // 最小请求间隔
        private const val MAX_RETRIES = 5 // 最大重试次数
    }
    
    private var lastRequestTime = 0L
    private val requestCache = mutableMapOf<String, ChatResponse>() // 简单的内存缓存
    
    /**
     * 调用 DeepSeek API,带速率限制和错误处理
     */
    suspend fun createChatCompletion(
        authToken: String,
        request: ChatRequest,
        useCache: Boolean = true
    ): DeepSeekResult<ChatResponse> {
        // 生成缓存键
        val cacheKey = generateCacheKey(request)
        
        // 检查缓存
        if (useCache && requestCache.containsKey(cacheKey)) {
            Log.d(TAG, "Using cached response")
            return DeepSeekResult.CachedFallback(requestCache[cacheKey]!!)
        }
        
        // 速率限制
        enforceRateLimit()
        
        var lastError: DeepSeekError? = null
        var retryCount = 0
        
        while (retryCount < MAX_RETRIES) {
            try {
                val response = api.createChatCompletion(authToken, request)
                
                when {
                    response.isSuccessful && response.body() != null -> {
                        val body = response.body()!!
                        // 缓存成功响应
                        requestCache[cacheKey] = body
                        return DeepSeekResult.Success(body)
                    }
                    response.code() == 401 || response.code() == 403 -> {
                        // 认证错误,不重试
                        val error = DeepSeekError.AuthError("Authentication failed: ${response.message()}")
                        Log.e(TAG, "Auth error: ${response.code()}")
                        return DeepSeekResult.Error(error)
                    }
                    response.code() == 429 -> {
                        // 速率限制错误
                        val retryAfter = response.headers()["Retry-After"]?.toLongOrNull() ?: 60000L
                        lastError = DeepSeekError.RateLimitError(retryAfter)
                        Log.w(TAG, "Rate limit hit, retry after: $retryAfter ms")
                        delay(retryAfter)
                    }
                    response.code() in 500..599 -> {
                        // 服务器错误,可重试
                        lastError = DeepSeekError.ServerError(response.code(), response.message())
                        Log.w(TAG, "Server error: ${response.code()}, retry $retryCount")
                    }
                    else -> {
                        lastError = DeepSeekError.UnknownError("HTTP ${response.code()}: ${response.message()}")
                        Log.w(TAG, "Unknown error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                lastError = DeepSeekError.NetworkError(e.message ?: "Network error")
                Log.e(TAG, "Network exception: ${e.message}", e)
            }
            
            retryCount++
            if (retryCount < MAX_RETRIES) {
                // 指数退避
                val backoffDelay = MIN_REQUEST_INTERVAL_MS * (1 shl retryCount)
                delay(backoffDelay)
            }
        }
        
        // 所有重试失败,尝试返回缓存
        if (requestCache.containsKey(cacheKey)) {
            Log.w(TAG, "All retries failed, using cached fallback")
            return DeepSeekResult.CachedFallback(requestCache[cacheKey]!!)
        }
        
        // 无缓存可用,返回错误
        return DeepSeekResult.Error(lastError ?: DeepSeekError.UnknownError("Unknown error after retries"))
    }
    
    /**
     * 强制速率限制
     */
    private suspend fun enforceRateLimit() {
        val now = System.currentTimeMillis()
        val timeSinceLastRequest = now - lastRequestTime
        
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
            val waitTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest
            Log.d(TAG, "Rate limiting: waiting $waitTime ms")
            delay(waitTime)
        }
        
        lastRequestTime = System.currentTimeMillis()
    }
    
    /**
     * 生成缓存键
     */
    private fun generateCacheKey(request: ChatRequest): String {
        return request.messages.joinToString("|") { it.content }
    }
    
    /**
     * 清除缓存
     */
    fun clearCache() {
        requestCache.clear()
        Log.d(TAG, "Cache cleared")
    }
    
    /**
     * 获取缓存大小
     */
    fun getCacheSize(): Int = requestCache.size
}
