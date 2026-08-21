package com.hearablemusic.player.ui.common.util

import java.io.File

/** Desktop actual：java.io.File.parent（desktop target 即 JVM，语义与 Android actual 一致）。 */
actual fun fileParentOf(path: String): String? = File(path).parent
