package com.example.hearablemusicplayer.domain.usecase.settings

import com.example.hearablemusicplayer.data.repository.SettingsRepository
import com.example.hearablemusicplayer.data.repository.DailyRefreshConfig
import com.example.hearablemusicplayer.data.model.AiProviderType
import com.example.hearablemusicplayer.data.model.AiProviderConfig
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
    val userName: Flow<String?> = settingsRepository.userName
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
    
    /**
     * 获取当前选中的 AI 服务商类型 Flow
     */
    val currentAiProvider: Flow<AiProviderType> = settingsRepository.currentAiProvider
    
    /**
     * 获取当前服务商类型
     */
    suspend fun getCurrentProvider(): AiProviderType {
        return settingsRepository.getCurrentProvider()
    }
    
    /**
     * 设置当前 AI 服务商
     */
    suspend fun setCurrentProvider(provider: AiProviderType) {
        settingsRepository.setCurrentProvider(provider)
    }
    
    /**
     * 获取指定服务商的配置
     */
    suspend fun getProviderConfig(provider: AiProviderType): AiProviderConfig {
        return settingsRepository.getProviderConfig(provider)
    }
    
    /**
     * 获取当前服务商的配置
     */
    suspend fun getCurrentProviderConfig(): AiProviderConfig {
        return settingsRepository.getCurrentProviderConfig()
    }
    
    /**
     * 保存服务商配置
     */
    suspend fun saveProviderConfig(config: AiProviderConfig) {
        settingsRepository.saveProviderConfig(config)
    }
    
    /**
     * 保存服务商 API Key
     */
    suspend fun saveProviderApiKey(provider: AiProviderType, apiKey: String) {
        settingsRepository.setProviderApiKey(provider, apiKey)
    }
    
    /**
     * 保存服务商模型名称
     */
    suspend fun saveProviderModel(provider: AiProviderType, model: String) {
        settingsRepository.setProviderModel(provider, model)
    }
    
    /**
     * 检查服务商是否已配置
     */
    suspend fun isProviderConfigured(provider: AiProviderType): Boolean {
        return settingsRepository.isProviderConfigured(provider)
    }
    
    /**
     * 获取所有已配置的服务商列表
     */
    suspend fun getConfiguredProviders(): List<AiProviderType> {
        return settingsRepository.getConfiguredProviders()
    }
    
    // ==================== AI 自动批量处理设置 ====================
    
    /**
     * 自动批量处理开关状态 Flow
     */
    val autoBatchProcess: Flow<Boolean> = settingsRepository.autoBatchProcess
    
    /**
     * 保存自动批量处理开关
     */
    suspend fun saveAutoBatchProcess(enabled: Boolean) {
        settingsRepository.saveAutoBatchProcess(enabled)
    }
    
    // ==================== 每日推荐刷新策略 ====================
    
    /**
     * 每日推荐刷新模式 Flow
     */
    val dailyRefreshMode: Flow<String> = settingsRepository.dailyRefreshMode
    
    /**
     * 按小时刷新间隔 Flow
     */
    val dailyRefreshHours: Flow<Int> = settingsRepository.dailyRefreshHours
    
    /**
     * 按启动次数刷新 Flow
     */
    val dailyRefreshStartupCount: Flow<Int> = settingsRepository.dailyRefreshStartupCount
    
    /**
     * 上次刷新时间戳 Flow
     */
    val lastDailyRefreshTimestamp: Flow<Long> = settingsRepository.lastDailyRefreshTimestamp
    
    /**
     * 自上次刷新后的启动次数 Flow
     */
    val appLaunchCountSinceRefresh: Flow<Int> = settingsRepository.appLaunchCountSinceRefresh
    
    /**
     * 保存每日推荐刷新模式
     */
    suspend fun saveDailyRefreshMode(mode: String) {
        settingsRepository.saveDailyRefreshMode(mode)
    }
    
    /**
     * 保存按小时刷新的间隔
     */
    suspend fun saveDailyRefreshHours(hours: Int) {
        settingsRepository.saveDailyRefreshHours(hours)
    }
    
    /**
     * 保存按启动次数刷新
     */
    suspend fun saveDailyRefreshStartupCount(count: Int) {
        settingsRepository.saveDailyRefreshStartupCount(count)
    }
    
    /**
     * 更新上次刷新时间戳
     */
    suspend fun updateLastDailyRefreshTimestamp() {
        settingsRepository.updateLastDailyRefreshTimestamp()
    }
    
    /**
     * 保存当前每日推荐的音乐ID
     */
    suspend fun saveCurrentDailyMusicId(musicId: Long) {
        settingsRepository.saveCurrentDailyMusicId(musicId)
    }
    
    /**
     * 获取当前每日推荐的音乐ID
     */
    suspend fun getCurrentDailyMusicId(): Long? {
        return settingsRepository.getCurrentDailyMusicId()
    }
    
    /**
     * 增加应用启动计数
     */
    suspend fun incrementAppLaunchCount() {
        settingsRepository.incrementAppLaunchCount()
    }
    
    /**
     * 获取所有刷新策略配置
     */
    suspend fun getDailyRefreshConfig(): DailyRefreshConfig {
        return settingsRepository.getDailyRefreshConfig()
    }
    
    /**
     * 判断是否需要刷新每日推荐
     * @return true 如果需要刷新
     */
    suspend fun shouldRefreshDailyRecommendation(): Boolean {
        val config = getDailyRefreshConfig()
        val currentTime = System.currentTimeMillis()
        
        // 如果从未刷新过（lastRefreshTimestamp 为 0），则需要刷新
        if (config.lastRefreshTimestamp == 0L) {
            return true
        }
        
        return when (config.mode) {
            "time" -> {
                // 按时间刷新：检查是否超过设定的小时数
                val hoursSinceRefresh = (currentTime - config.lastRefreshTimestamp) / (1000 * 60 * 60)
                hoursSinceRefresh >= config.refreshHours
            }
            "startup" -> {
                // 按启动次数刷新：检查启动次数是否达到设定值
                config.launchCountSinceRefresh >= config.startupCount
            }
            "smart" -> {
                // 智能刷新：预留接口，后续可根据听歌习惯、时间段等智能判断
                // 目前默认使用每天24小时刷新一次的策略
                val hoursSinceRefresh = (currentTime - config.lastRefreshTimestamp) / (1000 * 60 * 60)
                hoursSinceRefresh >= 24
            }
            else -> false
        }
    }
}