package com.hmp.data.util

expect object SecureStorageHelper {
    fun encrypt(plainText: String): String
    fun decrypt(encrypted: String): String
}
