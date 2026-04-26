package com.hmp.data.util

actual object SecureStorageHelper {
    actual fun encrypt(plainText: String): String = plainText
    actual fun decrypt(encrypted: String): String = encrypted
}
