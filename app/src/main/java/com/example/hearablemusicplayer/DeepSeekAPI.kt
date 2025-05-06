package com.example.hearablemusicplayer

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit


interface DeepSeekAPI {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authToken: String, // 动态传入 Bearer Token
        @Body request: ChatRequest // 请求体
    ): Response<ChatResponse>
}

// 请求体
data class ChatRequest(
    val model: String = "deepseek-chat",
//    val model: String = "Pro/deepseek-ai/DeepSeek-V3",
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

val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS) // 连接超时
    .readTimeout(60, TimeUnit.SECONDS)    // 读取超时
    .writeTimeout(60, TimeUnit.SECONDS)   // 写入超时
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 打印请求/响应日志
    })
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.deepseek.com") // DeepSeek Base URL
//    .baseUrl("https://api.siliconflow.cn/v1/") // SiliconFlow Base URL
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create()) // JSON 解析
    .build()

val DeepSeekService = retrofit.create(DeepSeekAPI::class.java)
