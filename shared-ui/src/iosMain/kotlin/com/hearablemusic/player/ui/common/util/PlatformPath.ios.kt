package com.hearablemusic.player.ui.common.util

import platform.Foundation.NSString
import platform.Foundation.stringByDeletingLastPathComponent

/** iOS actual：NSString.stringByDeletingLastPathComponent（语义同 File.parent）。 */
actual fun fileParentOf(path: String): String? {
    val parent = (path as NSString).stringByDeletingLastPathComponent
    return parent.takeIf { it.isNotEmpty() }
}