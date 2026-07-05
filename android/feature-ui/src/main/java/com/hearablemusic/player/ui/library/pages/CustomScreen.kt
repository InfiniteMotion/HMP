package com.hearablemusic.player.ui.library.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.annotation.StringRes
import androidx.compose.material3.Slider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.components.base.TitleWidget
import com.hearablemusic.player.ui.common.pages.base.SubScreen
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_BLUR_RADIUS
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_MATERIAL_PRESET
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_MODE
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_NOISE_FACTOR
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_TINT_ALPHA
import com.hearablemusic.player.ui.common.util.HAZE_MODE_CUSTOM
import com.hearablemusic.player.ui.common.util.HAZE_MODE_PRESET
import com.hearablemusic.player.ui.common.util.HazeMaterialPreset
import com.hearablemusic.player.ui.common.util.hazeMaterialPresetFromValue
import com.hearablemusic.player.ui.common.util.hazeStyleForIntensity
import com.hearablemusic.player.ui.common.util.hazeTintAlpha
import com.hearablemusic.player.ui.common.util.rememberHapticFeedback
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun CustomScreen(
    settingsViewModel: SettingsViewModel,
    navController: NavBackStack<NavKey>
) {
    val customMode by settingsViewModel.customMode.collectAsState("default")
    val backgroundStyle by settingsViewModel.backgroundStyle.collectAsState("FLUID")
    val hazeMode by settingsViewModel.hazeMode.collectAsState(DEFAULT_HAZE_MODE)
    val hazeMaterialPreset by settingsViewModel.hazeMaterialPreset.collectAsState(DEFAULT_HAZE_MATERIAL_PRESET)
    val hazeBlurRadius by settingsViewModel.hazeBlurRadius.collectAsState(DEFAULT_HAZE_BLUR_RADIUS)
    val hazeNoiseFactor by settingsViewModel.hazeNoiseFactor.collectAsState(DEFAULT_HAZE_NOISE_FACTOR)
    val hazeTintAlpha by settingsViewModel.hazeTintAlpha.collectAsState(DEFAULT_HAZE_TINT_ALPHA)
    
    CustomScreenContent(
        customMode = customMode,
        backgroundStyle = backgroundStyle,
        hazeMode = hazeMode,
        hazeMaterialPreset = hazeMaterialPreset,
        hazeBlurRadius = hazeBlurRadius,
        hazeNoiseFactor = hazeNoiseFactor,
        hazeTintAlpha = hazeTintAlpha,
        onBackClick = { navController.removeLastOrNull() },
        setCustomMode = settingsViewModel::saveCustomMode,
        setBackgroundStyle = settingsViewModel::saveBackgroundStyle,
        setHazeMode = settingsViewModel::saveHazeMode,
        applyHazeMaterialPreset = settingsViewModel::applyHazeMaterialPreset,
        setHazeBlurRadius = settingsViewModel::saveHazeBlurRadius,
        setHazeNoiseFactor = settingsViewModel::saveHazeNoiseFactor,
        setHazeTintAlpha = settingsViewModel::saveHazeTintAlpha
    )
}

