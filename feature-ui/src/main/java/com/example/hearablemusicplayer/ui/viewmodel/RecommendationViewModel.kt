package com.example.hearablemusicplayer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.domain.model.DailyMusicInfo
import com.example.hearablemusicplayer.domain.model.ListeningDuration
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.model.MusicLabel
import com.example.hearablemusicplayer.domain.usecase.music.GetAllMusicUseCase
import com.example.hearablemusicplayer.domain.usecase.music.GetDailyMusicRecommendationUseCase
import com.example.hearablemusicplayer.domain.usecase.playback.CurrentPlaybackUseCase
import com.example.hearablemusicplayer.domain.usecase.settings.UserSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val getDailyRecommendationUseCase: GetDailyMusicRecommendationUseCase,
    private val getAllMusicUseCase: GetAllMusicUseCase,
    private val userSettingsUseCase: UserSettingsUseCase,
    private val currentPlaybackUseCase: CurrentPlaybackUseCase
) : ViewModel() {

    // 每日推荐歌曲
    val dailyMusic = MutableStateFlow<MusicInfo?>(null)
    private val _dailyMusicInfo = MutableStateFlow<DailyMusicInfo?>(null)
    val dailyMusicInfo: StateFlow<DailyMusicInfo?> = _dailyMusicInfo
    private val _dailyMusicLabel = MutableStateFlow<List<MusicLabel?>>(emptyList())
    val dailyMusicLabel: StateFlow<List<MusicLabel?>> = _dailyMusicLabel

    // 心动歌单（相似歌曲）
    private val _heartbeatList = MutableStateFlow<List<MusicInfo>>(emptyList())
    val heartbeatList: StateFlow<List<MusicInfo>> = _heartbeatList
    
    // 待处理音乐数量
    val pendingMusicCount: StateFlow<Int> = getAllMusicUseCase
        .getMusicWithMissingExtraCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 批量处理进度
    data class BatchProcessingProgress(
        val totalCount: Int = 0,
        val processedCount: Int = 0,
        val currentMusicTitle: String = "",
        val isProcessing: Boolean = false,
        val isPaused: Boolean = false
    ) {
        val progressPercent: Float
            get() = if (totalCount > 0) processedCount.toFloat() / totalCount else 0f
    }
    
    private val _processingProgress = MutableStateFlow(BatchProcessingProgress())
    val processingProgress: StateFlow<BatchProcessingProgress> = _processingProgress
    
    private val _processingResult = MutableStateFlow<GetDailyMusicRecommendationUseCase.ProcessingResult?>(null)
    val processingResult: StateFlow<GetDailyMusicRecommendationUseCase.ProcessingResult?> = _processingResult
    
    private val _isProcessingExtraInfo = MutableStateFlow(false)
    val isProcessingExtraInfo: StateFlow<Boolean> = _isProcessingExtraInfo

    // 收听时长
    val recentListeningDurations: StateFlow<List<ListeningDuration>> = getDailyRecommendationUseCase
        .getRecentListeningDurations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 获取每日推荐音乐
     */
    fun getDailyMusicInfo() {
        viewModelScope.launch {
            // 先增加启动计数
            userSettingsUseCase.incrementAppLaunchCount()
            
            // 检查是否需要刷新
            val shouldRefresh = userSettingsUseCase.shouldRefreshDailyRecommendation()
            val config = userSettingsUseCase.getDailyRefreshConfig()
            
            Log.d("RecommendationViewModel", """
                刷新检查:
                - 刷新模式: ${config.mode}
                - 上次刷新时间: ${config.lastRefreshTimestamp}
                - 当前时间: ${System.currentTimeMillis()}
                - 启动次数: ${config.launchCountSinceRefresh}
                - 需要刷新: $shouldRefresh
            """.trimIndent())
            
            if (shouldRefresh) {
                refreshDailyMusicInfo()
            } else {
                if (dailyMusic.value == null) {
                    val savedMusicId = userSettingsUseCase.getCurrentDailyMusicId()
                    if (savedMusicId != null && savedMusicId > 0) {
                        val recommendation = getDailyRecommendationUseCase.getMusicWithExtraById(savedMusicId)
                        if (recommendation?.musicInfo != null) {
                            dailyMusic.value = recommendation.musicInfo
                            _dailyMusicInfo.value = recommendation.dailyMusicInfo
                            _dailyMusicLabel.value = recommendation.labels
                        } else {
                            refreshDailyMusicInfo()
                        }
                    } else {
                        refreshDailyMusicInfo()
                    }
                }
            }
        }
    }
    
    /**
     * 手动刷新每日推荐
     */
    fun refreshDailyMusicInfo() {
        viewModelScope.launch {
            val recommendation = getDailyRecommendationUseCase.getRandomMusicWithExtra()
            dailyMusic.value = recommendation.musicInfo
            _dailyMusicInfo.value = recommendation.dailyMusicInfo
            _dailyMusicLabel.value = recommendation.labels
            
            recommendation.musicInfo?.music?.id?.let { musicId ->
                userSettingsUseCase.saveCurrentDailyMusicId(musicId)
            }
            userSettingsUseCase.updateLastDailyRefreshTimestamp()
        }
    }
    
    /**
     * 暂停处理
     */
    fun pauseProcessing() {
        getDailyRecommendationUseCase.pauseProcessing()
        _processingProgress.value = _processingProgress.value.copy(isPaused = true)
    }
    
    /**
     * 继续处理
     */
    fun resumeProcessing() {
        getDailyRecommendationUseCase.resumeProcessing()
        _processingProgress.value = _processingProgress.value.copy(isPaused = false)
    }
    
    /**
     * 取消处理
     */
    fun cancelProcessing() {
        getDailyRecommendationUseCase.cancelProcessing()
        _processingProgress.value = BatchProcessingProgress()
        _isProcessingExtraInfo.value = false
    }
    
    /**
     * 清除处理结果
     */
    fun clearProcessingResult() {
        _processingResult.value = null
    }
    
    /**
     * 开始自动处理
     */
    fun startAutoProcessWithCurrentProvider() {
        if (_isProcessingExtraInfo.value) return
        
        _isProcessingExtraInfo.value = true
        
        viewModelScope.launch {
            _processingResult.value = null
            
            try {
                val totalCount = pendingMusicCount.first()
                _processingProgress.value = BatchProcessingProgress(
                    totalCount = totalCount,
                    processedCount = 0,
                    isProcessing = true,
                    isPaused = false
                )
                
                getDailyRecommendationUseCase.autoProcessMissingExtraInfoWithCurrentProvider(
                    onProgress = { music ->
                        val current = _processingProgress.value
                        _processingProgress.value = current.copy(
                            processedCount = current.processedCount + 1,
                            currentMusicTitle = music.music.title
                        )
                    },
                    onComplete = { result ->
                        _processingResult.value = result
                    },
                    delayMillis = 500
                )
            } catch (e: Exception) {
                Log.e("RecommendationViewModel", "Error: ${e.message}")
            } finally {
                val current = _processingProgress.value
                _processingProgress.value = current.copy(isProcessing = false, isPaused = false)
                _isProcessingExtraInfo.value = false
            }
        }
    }
    
    init {
        getDailyRecommendationUseCase.resetProcessingState()
        _isProcessingExtraInfo.value = false
        _processingProgress.value = BatchProcessingProgress()
        
        // 监听每日推荐变化，自动获取相似歌曲
        viewModelScope.launch {
            dailyMusic.filterNotNull().collectLatest { music ->
                _heartbeatList.value = currentPlaybackUseCase.getSimilarSongsByWeightedLabels(music.music.id, 10)
            }
        }
    }
    
    fun selectSong(musicInfo: MusicInfo) {
        dailyMusic.value = musicInfo
        viewModelScope.launch {
            val recommendation = getDailyRecommendationUseCase.getMusicWithExtraById(musicInfo.music.id)
            _dailyMusicInfo.value = recommendation?.dailyMusicInfo
            _dailyMusicLabel.value = recommendation?.labels ?: emptyList()
        }
    }
}
