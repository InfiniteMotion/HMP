package com.hearablemusic.player.ui.common.dialogs.base

import androidx.compose.ui.window.DialogProperties

/** Android actual：edge-to-edge 全屏遮罩（decorFitsSystemWindows 为 Android 变体专属参数）。 */
internal actual fun scrimDialogProperties(): DialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
    decorFitsSystemWindows = false,
)
