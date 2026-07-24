package com.hearablemusic.player.ui.library.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.hearablemusic.player.ui.R
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicLabel
import com.hmp.domain.music.usecase.GetDailyMusicRecommendationUseCase
import com.hmp.domain.setting.model.DailyMusicInfo
import com.hmp.domain.setting.model.PlaybackHistory
import com.hmp.domain.setting.usecase.PlaybackHistoryUseCase
import com.hearablemusic.player.ui.common.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SongDetailData(
    val musicInfo: MusicInfo,
    val dailyMusicInfo: DailyMusicInfo?,
    val labels: List<MusicLabel?>,
    val playbackHistory: List<PlaybackHistory> = emptyList()
)

class SongDetailViewModel(
    application: Application,
    private val getDailyRecommendationUseCase: GetDailyMusicRecommendationUseCase,
    private val playbackHistoryUseCase: PlaybackHistoryUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState<SongDetailData>>(UiState.Idle)
    val uiState: StateFlow<UiState<SongDetailData>> = _uiState.asStateFlow()

    private var currentMusicId: Long? = null

    fun loadSongDetail(musicId: Long) {
        currentMusicId = musicId
        _uiState.value = UiState.Loading
        
        viewModelScope.launch {
            try {
                val recommendation = getDailyRecommendationUseCase.getMusicWithExtraById(musicId)
                if (recommendation?.musicInfo != null) {
                    val historyFlow = playbackHistoryUseCase.getPlaybackHistory(musicId, 5)
                    
                    historyFlow.collectLatest { history ->
                        _uiState.value = UiState.Success(
                            SongDetailData(
                                musicInfo = recommendation.musicInfo!!,
                                dailyMusicInfo = recommendation.dailyMusicInfo,
                                labels = recommendation.labels,
                                playbackHistory = history
                            )
                        )
                    }
                } else {
                    _uiState.value = UiState.Error(getApplication<Application>().getString(R.string.music_not_found))
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: getApplication<Application>().getString(R.string.unknown_error))
            }
        }
    }

    fun retry() {
        currentMusicId?.let { loadSongDetail(it) }
    }
}