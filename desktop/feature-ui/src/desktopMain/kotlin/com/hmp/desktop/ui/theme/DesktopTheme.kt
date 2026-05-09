package com.hmp.desktop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hmp.desktop.ui.design.ColorTokens

object ThemeState {
    var isDark by mutableStateOf(false)
}

private val LightColorScheme = lightColorScheme(
    primary = ColorTokens.LightPrimary,
    onPrimary = ColorTokens.LightOnPrimary,
    primaryContainer = ColorTokens.LightPrimaryContainer,
    onPrimaryContainer = ColorTokens.LightOnPrimaryContainer,
    secondary = ColorTokens.LightSecondary,
    background = ColorTokens.LightBackground,
    surface = ColorTokens.LightSurface,
    error = ColorTokens.LightError,
    surfaceTint = ColorTokens.LightPrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = ColorTokens.DarkPrimary,
    onPrimary = ColorTokens.DarkOnPrimary,
    primaryContainer = ColorTokens.DarkPrimaryContainer,
    onPrimaryContainer = ColorTokens.DarkOnPrimaryContainer,
    secondary = ColorTokens.DarkSecondary,
    background = ColorTokens.DarkBackground,
    surface = ColorTokens.DarkSurface,
    error = ColorTokens.DarkError,
    surfaceTint = ColorTokens.DarkPrimary,
)

@Composable
fun DesktopTheme(
    darkTheme: Boolean = ThemeState.isDark,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
