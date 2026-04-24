package com.hmp.data.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVURLAsset
import platform.Foundation.NSURL

actual object MusicTagParser {

    @OptIn(ExperimentalForeignApi::class)
    actual fun parseLyrics(filePath: String): String? {
        return try {
            val url = NSURL.fileURLWithPath(filePath)
            val asset = AVURLAsset.URLAssetWithURL(url, null)
            // iOS lyrics parsing would go here
            null
        } catch (e: Exception) {
            null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun parseMetadata(filePath: String): MusicMetadata? {
        return try {
            val url = NSURL.fileURLWithPath(filePath)
            val asset = AVURLAsset.URLAssetWithURL(url, null)
            val duration = 0L // 暂时设置为0，需要修复AVURLAsset的duration访问

            MusicMetadata(
                title = null,
                artist = null,
                album = null,
                duration = duration,
                format = filePath.substringAfterLast(".").uppercase()
            )
        } catch (e: Exception) {
            null
        }
    }
}