@Composable
fun CustomScreenContent(
    customMode: String,
    backgroundStyle: String,
    hazeMode: String,
    hazeMaterialPreset: String,
    hazeBlurRadius: Float,
    hazeNoiseFactor: Float,
    hazeTintAlpha: Float,
    onBackClick: () -> Unit,
    setCustomMode: (String) -> Unit,
    setBackgroundStyle: (String) -> Unit,
    setHazeMode: (String) -> Unit,
    applyHazeMaterialPreset: (String, Float) -> Unit,
    setHazeBlurRadius: (Float) -> Unit,
    setHazeNoiseFactor: (Float) -> Unit,
    setHazeTintAlpha: (Float) -> Unit
) {
    SubScreen(
        onBackClick = onBackClick,
        title = stringResource(R.string.theme_customization)
    ) {
        val isLandscape = LocalWindowSizeInfo.current.isLandscape
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        SetThemeMode(customMode = customMode, setCustomMode = setCustomMode)
                        SetBackgroundStyle(backgroundStyle = backgroundStyle, setBackgroundStyle = setBackgroundStyle)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        SetHazeIntensity(
                            hazeMode = hazeMode, hazeMaterialPreset = hazeMaterialPreset,
                            hazeBlurRadius = hazeBlurRadius, hazeNoiseFactor = hazeNoiseFactor,
                            hazeTintAlpha = hazeTintAlpha, setHazeMode = setHazeMode,
                            applyHazeMaterialPreset = applyHazeMaterialPreset,
                            setHazeBlurRadius = setHazeBlurRadius, setHazeNoiseFactor = setHazeNoiseFactor,
                            setHazeTintAlpha = setHazeTintAlpha
                        )
                    }
                }
            } else {
                SetThemeMode(customMode = customMode, setCustomMode = setCustomMode)
                SetBackgroundStyle(backgroundStyle = backgroundStyle, setBackgroundStyle = setBackgroundStyle)
                SetHazeIntensity(hazeMode = hazeMode, hazeMaterialPreset = hazeMaterialPreset, hazeBlurRadius = hazeBlurRadius, hazeNoiseFactor = hazeNoiseFactor, hazeTintAlpha = hazeTintAlpha, setHazeMode = setHazeMode, applyHazeMaterialPreset = applyHazeMaterialPreset, setHazeBlurRadius = setHazeBlurRadius, setHazeNoiseFactor = setHazeNoiseFactor, setHazeTintAlpha = setHazeTintAlpha)
            }
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun SetHazeIntensity(
    hazeMode: String,
    hazeMaterialPreset: String,
    hazeBlurRadius: Float,
    hazeNoiseFactor: Float,
    hazeTintAlpha: Float,
    setHazeMode: (String) -> Unit,
    applyHazeMaterialPreset: (String, Float) -> Unit,
    setHazeBlurRadius: (Float) -> Unit,
    setHazeNoiseFactor: (Float) -> Unit,
    setHazeTintAlpha: (Float) -> Unit
) {
    val hazeState = rememberHazeState()
    val selectedPreset = hazeMaterialPresetFromValue(hazeMaterialPreset)
    val activeIntensity = selectedPreset.intensity
    val percent = (activeIntensity * 100).toInt()
    val haptic = rememberHapticFeedback()

    TitleWidget(title = stringResource(R.string.haze_intensity_title)) {
        val isLandscape = LocalWindowSizeInfo.current.isLandscape
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.haze_intensity_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HazeSelectionOption(
                title = stringResource(R.string.haze_mode_preset_title),
                description = stringResource(R.string.haze_mode_preset_desc),
                isSelected = hazeMode == HAZE_MODE_PRESET,
                onClick = {
                    applyHazeMaterialPreset(selectedPreset.value, selectedPreset.intensity)
                    haptic.performClick()
                }
            )

            HazeSelectionOption(
                title = stringResource(R.string.haze_mode_custom_title),
                description = stringResource(R.string.haze_mode_custom_desc),
                isSelected = hazeMode == HAZE_MODE_CUSTOM,
                onClick = {
                    setHazeMode(HAZE_MODE_CUSTOM)
                    haptic.performClick()
                }
            )

            if (hazeMode == HAZE_MODE_PRESET) {
                Text(
                    text = stringResource(R.string.haze_preset_list_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HazeMaterialPreset.entries.forEach { preset ->
                        HazeSelectionOption(
                            title = stringResource(preset.labelResId()),
                            description = null,
                            isSelected = preset == selectedPreset,
                            onClick = {
                                applyHazeMaterialPreset(preset.value, preset.intensity)
                                haptic.performClick()
                            }
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.haze_custom_params_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.haze_blur_radius_label, hazeBlurRadius),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = hazeBlurRadius,
                    onValueChange = { value ->
                        setHazeBlurRadius(value)
                    },
                    valueRange = 0f..60f
                )

                val noisePercent = (hazeNoiseFactor * 100).toInt()
                Text(
                    text = stringResource(R.string.haze_noise_factor_label, noisePercent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = hazeNoiseFactor,
                    onValueChange = { value ->
                        setHazeNoiseFactor(value)
                    },
                    valueRange = 0f..1f
                )

                val tintPercent = (hazeTintAlpha * 100).toInt()
                Text(
                    text = stringResource(R.string.haze_tint_alpha_label, tintPercent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = hazeTintAlpha,
                    onValueChange = { value ->
                        setHazeTintAlpha(value)
                    },
                    valueRange = 0f..1f
                )
            }

            if (hazeMode == HAZE_MODE_PRESET) {
                Text(
                    text = stringResource(R.string.haze_intensity_value, percent),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .hazeSource(state = hazeState),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .hazeEffect(
                            state = hazeState,
                            style = hazeStyleForIntensity()
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = hazeTintAlpha())
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.haze_intensity_preview_title),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = stringResource(R.string.haze_intensity_preview_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HazeSelectionOption(
    title: String,
    description: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Transparent
        ),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@StringRes
private fun HazeMaterialPreset.labelResId(): Int {
    return when (this) {
        HazeMaterialPreset.ULTRA_THIN -> R.string.haze_preset_ultra_thin
        HazeMaterialPreset.THIN -> R.string.haze_preset_thin
        HazeMaterialPreset.REGULAR -> R.string.haze_preset_regular
        HazeMaterialPreset.THICK -> R.string.haze_preset_thick
        HazeMaterialPreset.ULTRA_THICK -> R.string.haze_preset_ultra_thick
    }
}

@Composable
fun SetThemeMode(
    customMode: String,
    setCustomMode: (String) -> Unit
){
    TitleWidget(
        title = stringResource(R.string.set_theme_mode),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val haptic = rememberHapticFeedback()
            ThemeModeButton(
                text = stringResource(R.string.theme_light),
                isSelected = customMode == "light",
                onClick = {
                    setCustomMode("light")
                    haptic.performClick()
                }
            )
            ThemeModeButton(
                text = stringResource(R.string.theme_dark),
                isSelected = customMode == "dark",
                onClick = {
                    setCustomMode("dark")
                    haptic.performClick()
                }
            )
            ThemeModeButton(
                text = stringResource(R.string.theme_auto),
                isSelected = customMode == "default",
                onClick = {
                    setCustomMode("default")
                    haptic.performClick()
                }
            )
        }
    }
}

@Composable
fun ThemeModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
){
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Transparent,
        ),
        border = if(isSelected) BorderStroke(2.dp, color = MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .width(96.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SetBackgroundStyle(
    backgroundStyle: String,
    setBackgroundStyle: (String) -> Unit
) {
    TitleWidget(
        title = stringResource(R.string.set_background_style),
    ) {
        val isLandscape = LocalWindowSizeInfo.current.isLandscape
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val haptic = rememberHapticFeedback()
            
            BackgroundStyleOption(
                title = stringResource(R.string.style_fluid),
                description = stringResource(R.string.style_fluid_desc),
                isSelected = backgroundStyle == "FLUID",
                onClick = {
                    setBackgroundStyle("FLUID")
                    haptic.performClick()
                }
            )
            
            BackgroundStyleOption(
                title = stringResource(R.string.style_spots),
                description = stringResource(R.string.style_spots_desc),
                isSelected = backgroundStyle == "SPOTS",
                onClick = {
                    setBackgroundStyle("SPOTS")
                    haptic.performClick()
                }
            )
            
            BackgroundStyleOption(
                title = stringResource(R.string.style_blur),
                description = stringResource(R.string.style_blur_desc),
                isSelected = backgroundStyle == "BLUR",
                onClick = {
                    setBackgroundStyle("BLUR")
                    haptic.performClick()
                }
            )
        }
    }
}

@Composable
fun BackgroundStyleOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Transparent
        ),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null // Handled by Card clickable
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
