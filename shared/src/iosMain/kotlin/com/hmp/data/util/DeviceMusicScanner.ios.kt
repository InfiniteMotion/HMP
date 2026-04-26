package com.hmp.data.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSFileSize

@OptIn(ExperimentalForeignApi::class)
actual object DeviceMusicScanner {
    private val _isScanning = MutableStateFlow(false)

    actual fun isScanning(): Boolean = _isScanning.value

    actual suspend fun scanMusic(): List<ScannedMusicFile> = withContext(Dispatchers.Default) {
        _isScanning.value = true
        try {
            performScan()
        } finally {
            _isScanning.value = false
        }
    }

    private fun performScan(): List<ScannedMusicFile> {
        val musicList = mutableListOf<ScannedMusicFile>()
        val fileManager = NSFileManager.defaultManager

        val documentsURL = fileManager.URLForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask,
            null,
            true,
            null
        ) ?: return musicList

        val musicExtensions = setOf("mp3", "m4a", "wav", "flac", "aac", "ogg")

        scanDirectory(documentsURL.path!!, musicExtensions, musicList, fileManager)

        return musicList
    }

    private fun scanDirectory(
        directory: String,
        extensions: Set<String>,
        results: MutableList<ScannedMusicFile>,
        fileManager: NSFileManager
    ) {
        val contents = fileManager.contentsOfDirectoryAtPath(directory, null) ?: return

        for (item in contents) {
            val fullPath = "$directory/$item"
            val url = NSURL.fileURLWithPath(fullPath)
            if (url.hasDirectoryPath) {
                scanDirectory(fullPath, extensions, results, fileManager)
            } else {
                val ext = item.toString().substringAfterLast(".", "").lowercase()
                if (ext in extensions) {
                    results.add(createScannedMusicFile(fullPath, item.toString()))
                }
            }
        }
    }

    private fun createScannedMusicFile(path: String, filename: String): ScannedMusicFile {
        val title = filename.substringBeforeLast(".")

        val fileSize: Long? = try {
            val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(path, null)
            if (attributes != null) {
                val sizeValue = attributes[NSFileSize]
                when (sizeValue) {
                    is Long -> sizeValue
                    is Int -> sizeValue.toLong()
                    is Double -> sizeValue.toLong()
                    else -> null
                }
            } else null
        } catch (_: Exception) {
            null
        }

        return ScannedMusicFile(
            id = path.hashCode().toLong(),
            title = title,
            artist = "Unknown Artist",
            album = "Unknown Album",
            duration = 0L,
            path = path,
            albumArtUri = "",
            bitRate = null,
            sampleRate = null,
            fileSize = fileSize,
            format = path.substringAfterLast(".").uppercase()
        )
    }
}
