package com.example.hearablemusicplayer.domain.usecase.music

import android.util.Log
import com.example.hearablemusicplayer.data.database.DailyMusicInfo
import com.example.hearablemusicplayer.data.database.ListeningDuration
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.database.MusicLabel
import com.example.hearablemusicplayer.data.repository.MusicRepository
import com.example.hearablemusicplayer.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 每日AI音乐推荐Use Case
 * 
 * 负责AI音乐推荐相关的业务逻辑，包括：
 * - 获取随机推荐音乐及其详细信息
 * - 自动处理缺失音乐扩展信息
 * - 从DeepSeek AI获取音乐标签和描述
 * - API密钥验证
 * - 播放时长统计
 * 
 * @property musicRepository 音乐数据仓库
 * @property settingsRepository 设置数据仓库
 * @property musicLabelUseCase 音乐标签管理用例
 */
class GetDailyMusicRecommendationUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository,
    private val musicLabelUseCase: MusicLabelUseCase
) {
    
    /**
     * 音乐推荐数据结构
     * 
     * @property musicInfo 音乐基本信息
     * @property dailyMusicInfo AI生成的音乐扩展信息（风格、情绪、场景等）
     * @property labels 音乐标签列表
     */
    data class MusicRecommendation(
        val musicInfo: MusicInfo?,
        val dailyMusicInfo: DailyMusicInfo?,
        val labels: List<MusicLabel?>
    )
    
    /**
     * 获取随机音乐及其额外信息
     * 
     * 从数据库中随机获取一首已经有AI生成扩展信息的音乐，
     * 并加载其标签信息。用于每日推荐功能。
     * 
     * @return [MusicRecommendation] 包含音乐基本信息、扩展信息和标签
     */
    suspend fun getRandomMusicWithExtra(): MusicRecommendation {
        val musicInfo = musicRepository.getRandomMusicInfoWithExtra()
        val dailyMusicInfo = musicInfo?.music?.id?.let { musicRepository.getMusicExtraById(it) }
        val labels = musicInfo?.music?.id?.let { musicRepository.getMusicLabels(it) } ?: emptyList()
        return MusicRecommendation(musicInfo, dailyMusicInfo, labels)
    }
    
    /**
     * 自动处理缺失额外信息的音乐
     * 
     * 遍历数据库中所有缺失AI扩展信息的音乐，
     * 自动调用DeepSeek API获取并保存扩展信息和标签。
     * 支持进度回调和节流控制。
     * 
     * @param onProgress 处理每首音乐时的进度回调
     * @param delayMillis 每次请求之间的延迟时间（毫秒），默认500ms
     */
    suspend fun autoProcessMissingExtraInfo(
        onProgress: suspend (MusicInfo) -> Unit = {},
        delayMillis: Long = 500
    ) {
        while (true) {
            val music = musicRepository.getRandomMusicInfoWithMissingExtra() ?: break

            onProgress(music)
            getMusicExtraInfoFromLLM(music)
            delay(delayMillis)
        }
    }
    
    /**
     * 从DeepSeek获取音乐额外信息
     */
    private suspend fun getMusicExtraInfoFromLLM(input: MusicInfo) {
        val authToken = settingsRepository.getDeepSeekApiKey()
        val result = musicRepository.fetchMusicExtraInfo(authToken, input.music.title, input.music.artist)
        when (result) {
            is com.example.hearablemusicplayer.data.repository.Result.Success -> {
                val intro = result.data
                musicRepository.insertMusicExtra(input.music.id, intro)
                saveMusicLabels(input.music.id, intro)
                Log.d("GetDailyMusicRecommendationUseCase", "Successfully processed music extra info via Repository")
            }
            is com.example.hearablemusicplayer.data.repository.Result.Error -> {
                Log.e("GetDailyMusicRecommendationUseCase", "Fetch extra info failed: ${result.exception.message}")
            }
            is com.example.hearablemusicplayer.data.repository.Result.Loading -> {
                // Loading 状态，通常不会在此分支出现
            }
        }
    }
    
    /**
     * 保存音乐标签
     */
    private suspend fun saveMusicLabels(musicId: Long, dailyMusicInfo: DailyMusicInfo) {
        val labels = MusicLabels(
            genres = dailyMusicInfo.genre,
            moods = dailyMusicInfo.mood,
            scenarios = dailyMusicInfo.scenario,
            language = dailyMusicInfo.language,
            era = dailyMusicInfo.era
        )
        musicLabelUseCase.addMusicLabels(musicId, labels)
    }
    
    /**
     * 验证DeepSeek API密钥
     * 
     * 通过尝试调用DeepSeek API来验证密钥是否有效。
     * 
     * @param apiKey 待验证的API密钥
     * @return true 如果密钥有效，false 否则
     */
    suspend fun validateApiKey(apiKey: String): Boolean {
        return when (musicRepository.validateApiKey(apiKey)) {
            is com.example.hearablemusicplayer.data.repository.Result.Success -> true
            is com.example.hearablemusicplayer.data.repository.Result.Error -> false
            is com.example.hearablemusicplayer.data.repository.Result.Loading -> false
        }
    }
    
    /**
     * 获取最近的播放时长统计
     * 
     * 返回用户最近的音乐播放时长记录，用于显示听歌统计图表。
     * 
     * @return Flow<List<ListeningDuration>> 播放时长记录流
     */
    fun getRecentListeningDurations(): Flow<List<ListeningDuration>> {
        return musicRepository.getRecentListeningDurations()
    }
}
