package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.template.components.TitleWidget
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel

@Composable
fun CustomScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {
    SubScreen(
        navController = navController,
        title = "主题定制"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val customMode by viewModel.customMode.collectAsState("default")

            SetThemeMode(
                customMode = customMode,
                setCustomMode = viewModel::saveCustomMode
            )
        }
    }
}

@Composable
fun SetThemeMode(
    customMode: String,
    setCustomMode: (String) -> Unit
){
    TitleWidget(
        title = "设置主题明暗模式",
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
                text = "明",
                isSelected = customMode == "light",
                onClick = {
                    setCustomMode("light")
                    haptic.performClick()
                }
            )
            ThemeModeButton(
                text = "暗",
                isSelected = customMode == "dark",
                onClick = {
                    setCustomMode("dark")
                    haptic.performClick()
                }
            )
            ThemeModeButton(
                text = "Auto",
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
            containerColor = MaterialTheme.colorScheme.surface,
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

