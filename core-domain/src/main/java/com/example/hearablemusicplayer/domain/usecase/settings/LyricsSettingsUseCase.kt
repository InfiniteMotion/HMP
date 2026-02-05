package com.example.hearablemusicplayer.domain.usecase.settings

import com.example.hearablemusicplayer.domain.model.DisplayMode
import com.example.hearablemusicplayer.domain.model.LyricsConfig
import com.example.hearablemusicplayer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 歌词配置管理UseCase
 * 负责歌词显示参数的读取和保存
 */
class LyricsSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    
    // ==================== 文本大小配置 ====================
    
    val originalTextSize: Flow<Int> = settingsRepository.lyricsOriginalTextSize
    
    val translatedTextSize: Flow<Int> = settingsRepository.lyricsTranslatedTextSize
    
    val currentTimeTextSize: Flow<Int> = settingsRepository.lyricsCurrentTimeTextSize
    
    suspend fun saveOriginalTextSize(size: Int) {
        settingsRepository.saveLyricsOriginalTextSize(size)
    }
    
    suspend fun saveTranslatedTextSize(size: Int) {
        settingsRepository.saveLyricsTranslatedTextSize(size)
    }
    
    suspend fun saveCurrentTimeTextSize(size: Int) {
        settingsRepository.saveLyricsCurrentTimeTextSize(size)
    }
    
    // ==================== 间距配置 ====================
    
    val lineSpacing: Flow<Int> = settingsRepository.lyricsLineSpacing
    
    suspend fun saveLineSpacing(spacing: Int) {
        settingsRepository.saveLyricsLineSpacing(spacing)
    }
    
    // ==================== 显示模式配置 ====================
    
    val displayMode: Flow<DisplayMode> = settingsRepository.lyricsDisplayMode
    
    suspend fun saveDisplayMode(mode: DisplayMode) {
        settingsRepository.saveLyricsDisplayMode(mode)
    }
    
    // ==================== 综合配置操作 ====================
    
    /**
     * 获取完整的歌词配置
     */
    suspend fun getLyricsConfig(): LyricsConfig {
        return LyricsConfig(
            originalTextSize = settingsRepository.getLyricsOriginalTextSize(),
            translatedTextSize = settingsRepository.getLyricsTranslatedTextSize(),
            currentTimeTextSize = settingsRepository.getLyricsCurrentTimeTextSize(),
            lineSpacing = settingsRepository.getLyricsLineSpacing(),
            displayMode = settingsRepository.getLyricsDisplayMode()
        )
    }
    
    /**
     * 保存完整的歌词配置
     */
    suspend fun saveLyricsConfig(config: LyricsConfig) {
        saveOriginalTextSize(config.originalTextSize)
        saveTranslatedTextSize(config.translatedTextSize)
        saveCurrentTimeTextSize(config.currentTimeTextSize)
        saveLineSpacing(config.lineSpacing)
        saveDisplayMode(config.displayMode)
    }
    
    /**
     * 重置为默认配置
     */
    suspend fun resetToDefault() {
        val defaultConfig = LyricsConfig()
        saveLyricsConfig(defaultConfig)
    }
}