package com.hearablemusic.player.ui.common.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Desktop actual：JVM Calendar/SimpleDateFormat（与 Android actual 同实现，desktop target 即 JVM）。 */
actual fun epochMillisToYearMonth(epochMillis: Long): Pair<Int, Int> {
    val cal = Calendar.getInstance()
    cal.timeInMillis = epochMillis
    return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
}

actual fun nowEpochMillis(): Long = Calendar.getInstance().timeInMillis

actual fun formatEpochMillis(epochMillis: Long, pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
