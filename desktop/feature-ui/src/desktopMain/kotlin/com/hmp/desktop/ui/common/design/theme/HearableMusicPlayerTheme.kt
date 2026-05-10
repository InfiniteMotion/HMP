package com.hmp.desktop.ui.common.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.hmp.desktop.ui.common.design.typography.TypographyTokens

@Composable
fun HearableMusicPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = ThemeExtensionManager.getColorScheme(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TypographyTokens.Typography,
        content = content
    )
}
