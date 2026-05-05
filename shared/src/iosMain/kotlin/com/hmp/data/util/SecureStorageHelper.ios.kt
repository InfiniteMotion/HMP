package com.hmp.data.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding

@OptIn(ExperimentalForeignApi::class)
actual object SecureStorageHelper {
    private val keysDir: String by lazy {
        val docs = NSFileManager.defaultManager.URLForDirectory(
            NSDocumentDirectory, NSUserDomainMask, null, false, null
        )!!
        val dir = "${docs.path}/.keys"
        if (!NSFileManager.defaultManager.fileExistsAtPath(dir)) {
            NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
        }
        dir
    }

    actual fun encrypt(plainText: String): String {
        val token = "kc_" + kotlin.random.Random.nextLong().toString(36)
        val filePath = "$keysDir/$token"
        val data = (plainText as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return plainText
        NSFileManager.defaultManager.createFileAtPath(filePath, data, null)
        return token
    }

    actual fun decrypt(encrypted: String): String {
        if (!encrypted.startsWith("kc_")) return encrypted
        val filePath = "$keysDir/$encrypted"
        val data = NSFileManager.defaultManager.contentsAtPath(filePath) ?: return encrypted
        val bytes = data.bytes ?: return encrypted
        return bytes.readBytes(data.length.toInt()).decodeToString()
    }
}
