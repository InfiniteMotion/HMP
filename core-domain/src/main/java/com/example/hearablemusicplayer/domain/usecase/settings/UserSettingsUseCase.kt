package com.example.hearablemusicplayer.domain.usecase.settings

import com.example.hearablemusicplayer.data.repository.SettingsRepository
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
    
}
