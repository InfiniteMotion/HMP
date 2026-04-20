package com.example.hearablemusicplayer.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import com.hmp.domain.setting.model.AudioEffectSettings
import com.example.hearablemusicplayer.player.controller.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
@UnstableApi
class AudioEffectViewModel @Inject constructor(
    private val musicController: MusicController
) : ViewModel() {

    val audioEffectSettings: StateFlow<AudioEffectSettings> = musicController.audioEffectSettings
    val equalizerPresets: StateFlow<List<String>> = musicController.equalizerPresets
    val equalizerBandCount: StateFlow<Int> = musicController.equalizerBandCount
    val equalizerBandLevelRange: StateFlow<Pair<Int, Int>> = musicController.equalizerBandLevelRange
    val currentEqualizerBandLevels: StateFlow<FloatArray> = musicController.currentEqualizerBandLevels

    fun initializeAudioEffects() = musicController.initializeAudioEffects()
    fun setEqualizerPreset(preset: Int) = musicController.setEqualizerPreset(preset)
    fun setBassBoost(level: Int) = musicController.setBassBoost(level)
    fun setSurroundSound(enabled: Boolean) = musicController.setSurroundSound(enabled)
    fun setReverb(preset: Int) = musicController.setReverb(preset)
    fun setCustomEqualizer(bandLevels: FloatArray) = musicController.setCustomEqualizer(bandLevels)
    fun getCurrentEqualizerPreset() = musicController.getCurrentEqualizerPreset()
    fun getBassBoostLevel() = musicController.getBassBoostLevel()
    fun isSurroundSoundEnabled() = musicController.isSurroundSoundEnabled()
    fun getReverbPreset() = musicController.getReverbPreset()
    fun getCurrentEqualizerBandLevels() = musicController.getCurrentEqualizerBandLevels()
}