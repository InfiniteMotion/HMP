package com.hmp.domain.music.usecase

import com.hmp.domain.setting.model.AiProviderConfig
import com.hmp.domain.setting.model.DailyMusicInfo
import com.hmp.domain.setting.model.ListeningDuration
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicLabel
import com.hmp.domain.music.MusicRepository
import com.hmp.domain.setting.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 每日AI音乐推荐Use Case
 */
class GetDailyMusicRecommendationUseCase(
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository,
    private val musicLabelUseCase: MusicLabelUseCase
) {

    // ==================== 处理控制标志 ====================

    private val _isPaused = AtomicBoolean(false)
    private val _isCancelled = AtomicBoolean(false)

    fun pauseProcessing() {
        _isPaused.set(true)
        println("[DBG] Processing paused")
    }

    fun resumeProcessing() {
        _isPaused.set(false)
        println("[DBG] Processing resumed")
    }

    fun cancelProcessing() {
        _isCancelled.set(true)
        _isPaused.set(false)
        println("[DBG] Processing cancelled")
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
            val musicInfo = withTimeoutOrNull(2000) {
                musicRepository.getMusicInfoById(musicId).firstOrNull()
            } ?: run {
                println("[WRN] getMusicWithExtraById: Timeout or null for id $musicId")
                return null
            }

            val dailyMusicInfo = musicRepository.getMusicExtraById(musicId)
            val labels = musicRepository.getMusicLabels(musicId)
            return MusicRecommendation(musicInfo, dailyMusicInfo, labels)
        } catch (e: Exception) {
            println("[ERR] Error fetching music by id: $musicId")
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
            println("[WRN] No AI provider configured, skipping auto process")
            onComplete(ProcessingResult())
            return
        }

        var successCount = 0
        var skippedCount = 0
        var failedCount = 0
        val errors = mutableListOf<String>()

        while (true) {
            if (_isCancelled.get()) {
                println("[DBG] Processing cancelled by user")
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
        println("[DBG] Processing completed: $processingResult")
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
                println("[DBG] Successfully processed music via ${providerConfig.type.displayName}")
                ExtraInfoResult.Success(intro)
            },
            onFailure = { exception ->
                println("[ERR] Fetch extra info failed: ${exception.message}")
                ExtraInfoResult.Error(exception.message ?: "Unknown error")
            }
        )
    }

    fun getRecentListeningDurations(): Flow<List<ListeningDuration>> {
        return musicRepository.getRecentListeningDurations(35)
    }
}
