package com.example.hearablemusicplayer.data.util

import net.sourceforge.pinyin4j.PinyinHelper

/**
 * 将字符串转为用于排序的键：英文字母保留并转大写，中文取每字默认拼音的首字母，数字保留，其它字符用固定占位以便与英文混排。
 * 用于按标题/按歌手排序时的中英混排（如「北京」→ BJ，「Hello」→ HELLO）。
 */
fun stringToPinyinSortKey(s: String): String {
    if (s.isEmpty()) return ""
    return s.map { c -> charToPinyinSortKeyChar(c) }.joinToString("")
}

private const val OTHER_CHAR_PLACEHOLDER = '\uFFFF' // 其它字符排到末尾

private fun charToPinyinSortKeyChar(c: Char): Char {
    return when {
        c in 'a'..'z' -> c.uppercaseChar()
        c in 'A'..'Z' -> c
        c in '0'..'9' -> c
        else -> {
            val pyArray = PinyinHelper.toHanyuPinyinStringArray(c)
            if (!pyArray.isNullOrEmpty()) {
                val firstPy = pyArray[0]
                firstPy.firstOrNull()?.takeIf { it.isLetter() }?.uppercaseChar() ?: OTHER_CHAR_PLACEHOLDER
            } else {
                OTHER_CHAR_PLACEHOLDER
            }
        }
    }
}
