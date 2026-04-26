package com.hmp.data.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMetadataCommonKeyAlbumName
import platform.AVFoundation.AVMetadataCommonKeyArtist
import platform.AVFoundation.AVMetadataCommonKeyTitle
import platform.AVFoundation.AVURLAsset
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
actual object MusicTagParser {

    actual fun parseLyrics(filePath: String): String? = null

    actual fun parseMetadata(filePath: String): MusicMetadata? {
        val bridge = MetadataParserBridge.registered

        if (bridge != null) {
            return bridge.parse(filePath)
        }

        // Fallback: basic info from file path only
        return try {
            val url = NSURL.fileURLWithPath(filePath)
            val fallbackTitle = url.lastPathComponent?.substringBeforeLast(".") ?: "Unknown"
            MusicMetadata(
                title = fallbackTitle,
                artist = "Unknown Artist",
                album = "Unknown Album",
                format = filePath.substringAfterLast(".").uppercase()
            )
        } catch (_: Exception) {
            null
        }
    }
}

object MetadataParserBridge {
    var registered: Parser? = null

    fun register(parser: Parser) {
        registered = parser
    }

    interface Parser {
        fun parse(filePath: String): MusicMetadata?
    }
}
