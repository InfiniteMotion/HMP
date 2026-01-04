package com.example.hearablemusicplayer.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * 触觉反馈工具类
 * 提供不同强度的触觉反馈效果
 */
class HapticFeedbackHelper(private val view: View) {
    
    /**
     * 轻触反馈 - 用于一般点击操作
     */
    fun performLightClick() {
        view.performHapticFeedback(
            HapticFeedbackConstants.CLOCK_TICK,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
    
    /**
     * 标准点击反馈 - 用于按钮点击
     */
    fun performClick() {
        view.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
    
    /**
     * 长按反馈 - 用于长按操作
     */
    fun performLongPress() {
        view.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
    
    /**
     * 上下文点击反馈 - 用于菜单项选择
     */
    fun performContextClick() {
        view.performHapticFeedback(
            HapticFeedbackConstants.CONTEXT_CLICK,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
    
    /**
     * 键盘按键反馈 - 用于输入操作
     */
    fun performKeyboardPress() {
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
    
    /**
     * 确认反馈 - 用于确认操作（API 30+）
     */
    fun performConfirm() {
        view.performHapticFeedback(
            HapticFeedbackConstants.CONFIRM,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
    
    /**
     * 拒绝反馈 - 用于拒绝或取消操作（API 30+）
     */
    fun performReject() {
        view.performHapticFeedback(
            HapticFeedbackConstants.REJECT,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
    
    /**
     * 拖动开始反馈 - 用于开始拖动操作（API 34+）
     */
    fun performDragStart() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(
                HapticFeedbackConstants.DRAG_START,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        } else {
            performLightClick()
        }
    }
    
    /**
     * 手势开始反馈 - 用于手势开始（API 30+）
     */
    fun performGestureStart() {
        view.performHapticFeedback(
            HapticFeedbackConstants.GESTURE_START,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
    
    /**
     * 手势结束反馈 - 用于手势结束（API 30+）
     */
    fun performGestureEnd() {
        view.performHapticFeedback(
            HapticFeedbackConstants.GESTURE_END,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
}

/**
 * Compose中获取触觉反馈助手的扩展函数
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackHelper {
    val view = LocalView.current
    return remember(view) { HapticFeedbackHelper(view) }
}
