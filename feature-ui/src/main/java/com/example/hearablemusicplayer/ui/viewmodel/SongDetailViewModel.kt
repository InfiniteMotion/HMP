package com.example.hearablemusicplayer.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.music.MusicLabel
import com.example.hearablemusicplayer.domain.music.usecase.GetDailyMusicRecommendationUseCase
import com.example.hearablemusicplayer.domain.setting.model.DailyMusicInfo
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SongDetailData(
    val musicInfo: MusicInfo,
    val dailyMusicInfo: DailyMusicInfo?,
    val labels: List<MusicLabel?>
)

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val getDailyRecommendationUseCase: GetDailyMusicRecommendationUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SongDetailData>>(UiState.Idle)
    val uiState: StateFlow<UiState<SongDetailData>> = _uiState.asStateFlow()

    private var currentMusicId: Long? = null

    init {
        try {
            val songDetail = savedStateHandle.toRoute<Routes.SongDetail>()
            loadSongDetail(songDetail.musicId)
        } catch (e: Exception) {
            _uiState.value = UiState.Error("Invalid navigation arguments")
        }
    }

    fun loadSongDetail(musicId: Long) {
        currentMusicId = musicId
        _uiState.value = UiState.Loading
        
        viewModelScope.launch {
            try {
                val recommendation = getDailyRecommendationUseCase.getMusicWithExtraById(musicId)
                if (recommendation?.musicInfo != null) {
                    _uiState.value = UiState.Success(
                        SongDetailData(
                            musicInfo = recommendation.musicInfo!!,
                            dailyMusicInfo = recommendation.dailyMusicInfo,
                            labels = recommendation.labels
                        )
                    )
                } else {
                    _uiState.value = UiState.Error("Music not found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun retry() {
        currentMusicId?.let { loadSongDetail(it) }
    }
}
