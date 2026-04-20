package com.example.hearablemusicplayer.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmp.domain.setting.model.UserUsageAnalytics
import com.hmp.domain.setting.usecase.GetUserUsageDataUseCase
import com.example.hearablemusicplayer.ui.common.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserUsageDataViewModel @Inject constructor(
    private val getUserUsageDataUseCase: GetUserUsageDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<UserUsageAnalytics>>(UiState.Loading)
    val uiState: StateFlow<UiState<UserUsageAnalytics>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val analytics = getUserUsageDataUseCase.getAnalytics()
                _uiState.value = UiState.Success(analytics)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}