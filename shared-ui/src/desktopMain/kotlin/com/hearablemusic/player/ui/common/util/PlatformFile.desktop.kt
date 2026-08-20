package com.hearablemusic.player.ui.common.util

import java.io.File

/** Desktop actual：java.io.File.canWrite()。 */
actual fun isFilePathWritable(path: String): Boolean = File(path).canWrite()
