package com.hmp.desktop.ui.common.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 窗口宽度尺寸分类
 * 参考 Material3 WindowSizeClass 规范，适配桌面端场景
 */
@Immutable
enum class WindowWidthSizeClass {
    /** 紧凑型：< 600dp（手机竖屏） */
    Compact,
    /** 中等型：600dp - 840dp（小平板、手机横屏） */
    Medium,
    /** 扩展型：>= 840dp（平板横屏、桌面端） */
    Expanded
}

/**
 * 窗口高度尺寸分类
 */
@Immutable
enum class WindowHeightSizeClass {
    Compact,   // < 480dp
    Medium,    // 480dp - 900dp
    Expanded   // >= 900dp
}

/**
 * 窗口尺寸信息
 */
@Immutable
data class WindowSizeInfo(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass,
    val widthDp: Dp,
    val heightDp: Dp
) {
    /** 是否为紧凑布局（手机风格） */
    val isCompact: Boolean get() = widthSizeClass == WindowWidthSizeClass.Compact

    /** 是否为扩展布局（桌面风格） */
    val isExpanded: Boolean get() = widthSizeClass == WindowWidthSizeClass.Expanded

    /** 是否应使用侧边导航栏 */
    val useNavigationRail: Boolean get() = widthSizeClass >= WindowWidthSizeClass.Medium

    /** 是否应使用多面板布局 */
    val useMultiPane: Boolean get() = widthSizeClass == WindowWidthSizeClass.Expanded
}

/**
 * 根据宽度 Dp 值确定尺寸分类
 */
fun widthSizeClass(widthDp: Dp): WindowWidthSizeClass {
    return when {
        widthDp < 600.dp -> WindowWidthSizeClass.Compact
        widthDp < 840.dp -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
}

/**
 * 根据高度 Dp 值确定尺寸分类
 */
fun heightSizeClass(heightDp: Dp): WindowHeightSizeClass {
    return when {
        heightDp < 480.dp -> WindowHeightSizeClass.Compact
        heightDp < 900.dp -> WindowHeightSizeClass.Medium
        else -> WindowHeightSizeClass.Expanded
    }
}
