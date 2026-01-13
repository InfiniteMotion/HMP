package com.example.hearablemusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.domain.model.AiProviderConfig
import com.example.hearablemusicplayer.domain.model.enum.AiProviderType
import com.example.hearablemusicplayer.domain.usecase.music.GetDailyMusicRecommendationUseCase
import com.example.hearablemusicplayer.domain.usecase.settings.UserSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsUseCase: UserSettingsUseCase,
    private val getDailyRecommendationUseCase: GetDailyMusicRecommendationUseCase
) : ViewModel() {
    
    // User Info
    val isFirstLaunch = userSettingsUseCase.isFirstLaunch
    val userName = userSettingsUseCase.userName
    val customMode = userSettingsUseCase.customMode
    
    private val _avatarUri = MutableStateFlow("")
    val avatarUri: StateFlow<String> = _avatarUri
    
    fun getAvatarUri() {
        viewModelScope.launch {
            _avatarUri.value = userSettingsUseCase.getAvatarUri() ?: ""
        }
    }
    
    fun saveAvatarUri(uri: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveAvatarUri(uri)
        }
    }
    
    fun saveUserName(name: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveUserName(name)
        }
    }
    
    fun saveCustomMode(mode: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveThemeMode(mode)
        }
    }
    
    fun saveIsFirstLaunchStatus(status: Boolean) {
        viewModelScope.launch {
            userSettingsUseCase.saveIsFirstLaunch(status)
        }
    }

    fun saveIsLoadMusic(isLoad: Boolean) {
        viewModelScope.launch {
            userSettingsUseCase.saveIsLoadMusic(isLoad)
        }
    }
    
    // AI Config
    val currentAiProvider = userSettingsUseCase.currentAiProvider
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiProviderType.DEEPSEEK)
        
    private val _currentProviderConfig = MutableStateFlow<AiProviderConfig?>(null)
    val currentProviderConfig: StateFlow<AiProviderConfig?> = _currentProviderConfig
    
    fun loadCurrentProviderConfig() {
        viewModelScope.launch {
            _currentProviderConfig.value = userSettingsUseCase.getCurrentProviderConfig()
        }
    }
    
    fun loadProviderConfig(provider: AiProviderType) {
        viewModelScope.launch {
            _currentProviderConfig.value = userSettingsUseCase.getProviderConfig(provider)
        }
    }
    
    fun switchAiProvider(provider: AiProviderType) {
        viewModelScope.launch {
            userSettingsUseCase.setCurrentProvider(provider)
            loadProviderConfig(provider)
        }
    }
    
    fun saveAiProviderConfig(provider: AiProviderType, apiKey: String, model: String) {
        viewModelScope.launch {
            val config = AiProviderConfig(
                type = provider,
                apiKey = apiKey,
                model = model.ifBlank { provider.defaultModel },
                isConfigured = apiKey.isNotBlank()
            )
            userSettingsUseCase.saveProviderConfig(config)
            userSettingsUseCase.setCurrentProvider(provider)
            _currentProviderConfig.value = config
        }
    }
    
    // API Test
    sealed class ApiTestResult {
        data class Success(val message: String) : ApiTestResult()
        data class Error(val message: String) : ApiTestResult()
    }
    
    private val _apiTestResult = MutableStateFlow<ApiTestResult?>(null)
    val apiTestResult: StateFlow<ApiTestResult?> = _apiTestResult
    
    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi
    
    fun testAiProviderConnection(provider: AiProviderType, apiKey: String, model: String) {
        viewModelScope.launch {
            _isTestingApi.value = true
            _apiTestResult.value = null
            
            try {
                val config = AiProviderConfig(
                    type = provider,
                    apiKey = apiKey,
                    model = model.ifBlank { provider.defaultModel },
                    isConfigured = true
                )
                
                val isValid = getDailyRecommendationUseCase.validateProviderApiKey(config)
                _apiTestResult.value = if (isValid) {
                    ApiTestResult.Success("可以访问 ${provider.displayName}")
                } else {
                    ApiTestResult.Error("API Key 无效")
                }
            } catch (e: Exception) {
                _apiTestResult.value = ApiTestResult.Error("测试失败: ${e.message}")
            } finally {
                _isTestingApi.value = false
            }
        }
    }
    
    fun clearApiTestResult() {
        _apiTestResult.value = null
    }
    
    // Auto Batch Process
    val autoBatchProcess = userSettingsUseCase.autoBatchProcess
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    fun saveAutoBatchProcess(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsUseCase.saveAutoBatchProcess(enabled)
        }
    }
    
    // Daily Refresh Strategy
    val dailyRefreshMode = userSettingsUseCase.dailyRefreshMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "time")
        
    val dailyRefreshHours = userSettingsUseCase.dailyRefreshHours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 24)
        
    val dailyRefreshStartupCount = userSettingsUseCase.dailyRefreshStartupCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)
        
    fun saveDailyRefreshMode(mode: String) {
        viewModelScope.launch { userSettingsUseCase.saveDailyRefreshMode(mode) }
    }
    
    fun saveDailyRefreshHours(hours: Int) {
        viewModelScope.launch { userSettingsUseCase.saveDailyRefreshHours(hours) }
    }
    
    fun saveDailyRefreshStartupCount(count: Int) {
        viewModelScope.launch { userSettingsUseCase.saveDailyRefreshStartupCount(count) }
    }
    
    init {
        loadCurrentProviderConfig()
    }
}
