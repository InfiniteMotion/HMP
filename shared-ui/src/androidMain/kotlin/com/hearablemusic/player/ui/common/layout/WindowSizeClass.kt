package com.hearablemusic.player.ui.common.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * 窗口宽度尺寸分类，断点与 Material3 WindowSizeClass 规范一致。
 */
@Immutable
enum class WindowWidthSizeClass {
    /** < 600dp — 手机竖屏 */
    Compact,
    /** 600–840dp — 手机横屏、小平板竖屏 */
    Medium,
    /** >= 840dp — 平板横屏、桌面端 */
    Expanded
}

@Immutable
enum class WindowHeightSizeClass {
    /** < 480dp */
    Compact,
    /** 480–900dp */
    Medium,
    /** >= 900dp */
    Expanded
}

@Immutable
data class AppWindowSizeInfo(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass,
    val widthDp: Float,
    val heightDp: Float,
) {
    val isCompact: Boolean get() = widthSizeClass == WindowWidthSizeClass.Compact
    val isMedium: Boolean get() = widthSizeClass == WindowWidthSizeClass.Medium
    val isExpanded: Boolean get() = widthSizeClass == WindowWidthSizeClass.Expanded

    /** 横屏：宽度大于高度 */
    val isLandscape: Boolean get() = widthDp > heightDp

    /** 仅手机横屏（横屏+紧凑高度）使用融合侧边栏，节省垂直空间 */
    val useFusionSidebar: Boolean get() = isLandscape && heightSizeClass == WindowHeightSizeClass.Compact

    /** 手机横屏布局（播放页等需要特殊处理的场景） */
    val isPhoneLandscape: Boolean get() = useFusionSidebar
}

val LocalWindowSizeInfo = staticCompositionLocalOf<AppWindowSizeInfo> {
    error("AppWindowSizeInfo not provided")
}

@Composable
fun rememberAppWindowSizeInfo(): AppWindowSizeInfo {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val widthDp = with(density) { windowInfo.containerSize.width.toDp() }
    val heightDp = with(density) { windowInfo.containerSize.height.toDp() }
    return remember(widthDp, heightDp) {
        AppWindowSizeInfo(
            widthSizeClass = when {
                widthDp >= 840.dp -> WindowWidthSizeClass.Expanded
                widthDp >= 600.dp -> WindowWidthSizeClass.Medium
                else -> WindowWidthSizeClass.Compact
            },
            heightSizeClass = when {
                heightDp >= 900.dp -> WindowHeightSizeClass.Expanded
                heightDp >= 480.dp -> WindowHeightSizeClass.Medium
                else -> WindowHeightSizeClass.Compact
            },
            widthDp = widthDp.value,
            heightDp = heightDp.value,
        )
    }
}
