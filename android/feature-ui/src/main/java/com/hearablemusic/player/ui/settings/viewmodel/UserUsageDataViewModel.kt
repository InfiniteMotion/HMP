package com.hearablemusic.player.ui.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModelScope
import com.hmp.domain.setting.model.UserUsageAnalytics
import com.hmp.domain.setting.usecase.GetUserUsageDataUseCase
import com.hearablemusic.player.ui.common.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserUsageDataViewModel(
    private val application: Application,
    private val getUserUsageDataUseCase: GetUserUsageDataUseCase
) : AndroidViewModel(application) {

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
                _uiState.value = UiState.Error(e.message ?: application.getString(com.hearablemusic.player.ui.R.string.unknown_error))
            }
        }
    }
}