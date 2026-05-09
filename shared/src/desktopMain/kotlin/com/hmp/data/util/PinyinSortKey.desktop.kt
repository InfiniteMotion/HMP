package com.hmp.data.util

actual fun stringToPinyinSortKey(input: String): String {
    if (input.isEmpty()) return input
    val sb = StringBuilder(input.length)
    for (ch in input) {
        val pinyin = PinyinLookupTable.getPinyin(ch)
        if (pinyin != null) {
            sb.append(pinyin)
        } else if (ch.code in 0..127) {
            sb.append(ch.lowercaseChar())
        }
    }
    return if (sb.isEmpty()) input.lowercase() else sb.toString()
}
