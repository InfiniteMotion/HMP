package com.hearablemusic.player.ui.platform

import androidx.compose.runtime.Composable

/** Desktop actual：无系统状态栏概念，返回 null（调用方跳过沉浸式逻辑即可）。 */
@Composable
actual fun rememberStatusBarsController(): StatusBarsController? = null
