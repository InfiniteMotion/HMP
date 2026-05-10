package com.hmp.desktop.ui.library.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.usecase.SearchMusicUseCase
import com.hmp.desktop.ui.common.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchMusicUseCase: SearchMusicUseCase
) : ViewModel() {

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
                _searchState.value = UiState.Error(e.message ?: "Search failed")
            }
        }
    }
}