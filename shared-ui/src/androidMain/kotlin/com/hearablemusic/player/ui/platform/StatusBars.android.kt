package com.hearablemusic.player.ui.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * StatusBarsController 的 Android 实现。
 *
 * show/hide 状态栏；hide 同时设置 BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
 * （隐藏期间滑动可短暂唤出）。
 */
@Composable
actual fun rememberStatusBarsController(): StatusBarsController? {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window ?: return null
    val insetsController = remember(window, view) {
        WindowCompat.getInsetsController(window, view)
    }
    return remember(insetsController) {
        object : StatusBarsController {
            override fun show() {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }

            override fun hide() {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}
