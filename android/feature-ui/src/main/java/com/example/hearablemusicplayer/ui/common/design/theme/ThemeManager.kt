package com.example.hearablemusicplayer.ui.common.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.example.hearablemusicplayer.ui.common.design.colors.ColorTokens
import com.example.hearablemusicplayer.ui.common.viewmodel.PaletteColors

object ThemeManager {
    
    fun getLightColorScheme(): ColorScheme {
        return lightColorScheme(
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
    }
    
    fun getDarkColorScheme(): ColorScheme {
        return darkColorScheme(
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
    }
    
    fun generateDynamicColorScheme(paletteColors: PaletteColors, isDarkTheme: Boolean): ColorScheme {
        return if (isDarkTheme) {
            darkColorScheme(
                primary = paletteColors.vibrantColor,
                onPrimary = ColorTokens.DarkOnPrimary,
                primaryContainer = paletteColors.darkVibrantColor,
                onPrimaryContainer = ColorTokens.DarkOnPrimaryContainer,
                secondary = paletteColors.accentColor,
                background = paletteColors.darkMutedColor,
                surface = paletteColors.darkMutedColor,
                error = ColorTokens.DarkError,
                surfaceTint = paletteColors.vibrantColor,
            )
        } else {
            lightColorScheme(
                primary = paletteColors.vibrantColor,
                onPrimary = ColorTokens.LightOnPrimary,
                primaryContainer = paletteColors.lightVibrantColor,
                onPrimaryContainer = ColorTokens.LightOnPrimaryContainer,
                secondary = paletteColors.accentColor,
                background = ColorTokens.LightBackground,
                surface = ColorTokens.LightSurface,
                error = ColorTokens.LightError,
                surfaceTint = paletteColors.vibrantColor,
            )
        }
    }
    
    fun getPresetColorScheme(isDarkTheme: Boolean): ColorScheme {
        return if (isDarkTheme) {
            getDarkColorScheme()
        } else {
            getLightColorScheme()
        }
    }
}
