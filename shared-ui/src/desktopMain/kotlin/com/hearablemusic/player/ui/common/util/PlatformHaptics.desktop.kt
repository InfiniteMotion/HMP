package com.hearablemusic.player.ui.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hearablemusic.player.ui.platform.HapticEffect
import com.hearablemusic.player.ui.platform.HapticService

/** Desktop actual：无触觉硬件，空实现（契约见 commonMain PlatformHaptics.kt）。 */
private object NoopHapticService : HapticService {
    override fun perform(effect: HapticEffect) {}
}

@Composable
actual fun rememberPlatformHaptics(): HapticService = remember { NoopHapticService }
