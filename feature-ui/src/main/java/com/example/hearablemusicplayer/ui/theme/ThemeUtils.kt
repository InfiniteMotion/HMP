package com.example.hearablemusicplayer.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.hearablemusicplayer.ui.viewmodel.PaletteColors

/**
 * 获取预置主题ColorScheme
 * 根据主题模式返回黑白预置主题
 */
@Composable
fun getPresetColorScheme(isDarkTheme: Boolean): ColorScheme {
    return if (isDarkTheme) {
        darkColorScheme(
            primary = DarkPrimary,
            onPrimary = DarkOnPrimary,
            primaryContainer = DarkPrimaryContainer,
            onPrimaryContainer = DarkOnPrimaryContainer,
            secondary = DarkSecondary,
            background = DarkBackground,
            surface = DarkSurface,
            error = DarkError,
            surfaceTint = DarkPrimary,
        )
    } else {
        lightColorScheme(
            primary = LightPrimary,
            onPrimary = LightOnPrimary,
            primaryContainer = LightPrimaryContainer,
            onPrimaryContainer = LightOnPrimaryContainer,
            secondary = LightSecondary,
            background = LightBackground,
            surface = LightSurface,
            error = LightError,
            surfaceTint = LightPrimary,
        )
    }
}

/**
 * 动态主题生成工具类
 * 根据提取的专辑封面颜色生成Material Theme 3的ColorScheme
 * 只替换primary相关颜色，其他颜色保持默认
 */
@Composable
fun generateDynamicColorScheme(
    paletteColors: PaletteColors,
    isDarkTheme: Boolean
): ColorScheme {
    return if (isDarkTheme) {
        // 暗色主题 - 使用亮色系作为强调色
        val primary = paletteColors.lightVibrantColor
        val secondary = paletteColors.vibrantColor

        darkColorScheme(
            primary = primary,
            primaryContainer = paletteColors.darkMutedColor.copy(alpha = 0.5f),
            onPrimary = androidx.compose.ui.graphics.Color.Black,
            onPrimaryContainer = paletteColors.lightVibrantColor,
            secondary = secondary,
            onSecondary = androidx.compose.ui.graphics.Color.Black,
            background = DarkBackground,
            surface = DarkSurface
        )
    } else {
        // 亮色主题 - 使用深色系作为强调色
        val primary = paletteColors.darkVibrantColor
        val secondary = paletteColors.vibrantColor

        lightColorScheme(
            primary = primary,
            primaryContainer = paletteColors.lightMutedColor.copy(alpha = 0.5f),
            onPrimary = androidx.compose.ui.graphics.Color.White,
            onPrimaryContainer = paletteColors.darkVibrantColor,
            secondary = secondary,
            onSecondary = androidx.compose.ui.graphics.Color.White,
            background = LightBackground,
            surface = LightSurface
        )
    }
}
