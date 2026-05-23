package com.example.hearablemusicplayer.ui.common.design.dimens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearablemusicplayer.ui.common.layout.LocalWindowSizeInfo
import com.example.hearablemusicplayer.ui.common.layout.WindowWidthSizeClass

// ── Token 定义 ──────────────────────────────────────────────

@Immutable
data class SpacingSizes(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
)

@Immutable
data class CornerSizes(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
)

@Immutable
data class TypeSizes(
    val xs: TextUnit,
    val sm: TextUnit,
    val md: TextUnit,
    val lg: TextUnit,
    val xl: TextUnit,
)

@Immutable
data class IconSizes(
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
)

@Immutable
data class ComponentSizes(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp,
)

@Immutable
data class HMPDimens(
    val spacing: SpacingSizes,
    val corner: CornerSizes,
    val type: TypeSizes,
    val icon: IconSizes,
    val component: ComponentSizes,
)

// ── 三档具体值 ──────────────────────────────────────────────

internal fun dimensFor(sizeClass: WindowWidthSizeClass) = when (sizeClass) {
    // Compact: 基准 = 当前应用尺寸
    WindowWidthSizeClass.Compact -> HMPDimens(
        spacing = SpacingSizes(xs = 4.dp, sm = 8.dp, md = 16.dp, lg = 24.dp, xl = 32.dp),
        corner = CornerSizes(xs = 4.dp, sm = 12.dp, md = 20.dp, lg = 28.dp),
        type = TypeSizes(xs = 10.sp, sm = 12.sp, md = 16.sp, lg = 22.sp, xl = 32.sp),
        icon = IconSizes(sm = 16.dp, md = 24.dp, lg = 28.dp),
        component = ComponentSizes(xs = 48.dp, sm = 100.dp, md = 160.dp, lg = 220.dp, xl = 280.dp, xxl = 360.dp),
    )
    // Medium: ~1.25x Compact
    WindowWidthSizeClass.Medium -> HMPDimens(
        spacing = SpacingSizes(xs = 5.dp, sm = 10.dp, md = 20.dp, lg = 30.dp, xl = 40.dp),
        corner = CornerSizes(xs = 5.dp, sm = 14.dp, md = 24.dp, lg = 32.dp),
        type = TypeSizes(xs = 11.sp, sm = 14.sp, md = 18.sp, lg = 24.sp, xl = 35.sp),
        icon = IconSizes(sm = 18.dp, md = 28.dp, lg = 32.dp),
        component = ComponentSizes(xs = 56.dp, sm = 116.dp, md = 184.dp, lg = 252.dp, xl = 320.dp, xxl = 416.dp),
    )
    // Expanded: ~1.5x Compact
    WindowWidthSizeClass.Expanded -> HMPDimens(
        spacing = SpacingSizes(xs = 6.dp, sm = 12.dp, md = 24.dp, lg = 36.dp, xl = 48.dp),
        corner = CornerSizes(xs = 6.dp, sm = 16.dp, md = 28.dp, lg = 36.dp),
        type = TypeSizes(xs = 12.sp, sm = 16.sp, md = 20.sp, lg = 28.sp, xl = 38.sp),
        icon = IconSizes(sm = 20.dp, md = 32.dp, lg = 36.dp),
        component = ComponentSizes(xs = 64.dp, sm = 132.dp, md = 208.dp, lg = 284.dp, xl = 360.dp, xxl = 472.dp),
    )
}

// ── CompositionLocal ────────────────────────────────────────

val LocalHMPDimens = staticCompositionLocalOf<HMPDimens> {
    error("HMPDimens not provided")
}

@Composable
fun rememberHMPDimens(): HMPDimens {
    val info = LocalWindowSizeInfo.current
    // 手机横屏强制 Compact，无视实际 widthSizeClass
    val sizeClass = if (info.isPhoneLandscape) WindowWidthSizeClass.Compact else info.widthSizeClass
    return dimensFor(sizeClass)
}
