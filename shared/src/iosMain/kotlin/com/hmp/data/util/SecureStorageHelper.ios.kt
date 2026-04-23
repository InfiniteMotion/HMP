package com.hmp.data.util

import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.Foundation.create
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.dataWithBase64EncodedString
import platform.Foundation.bytes
import platform.Foundation.length
import platform.Security.*
import kotlinx.cinterop.*
import kotlin.experimental.and

actual object SecureStorageHelper {
    private const val KEY_TAG = "com.hmp.secure.key"

    actual fun encrypt(plainText: String): String {
        val key = getOrCreateKey()
        val data = plainText.dataUsingEncoding(NSUTF8StringEncoding)
            ?: return ""

        val error = alloc<CFErrorRefVar>()
        val encrypted = SecKeyCreateEncryptedData(
            key,
            kSecKeyAlgorithmECIESEncryptionCoeC8MACKeyAndXorKeyX963SHA512AESGCM,
            data,
            error.ptr
        ) ?: return ""

        return (encrypted as NSData).base64EncodedStringWithOptions(0u)
    }

    actual fun decrypt(encrypted: String): String {
        val key = getOrCreateKey()
        val encryptedData = NSData.dataWithBase64EncodedString(encrypted)
            ?: return ""

        val error = alloc<CFErrorRefVar>()
        val decrypted = SecKeyCreateDecryptedData(
            key,
            kSecKeyAlgorithmECIESEncryptionCoeC8MACKeyAndXorKeyX963SHA512AESGCM,
            encryptedData,
            error.ptr
        ) ?: return ""

        return NSString.create(
            bytes = (decrypted as NSData).bytes,
            length = (decrypted as NSData).length,
            encoding = NSUTF8StringEncoding
        )?.toString() ?: ""
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getOrCreateKey(): SecKey {
        val query: Map<String, Any> = mapOf(
            kSecClass as String to kSecClassKey,
            kSecAttrApplicationTag as String to KEY_TAG,
            kSecAttrKeyType as String to kSecAttrKeyTypeAES,
            kSecReturnRef as String to true
        )

        var result: CFTypeRef? = null
        val status = SecItemCopyMatching(query, result)
        if (status == errSecSuccess) {
            return result as SecKey
        }

        // Key not found, create a new one
        val attributes: Map<String, Any> = mapOf(
            kSecAttrKeyType as String to kSecAttrKeyTypeAES,
            kSecAttrKeySizeInBits as String to 256,
            kSecAttrApplicationTag as String to KEY_TAG
        )

        val key = SecKeyCreateRandomKey(attributes, null)
            ?: throw IllegalStateException("Failed to create secure key")

        return key
    }
}
