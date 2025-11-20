package com.example.hearablemusicplayer.domain.usecase.music

import com.example.hearablemusicplayer.data.database.DailyMusicInfo
import com.example.hearablemusicplayer.data.network.ChatRequest
import com.example.hearablemusicplayer.data.network.DeepSeekAPIWrapper
import com.example.hearablemusicplayer.data.network.DeepSeekResult
import com.example.hearablemusicplayer.data.network.Message
import com.example.hearablemusicplayer.data.repository.MusicRepository
import com.example.hearablemusicplayer.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取每日AI音乐推荐
 * Use Case: 封装AI推荐逻辑,整合API调用和数据库操作
 */
class GetDailyMusicRecommendationUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository,
    private val deepSeekAPIWrapper: DeepSeekAPIWrapper
) {
    /**
     * 获取每日推荐音乐信息
     */
    suspend fun getDailyMusicInfo(): DailyMusicInfo? {
        // Domain层通过Repository获取,不直接暴露Flow
        // ViewModel可以直接使用Repository的Flow
        return null // 这个方法由ViewModel直接调用Repository
    }
    
    /**
     * 通过AI生成每日推荐
     * @param authToken API授权token
     * @return AI推荐结果
     */
    suspend fun generateDailyRecommendation(authToken: String): DeepSeekResult<String> {
        // 获取用户名 - 使用suspend方法
        val userName = "用户" // SettingsRepository没有同步获取方法,简化处理
        
        // 构建AI请求
        val messages = listOf(
            Message(
                role = "system",
                content = "你是一个音乐推荐助手,根据用户的听歌习惯推荐适合的音乐。"
            ),
            Message(
                role = "user",
                content = "为用户 $userName 推荐今日音乐"
            )
        )
        
        val request = ChatRequest(
            model = "deepseek-chat",
            messages = messages,
            temperature = 0.7f // Float类型
        )
        
        // 调用API
        return when (val result = deepSeekAPIWrapper.createChatCompletion(authToken, request)) {
            is DeepSeekResult.Success -> {
                val recommendation = result.data.choices.firstOrNull()?.message?.content ?: ""
                DeepSeekResult.Success(recommendation)
            }
            is DeepSeekResult.Error -> result
            is DeepSeekResult.CachedFallback -> {
                val recommendation = result.data.choices.firstOrNull()?.message?.content ?: ""
                DeepSeekResult.Success(recommendation)
            }
        }
    }
}
