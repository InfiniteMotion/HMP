package com.hmp.data.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
actual object MusicTagParser {

    actual fun parseLyrics(filePath: String): String? {
        return null
    }

    actual fun parseMetadata(filePath: String): MusicMetadata? {
        return try {
            val url = NSURL.fileURLWithPath(filePath)
            val title = url.lastPathComponent?.substringBeforeLast(".") ?: "Unknown"
            MusicMetadata(
                title = title,
                artist = "Unknown Artist",
                album = "Unknown Album",
                duration = 0L,
                bitRate = null,
                sampleRate = null,
                format = filePath.substringAfterLast(".").uppercase()
            )
        } catch (_: Exception) {
            null
        }
    }
}
