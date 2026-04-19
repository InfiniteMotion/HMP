package com.example.hearablemusicplayer.ui.common.design.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.hearablemusicplayer.ui.common.design.animation.AnimationTokens
import com.example.hearablemusicplayer.ui.common.design.colors.ColorTokens
import com.example.hearablemusicplayer.ui.common.design.typography.TypographyTokens

@Immutable
class DesignSystem(
    val colors: ColorTokens,
    val typography: TypographyTokens,
    val animation: AnimationTokens
)

val LocalDesignSystem = staticCompositionLocalOf {
    DesignSystem(
        colors = ColorTokens,
        typography = TypographyTokens,
        animation = AnimationTokens
    )
}

@Composable
fun designSystem() = LocalDesignSystem.current
