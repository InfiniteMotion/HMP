package com.hmp.desktop.ui.settings.viewmodel

import com.hmp.domain.setting.model.AudioEffectSettings
import com.hmp.domain.setting.usecase.UserSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AudioEffectViewModel(
    private val settingsUseCase: UserSettingsUseCase
) {
    private val _audioEffectSettings = MutableStateFlow(AudioEffectSettings())
    val audioEffectSettings: StateFlow<AudioEffectSettings> = _audioEffectSettings

    private val _equalizerPresets = MutableStateFlow<List<String>>(emptyList())
    val equalizerPresets: StateFlow<List<String>> = _equalizerPresets

    private val _equalizerBandCount = MutableStateFlow(0)
    val equalizerBandCount: StateFlow<Int> = _equalizerBandCount

    private val _equalizerBandLevelRange = MutableStateFlow(Pair(0, 0))
    val equalizerBandLevelRange: StateFlow<Pair<Int, Int>> = _equalizerBandLevelRange

    private val _currentEqualizerBandLevels = MutableStateFlow(FloatArray(0))
    val currentEqualizerBandLevels: StateFlow<FloatArray> = _currentEqualizerBandLevels

    fun initializeAudioEffects() {}
    fun setEqualizerPreset(preset: Int) {}
    fun setBassBoost(level: Int) {}
    fun setSurroundSound(enabled: Boolean) {}
    fun setReverb(preset: Int) {}
    fun setCustomEqualizer(bandLevels: FloatArray) {}
    fun getCurrentEqualizerPreset(): Int = 0
    fun getBassBoostLevel(): Int = 0
    fun isSurroundSoundEnabled(): Boolean = false
    fun getReverbPreset(): Int = 0
    fun getCurrentEqualizerBandLevels(): FloatArray = FloatArray(0)
}
