package com.hmp.desktop.data.util

import com.hmp.data.util.stringToPinyinSortKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PinyinSortKeyTest {

    @Test
    fun emptyString_returnsEmpty() {
        assertEquals("", stringToPinyinSortKey(""))
    }

    @Test
    fun chineseCharacters_returnsPinyin() {
        assertEquals("zhongguo", stringToPinyinSortKey("中国"))
    }

    @Test
    fun asciiCharacters_returnsLowercase() {
        assertEquals("hello", stringToPinyinSortKey("Hello"))
    }

    @Test
    fun mixedChineseAndAscii_startsPinyin() {
        val key = stringToPinyinSortKey("中A")
        assertTrue(key.startsWith("zhong"))
        assertTrue(key.endsWith("a"))
    }

    @Test
    fun numbers_preserved() {
        assertEquals("123", stringToPinyinSortKey("123"))
    }

    @Test
    fun chineseSurnames_returnsPinyin() {
        assertEquals("zhangsan", stringToPinyinSortKey("张三"))
    }

    @Test
    fun chineseReturnsDifferentFromOriginal() {
        val key = stringToPinyinSortKey("中")
        assertNotEquals("中", key)
        assertEquals("zhong", key)
    }
}
