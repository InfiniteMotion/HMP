package com.hearablemusic.player.ui.common.viewmodel

import androidx.compose.ui.graphics.Color

/**
 * 封面取色结果（commonMain 共享模型）。
 * Android 端由 ThemeViewModel（Coil + Bitmap 直方图峰值检测）产出；
 * Desktop 端后续按同契约实现。供 ThemeManager 生成动态配色。
 */
data class PaletteColors(
    val peaks: List<Color> = emptyList(),      // 封面颜色峰，按权重降序
    val peakWeights: List<Float> = emptyList(), // 对应峰的权重 (0-1)，可驱动光斑大小/数量
    val primary: Color = Color(0xFF1E90FF),
    val background: Color = Color(0xFF121212),
    val accent: Color = Color(0xFF444444)
)
