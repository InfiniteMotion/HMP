package com.example.hearablemusicplayer.domain.usecase.music

import android.util.Log
import com.example.hearablemusicplayer.data.database.DailyMusicInfo
import com.example.hearablemusicplayer.data.database.ListeningDuration
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.database.MusicLabel
import com.example.hearablemusicplayer.data.repository.MusicRepository
import com.example.hearablemusicplayer.data.repository.SettingsRepository
import com.example.hearablemusicplayer.data.model.AiProviderType
import com.example.hearablemusicplayer.data.model.AiProviderConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.atomic.AtomicBoolean
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
    
    companion object {
        private const val TAG = "GetDailyMusicRecommendationUseCase"
    }
    
    // ==================== 处理控制标志 ====================
    
    private val _isPaused = AtomicBoolean(false)
    private val _isCancelled = AtomicBoolean(false)
    
    /**
     * 暂停处理
     */
    fun pauseProcessing() {
        _isPaused.set(true)
        Log.d(TAG, "Processing paused")
    }
    
    /**
     * 继续处理
     */
    fun resumeProcessing() {
        _isPaused.set(false)
        Log.d(TAG, "Processing resumed")
    }
    
    /**
     * 取消处理
     */
    fun cancelProcessing() {
        _isCancelled.set(true)
        _isPaused.set(false)
        Log.d(TAG, "Processing cancelled")
    }
    
    /**
     * 重置处理状态
     */
    fun resetProcessingState() {
        _isPaused.set(false)
        _isCancelled.set(false)
    }
    
    /**
     * 是否已暂停
     */
    fun isPaused(): Boolean = _isPaused.get()
    
    // ==================== 处理结果数据类 ====================
    
    /**
     * 处理结果统计
     */
    data class ProcessingResult(
        val totalProcessed: Int = 0,
        val successCount: Int = 0,
        val skippedCount: Int = 0,
        val failedCount: Int = 0,
        val errors: List<String> = emptyList(),
        val wasCancelled: Boolean = false
    ) {
        val isAllSuccess: Boolean
            get() = totalProcessed > 0 && failedCount == 0 && skippedCount == 0
    }
    
    /**
     * 单个处理结果
     */
    sealed class ExtraInfoResult {
        data class Success(val intro: DailyMusicInfo) : ExtraInfoResult()
        data object Skipped : ExtraInfoResult()
        data class Error(val message: String) : ExtraInfoResult()
    }
    
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
    
    // ==================== 多服务商支持方法 ====================
    
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
     * 验证指定服务商的 API Key
     * 
     * @param providerConfig 服务商配置
     * @return true 如果密钥有效
     */
    suspend fun validateProviderApiKey(providerConfig: AiProviderConfig): Boolean {
        return when (musicRepository.validateProviderApiKey(providerConfig)) {
            is com.example.hearablemusicplayer.data.repository.Result.Success -> true
            is com.example.hearablemusicplayer.data.repository.Result.Error -> false
            is com.example.hearablemusicplayer.data.repository.Result.Loading -> false
        }
    }
    
    /**
     * 自动处理缺失额外信息的音乐（使用当前服务商）
     * 支持暂停、取消控制和结果统计
     * 
     * @param onProgress 处理每首音乐时的进度回调
     * @param onComplete 处理完成时的回调，返回处理结果统计
     * @param delayMillis 每次请求之间的延迟时间（毫秒）
     */
    suspend fun autoProcessMissingExtraInfoWithCurrentProvider(
        onProgress: suspend (MusicInfo) -> Unit = {},
        onComplete: suspend (ProcessingResult) -> Unit = {},
        delayMillis: Long = 500
    ) {
        resetProcessingState()
        
        val providerConfig = settingsRepository.getCurrentProviderConfig()
        
        if (!providerConfig.isConfigured) {
            Log.w(TAG, "No AI provider configured, skipping auto process")
            onComplete(ProcessingResult())
            return
        }
        
        var successCount = 0
        var skippedCount = 0
        var failedCount = 0
        val errors = mutableListOf<String>()
        
        while (true) {
            // 检查是否取消
            if (_isCancelled.get()) {
                Log.d(TAG, "Processing cancelled by user")
                break
            }
            
            // 检查是否暂停
            while (_isPaused.get()) {
                delay(100)
                if (_isCancelled.get()) break
            }
            
            if (_isCancelled.get()) break
            
            val music = musicRepository.getRandomMusicInfoWithMissingExtra() ?: break
            
            onProgress(music)
            
            // 处理当前音乐并统计结果
            when (val result = getMusicExtraInfoWithCurrentProviderAndResult(music)) {
                is ExtraInfoResult.Success -> successCount++
                is ExtraInfoResult.Skipped -> skippedCount++
                is ExtraInfoResult.Error -> {
                    failedCount++
                    errors.add("${music.music.title}: ${result.message}")
                }
            }
            
            delay(delayMillis)
        }
        
        val processingResult = ProcessingResult(
            totalProcessed = successCount + skippedCount + failedCount,
            successCount = successCount,
            skippedCount = skippedCount,
            failedCount = failedCount,
            errors = errors,
            wasCancelled = _isCancelled.get()
        )
        
        onComplete(processingResult)
        Log.d(TAG, "Processing completed: $processingResult")
    }
    
    /**
     * 使用当前服务商处理音乐信息（返回结果）
     */
    private suspend fun getMusicExtraInfoWithCurrentProviderAndResult(input: MusicInfo): ExtraInfoResult {
        val providerConfig = settingsRepository.getCurrentProviderConfig()
        
        if (!providerConfig.isConfigured) {
            return ExtraInfoResult.Skipped
        }
        
        val result = musicRepository.fetchMusicExtraInfoWithProvider(
            providerConfig,
            input.music.title,
            input.music.artist
        )
        
        return when (result) {
            is com.example.hearablemusicplayer.data.repository.Result.Success -> {
                val intro = result.data
                musicRepository.insertMusicExtra(input.music.id, intro)
                saveMusicLabels(input.music.id, intro)
                Log.d(TAG, "Successfully processed music via ${providerConfig.type.displayName}")
                ExtraInfoResult.Success(intro)
            }
            is com.example.hearablemusicplayer.data.repository.Result.Error -> {
                Log.e(TAG, "Fetch extra info failed: ${result.exception.message}")
                ExtraInfoResult.Error(result.exception.message ?: "Unknown error")
            }
            is com.example.hearablemusicplayer.data.repository.Result.Loading -> {
                ExtraInfoResult.Skipped
            }
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
