package com.hmp.shared.resource

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
actual object SharedIconLoader {
    actual suspend fun loadIcon(iconName: String): ByteArray? {
        val path = findResourcePath("icons/$iconName", "png") ?: return null
        val nsData = NSData.dataWithContentsOfFile(path) ?: return null
        return nsData.toByteArray()
    }

    fun loadIconAsData(iconName: String): NSData? {
        val path = findResourcePath("icons/$iconName", "png") ?: return null
        return NSData.dataWithContentsOfFile(path)
    }

    private fun findResourcePath(name: String, ext: String): String? {
        NSBundle.mainBundle.pathForResource(name, ofType = ext)?.let { return it }
        for (bundle in NSBundle.allBundles) {
            (bundle as? NSBundle)?.pathForResource(name, ofType = ext)?.let { return it }
        }
        return null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    val bytes = ByteArray(length)
    if (length > 0) {
        bytes.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
        }
    }
    return bytes
}
