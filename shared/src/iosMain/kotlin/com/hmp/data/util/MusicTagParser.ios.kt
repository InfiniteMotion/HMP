package com.hmp.data.util

import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVURLAsset
import platform.Foundation.NSURL

actual object MusicTagParser {

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    actual fun parseLyrics(filePath: String): String? {
        return try {
            val url = NSURL.fileURLWithPath(filePath)
            val asset = AVURLAsset(url)
            // iOS lyrics parsing would go here
            null
        } catch (e: Exception) {
            null
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    actual fun parseMetadata(filePath: String): MusicMetadata? {
        return try {
            val url = NSURL.fileURLWithPath(filePath)
            val asset = AVURLAsset(url)
            val duration = (asset.duration.seconds * 1000).toLong()

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
