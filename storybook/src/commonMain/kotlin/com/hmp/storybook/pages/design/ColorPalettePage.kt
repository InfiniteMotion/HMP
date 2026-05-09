package com.hmp.storybook.pages.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmp.storybook.i18n.Strings
import com.hmp.storybook.layout.ColorSwatch
import com.hmp.storybook.layout.StorybookPage
import com.hmp.storybook.theme.AppLanguage
import com.hmp.storybook.theme.HDBlue
import com.hmp.storybook.theme.HDRed
import com.hmp.storybook.theme.LocalAppLanguage

data class ColorItem(val name: String, val color: Color, val hex: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPalettePage(onBack: () -> Unit) {
    val lang = LocalAppLanguage.current

    StorybookPage(
        title = Strings.colorPalette(lang),
        description = Strings.colorPaletteDescription(lang),
        onBack = onBack,
    ) {
        // 品牌色
        Text(
            text = Strings.brandColors(lang),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
        ) {
            ColorSwatch(
                name = "HDBlue (Klein Blue)",
                color = HDBlue,
                hex = "#002FA7",
                modifier = Modifier.weight(1f, fill = false).padding(bottom = 8.dp),
            )
            ColorSwatch(
                name = "HDRed (HD Red)",
                color = HDRed,
                hex = "#C92C2C",
                modifier = Modifier.weight(1f, fill = false).padding(bottom = 8.dp),
            )
        }

        // 语义色 - 浅色主题
        Text(
            text = "${Strings.semanticColors(lang)} - ${Strings.lightTheme(lang)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        val lightColors = listOf(
            ColorItem("Primary", MaterialTheme.colorScheme.primary, "#C92C2C"),
            ColorItem("On Primary", MaterialTheme.colorScheme.onPrimary, "#FFFFFF"),
            ColorItem("Primary Container", MaterialTheme.colorScheme.primaryContainer, "#1976D2"),
            ColorItem("On Primary Container", MaterialTheme.colorScheme.onPrimaryContainer, "#B00020"),
            ColorItem("Secondary", MaterialTheme.colorScheme.secondary, "#002FA7"),
            ColorItem("Error", MaterialTheme.colorScheme.error, "#1976D2"),
            ColorItem("Background", MaterialTheme.colorScheme.background, "#FFFFFF"),
            ColorItem("Surface", MaterialTheme.colorScheme.surface, "#FFFFFF"),
            ColorItem("On Surface", MaterialTheme.colorScheme.onSurface, "#1A1B1F"),
            ColorItem("Surface Variant", MaterialTheme.colorScheme.surfaceVariant, "#E1E2EC"),
            ColorItem("Outline", MaterialTheme.colorScheme.outline, "#757780"),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
        ) {
            lightColors.forEach { item ->
                ColorSwatch(
                    name = item.name,
                    color = item.color,
                    hex = item.hex,
                    modifier = Modifier.weight(1f, fill = false).padding(bottom = 8.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 动态主题说明
        Text(
            text = if (lang == AppLanguage.ZH) {
                "动态主题取色（Palette）从图片中提取 8 种颜色：dominant、vibrant、darkVibrant、lightVibrant、muted、darkMuted、lightMuted、accent，用于生成与内容匹配的动态配色方案。切换到深色主题可查看深色色板。颜色系统基于 Material Design 3，支持浅色/深色自动切换。"
            } else {
                "Dynamic theme palette extracts 8 colors from images: dominant, vibrant, darkVibrant, lightVibrant, muted, darkMuted, lightMuted, accent, used to generate content-matched dynamic color schemes. Switch to dark theme to see the dark palette. The color system is based on Material Design 3, supporting light/dark auto-switching."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
