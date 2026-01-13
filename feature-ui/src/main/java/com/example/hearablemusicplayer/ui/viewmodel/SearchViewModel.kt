package com.example.hearablemusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.usecase.music.SearchMusicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMusicUseCase: SearchMusicUseCase
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<MusicInfo>>(emptyList())
    val searchResults: StateFlow<List<MusicInfo>> = _searchResults
    
    fun searchMusic(query: String) {
        viewModelScope.launch {
            _searchResults.value = searchMusicUseCase(query)
        }
    }
}
