@file:androidx.annotation.OptIn(UnstableApi::class)
package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.BassBoostSlider
import com.example.hearablemusicplayer.ui.components.CustomEqualizer
import com.example.hearablemusicplayer.ui.components.EqualizerPresetSelector
import com.example.hearablemusicplayer.ui.components.ReverbSettings
import com.example.hearablemusicplayer.ui.components.SurroundSoundToggle
import com.example.hearablemusicplayer.ui.template.components.TitleWidget
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel

@Composable
fun AudioEffectsScreen(
    viewModel: PlayControlViewModel,
    navController: NavController
) {
    val audioEffectSettings by viewModel.audioEffectSettings.collectAsState()
    val equalizerPresets by viewModel.equalizerPresets.collectAsState()
    val equalizerBandCount by viewModel.equalizerBandCount.collectAsState()
    val equalizerBandLevelRange by viewModel.equalizerBandLevelRange.collectAsState()
    val currentEqualizerBandLevels by viewModel.currentEqualizerBandLevels.collectAsState()
    
    // 初始化音效状态
    LaunchedEffect(Unit) {
        viewModel.initializeAudioEffects()
    }
    
    // 使用SubScreen模板
    SubScreen(
        navController = navController,
        title = "音效设置"
    ) {
        TitleWidget(
            title = "预设场景音效"
        ) {
            EqualizerPresetSelector(
                presets = equalizerPresets,
                currentPreset = audioEffectSettings.equalizerPreset,
                onPresetSelected = viewModel::setEqualizerPreset
            )
        }

        TitleWidget(
            title = "音效设置"
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BassBoostSlider(
                    currentLevel = audioEffectSettings.bassBoostLevel,
                    onLevelChanged = viewModel::setBassBoost
                )
                SurroundSoundToggle(
                    isEnabled = audioEffectSettings.isSurroundSoundEnabled,
                    onToggle = viewModel::setSurroundSound
                )
                ReverbSettings(
                    currentPreset = audioEffectSettings.reverbPreset,
                    onPresetChanged = viewModel::setReverb
                )
            }
        }

        TitleWidget(
            title = "自定义均衡器"
        ) {
            CustomEqualizer(
                bandCount = equalizerBandCount,
                bandLevelRange = equalizerBandLevelRange,
                currentBandLevels = currentEqualizerBandLevels,
                onBandLevelChanged = { index, level ->
                    val newLevels = currentEqualizerBandLevels.copyOf()
                    newLevels[index] = level
                    viewModel.setCustomEqualizer(newLevels)
                },
                onResetAll = {
                    // 重置所有频段到0
                    val resetLevels = FloatArray(equalizerBandCount) { 0f }
                    viewModel.setCustomEqualizer(resetLevels)
                }
            )
        }
    }
}
