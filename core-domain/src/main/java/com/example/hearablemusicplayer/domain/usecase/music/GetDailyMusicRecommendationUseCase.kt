package com.example.hearablemusicplayer.domain.usecase.music

import android.util.Log
import com.example.hearablemusicplayer.domain.model.*
import com.example.hearablemusicplayer.domain.repository.MusicRepository
import com.example.hearablemusicplayer.domain.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * 每日AI音乐推荐Use Case
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
    
    fun pauseProcessing() {
        _isPaused.set(true)
        Log.d(TAG, "Processing paused")
    }
    
    fun resumeProcessing() {
        _isPaused.set(false)
        Log.d(TAG, "Processing resumed")
    }
    
    fun cancelProcessing() {
        _isCancelled.set(true)
        _isPaused.set(false)
        Log.d(TAG, "Processing cancelled")
    }
    
    fun resetProcessingState() {
        _isPaused.set(false)
        _isCancelled.set(false)
    }
    
    fun isPaused(): Boolean = _isPaused.get()
    
    // ==================== 处理结果数据类 ====================
    
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
    
    sealed class ExtraInfoResult {
        data class Success(val intro: DailyMusicInfo) : ExtraInfoResult()
        data object Skipped : ExtraInfoResult()
        data class Error(val message: String) : ExtraInfoResult()
    }
    
    data class MusicRecommendation(
        val musicInfo: MusicInfo?,
        val dailyMusicInfo: DailyMusicInfo?,
        val labels: List<MusicLabel?>
    )
    
    suspend fun getRandomMusicWithExtra(): MusicRecommendation {
        val musicInfo = musicRepository.getRandomMusicInfoWithExtra()
        val dailyMusicInfo = musicInfo?.music?.id?.let { musicRepository.getMusicExtraById(it) }
        val labels = musicInfo?.music?.id?.let { musicRepository.getMusicLabels(it) } ?: emptyList()
        return MusicRecommendation(musicInfo, dailyMusicInfo, labels)
    }

    suspend fun getMusicWithExtraById(musicId: Long): MusicRecommendation? {
        try {
            // 使用超时保护，防止数据库查询无结果时 Flow 挂起
            val musicInfo = withTimeoutOrNull(2000) {
                musicRepository.getMusicInfoById(musicId).firstOrNull()
            } ?: run {
                Log.w(TAG, "getMusicWithExtraById: Timeout or null for id $musicId")
                return null
            }
            
            val dailyMusicInfo = musicRepository.getMusicExtraById(musicId)
            val labels = musicRepository.getMusicLabels(musicId)
            return MusicRecommendation(musicInfo, dailyMusicInfo, labels)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching music by id: $musicId", e)
            return null
        }
    }
    
    // ==================== 多服务商支持方法 ====================
    
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
    
    suspend fun validateProviderApiKey(providerConfig: AiProviderConfig): Boolean {
        return musicRepository.validateProviderApiKey(providerConfig).getOrDefault(false)
    }
    
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
            if (_isCancelled.get()) {
                Log.d(TAG, "Processing cancelled by user")
                break
            }
            
            while (_isPaused.get()) {
                delay(100)
                if (_isCancelled.get()) break
            }
            
            if (_isCancelled.get()) break
            
            val music = musicRepository.getRandomMusicInfoWithMissingExtra() ?: break
            
            onProgress(music)
            
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
        
        return result.fold(
            onSuccess = { intro ->
                musicRepository.insertMusicExtra(input.music.id, intro)
                saveMusicLabels(input.music.id, intro)
                Log.d(TAG, "Successfully processed music via ${providerConfig.type.displayName}")
                ExtraInfoResult.Success(intro)
            },
            onFailure = { exception ->
                Log.e(TAG, "Fetch extra info failed: ${exception.message}")
                ExtraInfoResult.Error(exception.message ?: "Unknown error")
            }
        )
    }
    
    fun getRecentListeningDurations(): Flow<List<ListeningDuration>> {
        // 请求最近35天的数据（5周 x 7天），以支持月视图热力图
        return musicRepository.getRecentListeningDurations(35)
    }
}
