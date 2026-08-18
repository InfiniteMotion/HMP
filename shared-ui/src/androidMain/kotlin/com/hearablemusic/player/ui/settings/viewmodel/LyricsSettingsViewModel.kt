package com.hearablemusic.player.ui.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.config.LyricsConfig
import com.hmp.domain.setting.usecase.LyricsSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 歌词设置相关状态（LyricsScreen 使用），从 SettingsViewModel 中拆出。
 */
class LyricsSettingsViewModel(
    application: Application,
    private val lyricsSettingsUseCase: LyricsSettingsUseCase
) : AndroidViewModel(application) {

    // 文本大小配置
    val lyricsOriginalTextSize: StateFlow<Int> = lyricsSettingsUseCase.originalTextSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14)

    val lyricsTranslatedTextSize: StateFlow<Int> = lyricsSettingsUseCase.translatedTextSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14)

    val lyricsCurrentTimeTextSize: StateFlow<Int> = lyricsSettingsUseCase.currentTimeTextSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16)

    // 间距配置
    val lyricsLineSpacing: StateFlow<Int> = lyricsSettingsUseCase.lineSpacing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 6)

    // 显示模式配置
    val lyricsDisplayMode: StateFlow<DisplayMode> = lyricsSettingsUseCase.displayMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DisplayMode.DUAL)

    // 对齐配置
    val lyricsAlignment: StateFlow<LyricsAlignment> = lyricsSettingsUseCase.alignment
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LyricsAlignment.CENTER)

    // 逐字（卡拉 OK）显示开关
    val lyricsKaraokeEnabled: StateFlow<Boolean> = lyricsSettingsUseCase.karaokeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun saveLyricsOriginalTextSize(size: Int) {
        viewModelScope.launch { lyricsSettingsUseCase.saveOriginalTextSize(size) }
    }

    fun saveLyricsTranslatedTextSize(size: Int) {
        viewModelScope.launch { lyricsSettingsUseCase.saveTranslatedTextSize(size) }
    }

    fun saveLyricsCurrentTimeTextSize(size: Int) {
        viewModelScope.launch { lyricsSettingsUseCase.saveCurrentTimeTextSize(size) }
    }

    fun saveLyricsLineSpacing(spacing: Int) {
        viewModelScope.launch { lyricsSettingsUseCase.saveLineSpacing(spacing) }
    }

    fun saveLyricsDisplayMode(mode: DisplayMode) {
        viewModelScope.launch { lyricsSettingsUseCase.saveDisplayMode(mode) }
    }

    fun saveLyricsAlignment(alignment: LyricsAlignment) {
        viewModelScope.launch { lyricsSettingsUseCase.saveAlignment(alignment) }
    }

    fun saveLyricsKaraokeEnabled(enabled: Boolean) {
        viewModelScope.launch { lyricsSettingsUseCase.saveKaraokeEnabled(enabled) }
    }

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

    fun saveLyricsConfig(config: LyricsConfig) {
        viewModelScope.launch { lyricsSettingsUseCase.saveLyricsConfig(config) }
    }

    fun resetLyricsConfig() {
        viewModelScope.launch { lyricsSettingsUseCase.resetToDefault() }
    }
}
