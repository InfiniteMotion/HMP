package com.example.hearablemusicplayer.domain.usecase.settings

import com.example.hearablemusicplayer.data.repository.SettingsRepository
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
    
    // DeepSeek API Key
    suspend fun saveDeepSeekApiKey(apiKey: String) {
        settingsRepository.saveDeepSeekApiKey(apiKey)
    }
    
    suspend fun getDeepSeekApiKey(): String {
        return settingsRepository.getDeepSeekApiKey()
    }
}
