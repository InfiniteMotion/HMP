package com.hearablemusic.player.ui.common.util

import java.util.Calendar

/** Android actual：与原 MusicListConfig 行为一致的 Calendar 实现。 */
actual fun epochMillisToYearMonth(epochMillis: Long): Pair<Int, Int> {
    val cal = Calendar.getInstance()
    cal.timeInMillis = epochMillis
    return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
}

actual fun nowEpochMillis(): Long = Calendar.getInstance().timeInMillis
