package com.hearablemusic.player.ui.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hearablemusic.player.ui.R
import androidx.lifecycle.viewModelScope
import com.hmp.domain.backup.usecase.DeleteBackupUseCase
import com.hmp.domain.backup.usecase.ExportUserDataBackupUseCase
import com.hmp.domain.backup.usecase.GetBackupsUseCase
import com.hmp.domain.backup.usecase.ImportUserDataBackupUseCase
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.config.LyricsConfig
import com.hmp.domain.music.usecase.GetDailyMusicRecommendationUseCase
import com.hmp.domain.setting.model.AiAccessMode
import com.hmp.domain.setting.model.AiEndpointConfig
import com.hmp.domain.setting.usecase.LyricsSettingsUseCase
import com.hmp.domain.setting.usecase.UserSettingsUseCase
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_BLUR_RADIUS
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_NOISE_FACTOR
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_TINT_ALPHA
import com.hearablemusic.player.ui.common.util.HAZE_MODE_CUSTOM
import com.hearablemusic.player.ui.common.util.HAZE_MODE_PRESET
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val userSettingsUseCase: UserSettingsUseCase,
    private val lyricsSettingsUseCase: LyricsSettingsUseCase,
    private val getDailyRecommendationUseCase: GetDailyMusicRecommendationUseCase,
    private val exportUserDataBackupUseCase: ExportUserDataBackupUseCase,
    private val importUserDataBackupUseCase: ImportUserDataBackupUseCase,
    private val getBackupsUseCase: GetBackupsUseCase,
    private val deleteBackupUseCase: DeleteBackupUseCase
) : AndroidViewModel(application) {

    // User Info
    val isFirstLaunch = userSettingsUseCase.isFirstLaunch
    val isLoadMusic = userSettingsUseCase.isLoadMusic
    val userName = userSettingsUseCase.userName
    val customMode = userSettingsUseCase.customMode
    val backgroundStyle = userSettingsUseCase.backgroundStyle
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), "FLUID")
    val hazeMode = userSettingsUseCase.hazeMode
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), HAZE_MODE_CUSTOM)
    val hazeMaterialPreset = userSettingsUseCase.hazeMaterialPreset
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), "regular")
    val hazeBlurRadius = userSettingsUseCase.hazeBlurRadius
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000),
            DEFAULT_HAZE_BLUR_RADIUS
        )
    val hazeNoiseFactor = userSettingsUseCase.hazeNoiseFactor
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000),
            DEFAULT_HAZE_NOISE_FACTOR
        )
    val hazeTintAlpha = userSettingsUseCase.hazeTintAlpha
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000),
            DEFAULT_HAZE_TINT_ALPHA
        )
    val hazeIntensity = userSettingsUseCase.hazeIntensity
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), 0.6f)

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

    fun saveBackgroundStyle(style: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveBackgroundStyle(style)
        }
    }

    fun saveHazeMode(mode: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveHazeMode(mode)
        }
    }

    fun saveHazeMaterialPreset(preset: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveHazeMaterialPreset(preset)
        }
    }

    fun saveHazeBlurRadius(radius: Float) {
        viewModelScope.launch {
            userSettingsUseCase.saveHazeBlurRadius(radius)
        }
    }

    fun saveHazeNoiseFactor(noiseFactor: Float) {
        viewModelScope.launch {
            userSettingsUseCase.saveHazeNoiseFactor(noiseFactor)
        }
    }

    fun saveHazeTintAlpha(alpha: Float) {
        viewModelScope.launch {
            userSettingsUseCase.saveHazeTintAlpha(alpha)
        }
    }

    fun applyHazeMaterialPreset(preset: String, intensity: Float) {
        viewModelScope.launch {
            userSettingsUseCase.saveHazeMode(HAZE_MODE_PRESET)
            userSettingsUseCase.saveHazeMaterialPreset(preset)
            userSettingsUseCase.saveHazeIntensity(intensity)
        }
    }

    fun saveHazeIntensity(intensity: Float) {
        viewModelScope.launch {
            userSettingsUseCase.saveHazeMode(HAZE_MODE_CUSTOM)
            userSettingsUseCase.saveHazeIntensity(intensity)
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
    val aiAccessMode = userSettingsUseCase.aiAccessMode
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), AiAccessMode.FREE)

    val aiFreeTrialRemainingCount = userSettingsUseCase.aiFreeTrialRemainingCount
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), 100)

    private val _customAiConfig = MutableStateFlow(AiEndpointConfig())
    val customAiConfig: StateFlow<AiEndpointConfig> = _customAiConfig

    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels

    fun loadCustomAiConfig() {
        viewModelScope.launch {
            _customAiConfig.value = userSettingsUseCase.getCustomAiConfig()
        }
    }

    fun switchAiAccessMode(mode: AiAccessMode) {
        viewModelScope.launch {
            userSettingsUseCase.saveAiAccessMode(mode)
        }
    }

    fun saveCustomAiConfig(endpoint: String, apiKey: String, model: String) {
        viewModelScope.launch {
            val config = AiEndpointConfig(
                endpoint = endpoint,
                apiKey = apiKey,
                selectedModel = model,
                isConfigured = endpoint.isNotBlank() && apiKey.isNotBlank()
            )
            userSettingsUseCase.saveCustomAiConfig(config)
            _customAiConfig.value = config
        }
    }

    fun fetchAvailableModels(endpoint: String, apiKey: String) {
        viewModelScope.launch {
            _isTestingApi.value = true
            try {
                val config = AiEndpointConfig(endpoint = endpoint, apiKey = apiKey, isConfigured = true)
                val result = getDailyRecommendationUseCase.fetchModels(config)
                result.onSuccess { models ->
                    _availableModels.value = models
                    _apiTestResult.value = ApiTestResult.Success(getApplication<Application>().getString(R.string.fetched_n_models, models.size))
                }.onFailure { e ->
                    _apiTestResult.value = ApiTestResult.Error(getApplication<Application>().getString(R.string.fetch_models_failed, e.message ?: ""))
                }
            } catch (e: Exception) {
                _apiTestResult.value = ApiTestResult.Error(getApplication<Application>().getString(R.string.fetch_models_failed, e.message ?: ""))
            } finally {
                _isTestingApi.value = false
            }
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

    // ==================== 歌词配置相关 ====================

    // 文本大小配置
    val lyricsOriginalTextSize = lyricsSettingsUseCase.originalTextSize
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), 14)

    val lyricsTranslatedTextSize = lyricsSettingsUseCase.translatedTextSize
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), 14)

    val lyricsCurrentTimeTextSize = lyricsSettingsUseCase.currentTimeTextSize
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), 16)

    // 间距配置
    val lyricsLineSpacing = lyricsSettingsUseCase.lineSpacing
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), 6)

    // 显示模式配置
    val lyricsDisplayMode = lyricsSettingsUseCase.displayMode
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), DisplayMode.DUAL)

    // 对齐配置
    val lyricsAlignment = lyricsSettingsUseCase.alignment
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), LyricsAlignment.CENTER)

    // 逐字（卡拉 OK）显示开关
    val lyricsKaraokeEnabled = lyricsSettingsUseCase.karaokeEnabled
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), true)

    // ==================== 歌词配置操作方法 ====================

    fun saveLyricsOriginalTextSize(size: Int) {
        viewModelScope.launch {
            lyricsSettingsUseCase.saveOriginalTextSize(size)
        }
    }

    fun saveLyricsTranslatedTextSize(size: Int) {
        viewModelScope.launch {
            lyricsSettingsUseCase.saveTranslatedTextSize(size)
        }
    }

    fun saveLyricsCurrentTimeTextSize(size: Int) {
        viewModelScope.launch {
            lyricsSettingsUseCase.saveCurrentTimeTextSize(size)
        }
    }

    fun saveLyricsLineSpacing(spacing: Int) {
        viewModelScope.launch {
            lyricsSettingsUseCase.saveLineSpacing(spacing)
        }
    }

    fun saveLyricsDisplayMode(mode: DisplayMode) {
        viewModelScope.launch {
            lyricsSettingsUseCase.saveDisplayMode(mode)
        }
    }

    fun saveLyricsAlignment(alignment: LyricsAlignment) {
        viewModelScope.launch {
            lyricsSettingsUseCase.saveAlignment(alignment)
        }
    }

    fun saveLyricsKaraokeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            lyricsSettingsUseCase.saveKaraokeEnabled(enabled)
        }
    }

    /**
     * 获取完整的歌词配置
     */
    fun getLyricsConfig(): LyricsConfig {
        return LyricsConfig(
            originalTextSize = lyricsOriginalTextSize.value,
            translatedTextSize = lyricsTranslatedTextSize.value,
            currentTimeTextSize = lyricsCurrentTimeTextSize.value,
            lineSpacing = lyricsLineSpacing.value,
            displayMode = lyricsDisplayMode.value,
            alignment = lyricsAlignment.value,
            karaokeEnabled = lyricsKaraokeEnabled.value
        )
    }

    /**
     * 保存完整的歌词配置
     */
    fun saveLyricsConfig(config: LyricsConfig) {
        viewModelScope.launch {
            lyricsSettingsUseCase.saveLyricsConfig(config)
        }
    }

    /**
     * 重置歌词配置为默认值
     */
    fun resetLyricsConfig() {
        viewModelScope.launch {
            lyricsSettingsUseCase.resetToDefault()
        }
    }

    fun testAiConnection(endpoint: String, apiKey: String) {
        viewModelScope.launch {
            _isTestingApi.value = true
            _apiTestResult.value = null

            try {
                val config = AiEndpointConfig(
                    endpoint = endpoint,
                    apiKey = apiKey,
                    isConfigured = true
                )

                val isValid = getDailyRecommendationUseCase.validateProviderApiKey(config)
                _apiTestResult.value = if (isValid) {
                    ApiTestResult.Success(getApplication<Application>().getString(R.string.connected))
                } else {
                    ApiTestResult.Error(getApplication<Application>().getString(R.string.api_key_invalid))
                }
            } catch (e: Exception) {
                _apiTestResult.value = ApiTestResult.Error(getApplication<Application>().getString(R.string.test_failed, e.message ?: ""))
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
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), false)

    fun saveAutoBatchProcess(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsUseCase.saveAutoBatchProcess(enabled)
        }
    }

    // Daily Refresh Strategy
    val dailyRefreshMode = userSettingsUseCase.dailyRefreshMode
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), "time")

    val dailyRefreshHours = userSettingsUseCase.dailyRefreshHours
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), 24)

    val dailyRefreshStartupCount = userSettingsUseCase.dailyRefreshStartupCount
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), 3)

    fun saveDailyRefreshMode(mode: String) {
        viewModelScope.launch { userSettingsUseCase.saveDailyRefreshMode(mode) }
    }

    fun saveDailyRefreshHours(hours: Int) {
        viewModelScope.launch { userSettingsUseCase.saveDailyRefreshHours(hours) }
    }

    fun saveDailyRefreshStartupCount(count: Int) {
        viewModelScope.launch { userSettingsUseCase.saveDailyRefreshStartupCount(count) }
    }

    // Backup & Restore
    private val _backupResult = MutableStateFlow<String?>(null)
    val backupResult: StateFlow<String?> = _backupResult

    private val _localBackups = MutableStateFlow<List<String>>(emptyList())
    val localBackups: StateFlow<List<String>> = _localBackups

    fun loadLocalBackups() {
        viewModelScope.launch {
            getBackupsUseCase()
                .onSuccess { _localBackups.value = it }
        }
    }

    fun deleteLocalBackup(filePath: String) {
        viewModelScope.launch {
            deleteBackupUseCase(filePath)
                .onSuccess { loadLocalBackups() }
        }
    }

    fun clearBackupResult() {
        _backupResult.value = null
    }

    fun exportBackup(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            exportUserDataBackupUseCase()
                .onSuccess { filePath ->
                    _backupResult.value = getApplication<Application>().getString(R.string.backup_desc) + ": $filePath"
                    loadLocalBackups()
                    onSuccess(filePath)
                }
                .onFailure { e ->
                    _backupResult.value = getApplication<Application>().getString(R.string.backup_failed, e.message ?: "")
                    onError(e.message ?: getApplication<Application>().getString(R.string.unknown_error))
                }
        }
    }

    fun restoreBackup(filePath: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
            viewModelScope.launch {
            importUserDataBackupUseCase(filePath)
                .onSuccess {
                    _backupResult.value = getApplication<Application>().getString(R.string.restore_success)
                    onSuccess()
                }
                .onFailure { e ->
                    _backupResult.value = getApplication<Application>().getString(R.string.restore_failed, e.message ?: "")
                    onError(e.message ?: getApplication<Application>().getString(R.string.unknown_error))
                }
            }
    }

    init {
        loadCustomAiConfig()
        loadLocalBackups()
        getAvatarUri()
    }
}
