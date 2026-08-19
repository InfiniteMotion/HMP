package com.hearablemusic.player.ui.common.util

import java.io.File

/** Android actual：沿用 java.io.File.parent 语义（'/' 与 '\' 分隔符均处理）。 */
actual fun fileParentOf(path: String): String? = File(path).parent
