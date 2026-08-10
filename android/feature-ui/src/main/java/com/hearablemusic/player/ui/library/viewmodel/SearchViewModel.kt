package com.hearablemusic.player.ui.library.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hearablemusic.player.ui.R
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.usecase.SearchMusicUseCase
import com.hearablemusic.player.ui.common.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    application: Application,
    private val searchMusicUseCase: SearchMusicUseCase
) : AndroidViewModel(application) {

    private val _searchState = MutableStateFlow<UiState<List<MusicInfo>>>(UiState.Idle)
    val searchState: StateFlow<UiState<List<MusicInfo>>> = _searchState

    fun searchMusic(query: String) {
        if (query.isBlank()) {
            _searchState.value = UiState.Idle
            return
        }
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            try {
                val results = searchMusicUseCase(query)
                _searchState.value = if (results.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(results)
                }
            } catch (e: Exception) {
                _searchState.value = UiState.Error(e.message ?: getApplication<Application>().getString(R.string.search_failed))
            }
        }
    }
}