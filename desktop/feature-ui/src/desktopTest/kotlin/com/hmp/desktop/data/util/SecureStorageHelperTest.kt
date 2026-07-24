package com.hmp.desktop.data.util

import com.hmp.data.util.SecureStorageHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class SecureStorageHelperTest {

    @Test
    fun encryptDecrypt_roundTrip() {
        val plain = "Hello, World!"
        val encrypted = SecureStorageHelper.encrypt(plain)
        val decrypted = SecureStorageHelper.decrypt(encrypted)
        assertEquals(plain, decrypted)
    }

    @Test
    fun encrypt_producesDifferentOutput() {
        val plain = "test"
        val encrypted = SecureStorageHelper.encrypt(plain)
        assertNotEquals(plain, encrypted)
    }

    @Test
    fun encrypt_containsColonSeparator() {
        val encrypted = SecureStorageHelper.encrypt("test")
        val parts = encrypted.split(":")
        assertEquals(2, parts.size)
    }

    @Test
    fun decrypt_invalidFormat_throws() {
        assertFailsWith<IllegalArgumentException> {
            SecureStorageHelper.decrypt("invalid")
        }
    }

    @Test
    fun decrypt_emptyString_throws() {
        assertFailsWith<Exception> {
            SecureStorageHelper.decrypt("")
        }
    }

    @Test
    fun encryptDecrypt_emptyString() {
        val encrypted = SecureStorageHelper.encrypt("")
        val decrypted = SecureStorageHelper.decrypt(encrypted)
        assertEquals("", decrypted)
    }

    @Test
    fun encryptDecrypt_unicodeText() {
        val plain = "音乐播放器🎵"
        val encrypted = SecureStorageHelper.encrypt(plain)
        val decrypted = SecureStorageHelper.decrypt(encrypted)
        assertEquals(plain, decrypted)
    }

    @Test
    fun encryptDecrypt_longText() {
        val plain = "A".repeat(10000)
        val encrypted = SecureStorageHelper.encrypt(plain)
        val decrypted = SecureStorageHelper.decrypt(encrypted)
        assertEquals(plain, decrypted)
    }

    @Test
    fun encrypt_samePlaintext_differentCiphertext() {
        // GCM uses random IV, so same plaintext should produce different ciphertext
        val enc1 = SecureStorageHelper.encrypt("test")
        val enc2 = SecureStorageHelper.encrypt("test")
        assertNotEquals(enc1, enc2)
    }
}
