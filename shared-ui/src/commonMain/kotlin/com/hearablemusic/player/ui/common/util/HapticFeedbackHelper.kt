package com.hearablemusic.player.ui.common.util

import androidx.compose.runtime.Composable

/**
 * 触觉反馈助手（commonMain 契约）。
 *
 * 与 rememberPlatformHaptics() 同模式（见 PlatformHaptics.kt 说明）：
 * 触觉依赖窗口 View（平台 UI 基础设施），用 expect/actual composable 隔离；
 * Android actual 包装 LocalView.performHapticFeedback，Desktop（第 5 步）actual 给空实现。
 * 迁移中的 commonMain 组件与 androidMain 旧页面共用同一入口。
 */
interface HapticFeedbackHelper {
    /** 轻触反馈 - 一般点击操作 */
    fun performLightClick()

    /** 标准点击反馈 - 按钮点击 */
    fun performClick()

    /** 长按反馈 */
    fun performLongPress()

    /** 上下文点击反馈 - 菜单项选择 */
    fun performContextClick()

    /** 键盘按键反馈 */
    fun performKeyboardPress()

    /** 确认反馈 */
    fun performConfirm()

    /** 拒绝反馈 */
    fun performReject()

    /** 拖动开始反馈 */
    fun performDragStart()

    /** 手势开始反馈 */
    fun performGestureStart()

    /** 手势结束反馈 */
    fun performGestureEnd()
}

/** Compose 中获取触觉反馈助手（平台 actual 提供）。 */
@Composable
expect fun rememberHapticFeedback(): HapticFeedbackHelper
