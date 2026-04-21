package com.hmp.data.util

import platform.Security.SecKeyCopyError
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecRandomCopyBytes
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.dataUsingEncoding
import platform.Foundation.init
import platform.Foundation.length

actual object SecureStorageHelper {
    private const val KEY_TAG = "com.hmp.secure.key"

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    actual fun encrypt(plainText: String): String {
        val key = getOrCreateKey()
        val data = plainText.dataUsingEncoding(kotlinx.cinterop.ObjCRuntime.kCFStringEncodingUTF8)
            ?: return ""

        val encryptedData = NSMutableData()
        val status = SecKeyCreateEncryptedData(
            key,
            SecKeyAlgorithm.AES_GCM_NoPadding,
            data as NSData,
            encryptedData as NSMutableData
        )

        return if (status) {
            val base64 = android.util.Base64.encodeToString(
                encryptedData.bytes?.toByteArray() ?: ByteArray(0),
                android.util.Base64.NO_WRAP
            )
            base64
        } else {
            ""
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    actual fun decrypt(encrypted: String): String {
        val key = getOrCreateKey()
        val encryptedData = android.util.Base64.decode(encrypted, android.util.Base64.NO_WRAP)
        val nsData = NSData.dataWithBytes(encryptedData, length = encryptedData.size.toULong())

        val decryptedData = NSMutableData()
        val status = SecKeyCreateDecryptedData(
            key,
            SecKeyAlgorithm.AES_GCM_NoPadding,
            nsData,
            decryptedData as NSMutableData
        )

        return if (status) {
            String(decryptedData.bytes?.toByteArray() ?: ByteArray(0), Charsets.UTF_8)
        } else {
            ""
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    private fun getOrCreateKey(): SecKey {
        val query = mapOf(
            SecItemClass to kSecClassKey,
            SecAttrApplicationTag to KEY_TAG,
            SecAttrKeyType to kSecAttrKeyTypeAES,
            SecReturnRef to true
        )

        var result = SecItemCopyMatching(query, null)
        if (result == SecKeyCopyError.success) {
            return result as SecKey
        }

        val keySize = 256
        val attributes = mapOf(
            SecAttrKeyType to kSecAttrKeyTypeAES,
            SecAttrKeySizeInBits to keySize,
            SecAttrApplicationTag to KEY_TAG,
            SecPrivateKeyAttrs to mapOf(
                SecIsPermanent to true
            )
        )

        var error: kotlinx.cinterop.CPointer< SecKeyCopyError>? = null
        val privateKey = SecKeyCreateRandomKey(attributes, error)
        return SecKeyCopyPublicKey(privateKey!!)!!
    }
}
