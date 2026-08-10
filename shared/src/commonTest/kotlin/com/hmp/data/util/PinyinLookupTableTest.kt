package com.hmp.data.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PinyinLookupTableTest {

    @Test
    fun getPinyin_commonCharacters_returnsCorrect() {
        assertEquals("zhong", PinyinLookupTable.getPinyin('中'))
        assertEquals("guo", PinyinLookupTable.getPinyin('国'))
        assertEquals("ren", PinyinLookupTable.getPinyin('人'))
        assertEquals("da", PinyinLookupTable.getPinyin('大'))
        assertEquals("xiao", PinyinLookupTable.getPinyin('小'))
    }

    @Test
    fun getPinyin_firstChar_returnsYi() {
        assertEquals("yi", PinyinLookupTable.getPinyin('一'))
    }

    @Test
    fun getPinyin_numbers_returnsNull() {
        assertNull(PinyinLookupTable.getPinyin('0'))
        assertNull(PinyinLookupTable.getPinyin('9'))
    }

    @Test
    fun getPinyin_ascii_returnsNull() {
        assertNull(PinyinLookupTable.getPinyin('A'))
        assertNull(PinyinLookupTable.getPinyin('z'))
    }

    @Test
    fun getPinyin_latin_returnsNull() {
        assertNull(PinyinLookupTable.getPinyin('é'))
        assertNull(PinyinLookupTable.getPinyin('ñ'))
    }

    @Test
    fun getPinyin_commonSurnames_returnsCorrect() {
        assertEquals("zhang", PinyinLookupTable.getPinyin('张'))
        assertEquals("wang", PinyinLookupTable.getPinyin('王'))
        assertEquals("li", PinyinLookupTable.getPinyin('李'))
        assertEquals("zhao", PinyinLookupTable.getPinyin('赵'))
    }

    @Test
    fun getPinyin_musicRelated_returnsNonNull() {
        assertNotNull(PinyinLookupTable.getPinyin('音'))
        assertNotNull(PinyinLookupTable.getPinyin('乐'))
        assertNotNull(PinyinLookupTable.getPinyin('歌'))
        assertNotNull(PinyinLookupTable.getPinyin('曲'))
    }
}
