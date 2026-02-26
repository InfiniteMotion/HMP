package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.TitleWidget
import com.example.hearablemusicplayer.ui.pages.base.SubScreen
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel

@Composable
fun CustomScreen(
    settingsViewModel: SettingsViewModel,
    navController: NavController
) {
    val customMode by settingsViewModel.customMode.collectAsState("default")
    val backgroundStyle by settingsViewModel.backgroundStyle.collectAsState("FLUID")
    
    CustomScreenContent(
        customMode = customMode,
        backgroundStyle = backgroundStyle,
        onBackClick = { navController.popBackStack() },
        setCustomMode = settingsViewModel::saveCustomMode,
        setBackgroundStyle = settingsViewModel::saveBackgroundStyle
    )
}

@Composable
fun CustomScreenContent(
    customMode: String,
    backgroundStyle: String,
    onBackClick: () -> Unit,
    setCustomMode: (String) -> Unit,
    setBackgroundStyle: (String) -> Unit
) {
    SubScreen(
        onBackClick = onBackClick,
        title = stringResource(R.string.theme_customization)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SetThemeMode(
                customMode = customMode,
                setCustomMode = setCustomMode
            )

            SetBackgroundStyle(
                backgroundStyle = backgroundStyle,
                setBackgroundStyle = setBackgroundStyle
            )

            Spacer(modifier = Modifier.height(64.dp))
        }
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
