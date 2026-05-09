package com.example.hearablemusicplayer.ui.common.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.common.components.SegmentedControl
import com.example.hearablemusicplayer.ui.common.components.SegmentedOption
import com.example.hearablemusicplayer.ui.common.dialogs.base.ScrimDialog
import com.example.hearablemusicplayer.ui.common.util.HazeRenderSettings
import com.example.hearablemusicplayer.ui.common.util.LocalHazeRenderSettings
import com.example.hearablemusicplayer.ui.common.util.ProvideHazeRenderSettings
import com.example.hearablemusicplayer.ui.common.util.hazeStyleForIntensity
import com.example.hearablemusicplayer.ui.common.util.hazeTintAlpha
import com.example.hearablemusicplayer.ui.common.util.rememberHapticFeedback
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    hazeState: HazeState? = null,
    hazeRenderSettings: HazeRenderSettings? = null
) {
    val haptic = rememberHapticFeedback()
    val resolvedHazeRenderSettings = hazeRenderSettings ?: LocalHazeRenderSettings.current
    
    // State
    var selectedOption by remember { mutableStateOf("0") }
    var customMinutesInput by remember { mutableStateOf("") }

    // Segmented control options
    val options = listOf(
        SegmentedOption("0", stringResource(R.string.timer_off)),
        SegmentedOption("15", "15 min"),
        SegmentedOption("30", "30 min"),
        SegmentedOption("60", "60 min")
    )

    ProvideHazeRenderSettings(settings = resolvedHazeRenderSettings) {
        ScrimDialog(onDismissRequest = onDismiss) {
            val dialogShape = RoundedCornerShape(28.dp)
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .clip(dialogShape)
                    .then(
                        if (hazeState != null) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = hazeStyleForIntensity()
                            )
                        } else Modifier
                    ),
                shape = dialogShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (hazeState != null) {
                        MaterialTheme.colorScheme.surface.copy(alpha = hazeTintAlpha())
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.sleep_timer),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Segmented Control
                    SegmentedControl(
                        modifier = Modifier.fillMaxWidth(),
                        options = options,
                        selectedOption = selectedOption,
                        onOptionSelected = { optionId ->
                            haptic.performClick()
                            selectedOption = optionId
                            if (optionId != "custom") {
                                customMinutesInput = ""
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Custom Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = customMinutesInput,
                            onValueChange = { input ->
                                // Allow digits only
                                if (input.all { it.isDigit() }) {
                                    customMinutesInput = input
                                    if (input.isNotEmpty()) {
                                        selectedOption = "custom"
                                    }
                                }
                            },
                            label = { Text(stringResource(R.string.custom_minutes)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                haptic.performClick()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                haptic.performConfirm()
                                val minutes = when {
                                    selectedOption == "custom" -> customMinutesInput.toIntOrNull()
                                        ?: 0

                                    else -> selectedOption.toIntOrNull() ?: 0
                                }
                                onConfirm(minutes)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ok),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

