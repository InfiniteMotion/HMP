package com.hmp.domain.setting.usecase

import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.config.LyricsConfig
import com.hmp.domain.lyrics.LyricsComponent
import com.hmp.domain.lyrics.LyricsComponentConfig
import com.hmp.domain.lyrics.LyricsConfigResolver
import com.hmp.domain.setting.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class LyricsSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ==================== Per-component config ====================

    fun getComponentConfig(component: LyricsComponent): Flow<LyricsComponentConfig> {
        val flow = when (component) {
            LyricsComponent.PLAYER -> settingsRepository.lyricsPlayerConfig
            LyricsComponent.FULLSCREEN -> settingsRepository.lyricsFullscreenConfig
            LyricsComponent.FLOATING -> settingsRepository.lyricsFloatingConfig
        }
        return flow.map { parseConfig(it) }
    }

    suspend fun saveComponentConfig(component: LyricsComponent, config: LyricsComponentConfig) {
        val jsonStr = json.encodeToString(LyricsComponentConfig.serializer(), config)
        when (component) {
            LyricsComponent.PLAYER -> settingsRepository.saveLyricsPlayerConfig(jsonStr)
            LyricsComponent.FULLSCREEN -> settingsRepository.saveLyricsFullscreenConfig(jsonStr)
            LyricsComponent.FLOATING -> settingsRepository.saveLyricsFloatingConfig(jsonStr)
        }
    }

    /**
     * 获取所有组件配置的 Map（用于解析 linkedTo 链）
     */
    suspend fun getAllComponentConfigs(): Map<LyricsComponent, LyricsComponentConfig> {
        return LyricsComponent.entries.associateWith { component ->
            val jsonStr = when (component) {
                LyricsComponent.PLAYER -> settingsRepository.getLyricsPlayerConfig()
                LyricsComponent.FULLSCREEN -> settingsRepository.getLyricsFullscreenConfig()
                LyricsComponent.FLOATING -> settingsRepository.getLyricsFloatingConfig()
            }
            parseConfig(jsonStr)
        }
    }

    /**
     * 解析 linkedTo 链，返回最终生效的 LyricsConfig
     */
    suspend fun resolveConfig(component: LyricsComponent): LyricsConfig {
        val allConfigs = getAllComponentConfigs()
        return LyricsConfigResolver.resolve(component, allConfigs)
    }

    /**
     * 重置指定组件为默认配置
     */
    suspend fun resetComponentToDefault(component: LyricsComponent) {
        saveComponentConfig(component, LyricsComponentConfig.DEFAULT)
    }

    private fun parseConfig(jsonStr: String): LyricsComponentConfig {
        return try {
            json.decodeFromString(LyricsComponentConfig.serializer(), jsonStr)
        } catch (e: Exception) {
            LyricsComponentConfig.DEFAULT
        }
    }

    // Floating lyrics enabled toggle
    val floatingLyricsEnabled = settingsRepository.floatingLyricsEnabled

    suspend fun saveFloatingLyricsEnabled(enabled: Boolean) {
        settingsRepository.saveFloatingLyricsEnabled(enabled)
    }

    // ==================== Legacy (deprecated) ====================

    @Deprecated("Use getComponentConfig(LyricsComponent.PLAYER) instead")
    val originalTextSize: Flow<Int> = settingsRepository.lyricsOriginalTextSize

    @Deprecated("Use getComponentConfig(LyricsComponent.PLAYER) instead")
    val translatedTextSize: Flow<Int> = settingsRepository.lyricsTranslatedTextSize

    @Deprecated("Use getComponentConfig(LyricsComponent.PLAYER) instead")
    val currentTimeTextSize: Flow<Int> = settingsRepository.lyricsCurrentTimeTextSize

    @Deprecated("Use getComponentConfig(LyricsComponent.PLAYER) instead")
    val lineSpacing: Flow<Int> = settingsRepository.lyricsLineSpacing

    @Deprecated("Use getComponentConfig(LyricsComponent.PLAYER) instead")
    val displayMode: Flow<DisplayMode> = settingsRepository.lyricsDisplayMode

    @Deprecated("Use getComponentConfig(LyricsComponent.PLAYER) instead")
    val alignment: Flow<LyricsAlignment> = settingsRepository.lyricsAlignment

    @Deprecated("Use saveComponentConfig() instead")
    suspend fun saveOriginalTextSize(size: Int) { settingsRepository.saveLyricsOriginalTextSize(size) }

    @Deprecated("Use saveComponentConfig() instead")
    suspend fun saveTranslatedTextSize(size: Int) { settingsRepository.saveLyricsTranslatedTextSize(size) }

    @Deprecated("Use saveComponentConfig() instead")
    suspend fun saveCurrentTimeTextSize(size: Int) { settingsRepository.saveLyricsCurrentTimeTextSize(size) }

    @Deprecated("Use saveComponentConfig() instead")
    suspend fun saveLineSpacing(spacing: Int) { settingsRepository.saveLyricsLineSpacing(spacing) }

    @Deprecated("Use saveComponentConfig() instead")
    suspend fun saveDisplayMode(mode: DisplayMode) { settingsRepository.saveLyricsDisplayMode(mode) }

    @Deprecated("Use saveComponentConfig() instead")
    suspend fun saveAlignment(alignment: LyricsAlignment) { settingsRepository.saveLyricsAlignment(alignment) }

    @Deprecated("Use resolveConfig(LyricsComponent.PLAYER) instead")
    suspend fun getLyricsConfig(): LyricsConfig {
        return LyricsConfig(
            originalTextSize = settingsRepository.getLyricsOriginalTextSize(),
            translatedTextSize = settingsRepository.getLyricsTranslatedTextSize(),
            currentTimeTextSize = settingsRepository.getLyricsCurrentTimeTextSize(),
            lineSpacing = settingsRepository.getLyricsLineSpacing(),
            displayMode = settingsRepository.getLyricsDisplayMode(),
            alignment = settingsRepository.getLyricsAlignment()
        )
    }

    @Deprecated("Use saveComponentConfig() instead")
    suspend fun saveLyricsConfig(config: LyricsConfig) {
        saveOriginalTextSize(config.originalTextSize)
        saveTranslatedTextSize(config.translatedTextSize)
        saveCurrentTimeTextSize(config.currentTimeTextSize)
        saveLineSpacing(config.lineSpacing)
        saveDisplayMode(config.displayMode)
        saveAlignment(config.alignment)
    }

    @Deprecated("Use resetComponentToDefault() instead")
    suspend fun resetToDefault() {
        val defaultConfig = LyricsConfig()
        saveLyricsConfig(defaultConfig)
    }
}
