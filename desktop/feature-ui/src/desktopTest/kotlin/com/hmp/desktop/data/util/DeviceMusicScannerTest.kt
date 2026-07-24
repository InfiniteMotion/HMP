package com.hmp.desktop.data.util

import com.hmp.data.util.DeviceMusicScanner
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceMusicScannerTest {

    @Test
    fun isScanning_initiallyFalse() {
        assertFalse(DeviceMusicScanner.isScanning())
    }

    @Test
    fun setScanDirectories_doesNotThrow() {
        val dirs = listOf(File("/tmp/test1"), File("/tmp/test2"))
        DeviceMusicScanner.setScanDirectories(dirs)
    }

    @Test
    fun setBlockedDirectories_doesNotThrow() {
        DeviceMusicScanner.setBlockedDirectories(listOf("/tmp/blocked"))
    }

    @Test
    fun addScanDirectory_doesNotThrow() {
        DeviceMusicScanner.addScanDirectory(File("/tmp/test"))
    }
}
