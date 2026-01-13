package com.example.hearablemusicplayer.domain.usecase.settings

import com.example.hearablemusicplayer.domain.repository.SettingsRepository
import com.example.hearablemusicplayer.domain.model.DailyRefreshConfig
import com.example.hearablemusicplayer.domain.model.AiProviderConfig
import com.example.hearablemusicplayer.domain.model.enum.AiProviderType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 用户设置管理
 * Use Case: 封装用户相关设置的读写逻辑
 */
class UserSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    // 首次启动状态
    val isFirstLaunch: Flow<Boolean> = settingsRepository.isFirstLaunch
    
    suspend fun saveIsFirstLaunch(status: Boolean) {
        settingsRepository.saveIsFirstLaunch(status)
    }
    
    // 用户名
    val userName: Flow<String> = settingsRepository.userName
    suspend fun saveUserName(name: String) {
        settingsRepository.saveUserName(name)
    }

    // 主题明暗模式
    val customMode: Flow<String> = settingsRepository.themeMode
    suspend fun saveThemeMode(mode: String) {
        settingsRepository.saveThemeMode(mode)
    }
    
    // 头像URI
    suspend fun getAvatarUri(): String? {
        return settingsRepository.getAvatarUri()
    }
    
    suspend fun saveAvatarUri(uri: String) {
        settingsRepository.saveAvatarUri(uri)
    }
    
    // 音乐加载状态
    val isLoadMusic: Flow<Boolean> = settingsRepository.isLoadMusic
    
    suspend fun saveIsLoadMusic(status: Boolean) {
        settingsRepository.saveIsLoadMusic(status)
    }
    
    // ==================== 多 AI 服务商配置 ====================
    
    val currentAiProvider: Flow<AiProviderType> = settingsRepository.currentAiProvider
    
    suspend fun getCurrentProvider(): AiProviderType {
        return settingsRepository.getCurrentProvider()
    }
    
    suspend fun setCurrentProvider(provider: AiProviderType) {
        settingsRepository.setCurrentProvider(provider)
    }
    
    suspend fun getProviderConfig(provider: AiProviderType): AiProviderConfig {
        return settingsRepository.getProviderConfig(provider)
    }
    
    suspend fun getCurrentProviderConfig(): AiProviderConfig {
        return settingsRepository.getCurrentProviderConfig()
    }
    
    suspend fun saveProviderConfig(config: AiProviderConfig) {
        settingsRepository.saveProviderConfig(config)
    }
    
    suspend fun saveProviderApiKey(provider: AiProviderType, apiKey: String) {
        settingsRepository.setProviderApiKey(provider, apiKey)
    }
    
    suspend fun saveProviderModel(provider: AiProviderType, model: String) {
        settingsRepository.setProviderModel(provider, model)
    }
    
    suspend fun isProviderConfigured(provider: AiProviderType): Boolean {
        return settingsRepository.isProviderConfigured(provider)
    }
    
    suspend fun getConfiguredProviders(): List<AiProviderType> {
        return settingsRepository.getConfiguredProviders()
    }
    
    // ==================== AI 自动批量处理设置 ====================
    
    val autoBatchProcess: Flow<Boolean> = settingsRepository.autoBatchProcess
    
    suspend fun saveAutoBatchProcess(enabled: Boolean) {
        settingsRepository.saveAutoBatchProcess(enabled)
    }
    
    // ==================== 每日推荐刷新策略 ====================
    
    val dailyRefreshMode: Flow<String> = settingsRepository.dailyRefreshMode
    
    val dailyRefreshHours: Flow<Int> = settingsRepository.dailyRefreshHours
    
    val dailyRefreshStartupCount: Flow<Int> = settingsRepository.dailyRefreshStartupCount
    
    val lastDailyRefreshTimestamp: Flow<Long> = settingsRepository.lastDailyRefreshTimestamp
    
    val appLaunchCountSinceRefresh: Flow<Int> = settingsRepository.appLaunchCountSinceRefresh
    
    suspend fun saveDailyRefreshMode(mode: String) {
        settingsRepository.saveDailyRefreshMode(mode)
    }
    
    suspend fun saveDailyRefreshHours(hours: Int) {
        settingsRepository.saveDailyRefreshHours(hours)
    }
    
    suspend fun saveDailyRefreshStartupCount(count: Int) {
        settingsRepository.saveDailyRefreshStartupCount(count)
    }
    
    suspend fun updateLastDailyRefreshTimestamp() {
        settingsRepository.updateLastDailyRefreshTimestamp()
    }
    
    suspend fun saveCurrentDailyMusicId(musicId: Long) {
        settingsRepository.saveCurrentDailyMusicId(musicId)
    }
    
    suspend fun getCurrentDailyMusicId(): Long? {
        return settingsRepository.getCurrentDailyMusicId()
    }
    
    suspend fun incrementAppLaunchCount() {
        settingsRepository.incrementAppLaunchCount()
    }
    
    suspend fun getDailyRefreshConfig(): DailyRefreshConfig {
        return settingsRepository.getDailyRefreshConfig()
    }
    
    suspend fun shouldRefreshDailyRecommendation(): Boolean {
        val config = getDailyRefreshConfig()
        val currentTime = System.currentTimeMillis()
        
        if (config.lastRefreshTimestamp == 0L) {
            return true
        }
        
        return when (config.mode) {
            "time" -> {
                val hoursSinceRefresh = (currentTime - config.lastRefreshTimestamp) / (1000 * 60 * 60)
                hoursSinceRefresh >= config.refreshHours
            }
            "startup" -> {
                config.launchCountSinceRefresh >= config.startupCount
            }
            "smart" -> {
                val hoursSinceRefresh = (currentTime - config.lastRefreshTimestamp) / (1000 * 60 * 60)
                hoursSinceRefresh >= 24
            }
            else -> false
        }
    }
}
