package com.hearablemusic.player.ui.platform

import androidx.compose.runtime.Composable

/**
 * 平台状态栏控制器。
 *
 * 歌词页沉浸式交互（控件隐藏时同时隐藏状态栏，交互/退出时恢复）
 * 依赖窗口系统，属平台 UI 基础设施，故用 expect/actual composable
 * （与 PlatformHaptics 同模式）：Android actual 包装 WindowInsetsController，
 * Desktop actual 返回 null（无系统状态栏概念）。
 */
interface StatusBarsController {
    /** 显示状态栏。 */
    fun show()

    /** 隐藏状态栏（滑动可短暂唤出）。 */
    fun hide()
}

/** 取当前平台状态栏控制器；平台无窗口概念时返回 null（调用方跳过即可）。 */
@Composable
expect fun rememberStatusBarsController(): StatusBarsController?
