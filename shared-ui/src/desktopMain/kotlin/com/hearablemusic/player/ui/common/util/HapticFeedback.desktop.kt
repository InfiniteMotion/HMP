package com.hearablemusic.player.ui.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** Desktop actual：无触觉硬件，全部空实现（契约见 commonMain HapticFeedbackHelper.kt）。 */
private object DesktopHapticFeedbackHelper : HapticFeedbackHelper {
    override fun performLightClick() {}
    override fun performClick() {}
    override fun performLongPress() {}
    override fun performContextClick() {}
    override fun performKeyboardPress() {}
    override fun performConfirm() {}
    override fun performReject() {}
    override fun performDragStart() {}
    override fun performGestureStart() {}
    override fun performGestureEnd() {}
}

@Composable
actual fun rememberHapticFeedback(): HapticFeedbackHelper = remember { DesktopHapticFeedbackHelper }
