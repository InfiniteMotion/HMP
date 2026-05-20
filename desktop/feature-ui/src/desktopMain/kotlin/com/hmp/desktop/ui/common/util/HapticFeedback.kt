package com.hmp.desktop.ui.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class HapticFeedbackHelper {
    fun performLightClick() {}
    fun performClick() {}
    fun performLongPress() {}
    fun performContextClick() {}
    fun performKeyboardPress() {}
    fun performConfirm() {}
    fun performReject() {}
    fun performDragStart() {}
    fun performGestureStart() {}
    fun performGestureEnd() {}
}

@Composable
fun rememberHapticFeedback(): HapticFeedbackHelper {
    return remember { HapticFeedbackHelper() }
}
