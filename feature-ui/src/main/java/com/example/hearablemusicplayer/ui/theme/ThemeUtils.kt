package com.example.hearablemusicplayer.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.hearablemusicplayer.ui.viewmodel.PaletteColors

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
        // 暗色主题 - 只替换primary相关颜色
        darkColorScheme(
            primary = paletteColors.dominantColor,
            primaryContainer = paletteColors.dominantColor.copy(alpha = 0.12f),
            onPrimary = paletteColors.primaryColor,
            onPrimaryContainer = paletteColors.dominantColor.copy(alpha = 0.87f),
            secondary = paletteColors.vibrantColor,
            onSecondary = paletteColors.primaryColor,
        )
    } else {
        // 亮色主题 - 只替换primary相关颜色
        lightColorScheme(
            primary = paletteColors.dominantColor,
            primaryContainer = paletteColors.dominantColor.copy(alpha = 0.12f),
            onPrimary = paletteColors.primaryColor,
            onPrimaryContainer = paletteColors.dominantColor.copy(alpha = 0.87f),
            secondary = paletteColors.vibrantColor,
            onSecondary = paletteColors.primaryColor,
        )
    }
}
