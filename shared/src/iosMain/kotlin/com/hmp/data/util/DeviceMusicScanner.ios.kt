package com.hmp.data.util

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.URLForDirectory
import platform.Foundation.NSUserDomainMask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

actual object DeviceMusicScanner {
    private val _isScanning = MutableStateFlow(false)

    actual fun isScanning(): Boolean = _isScanning.value

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    actual suspend fun scanMusic(): List<ScannedMusicFile> = withContext(Dispatchers.IO) {
        _isScanning.value = true
        try {
            performScan()
        } finally {
            _isScanning.value = false
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
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

        scanDirectory(documentsURL.path, musicExtensions, musicList, fileManager)

        return musicList
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    private fun scanDirectory(
        directory: String,
        extensions: Set<String>,
        results: MutableList<ScannedMusicFile>,
        fileManager: NSFileManager
    ) {
        val contents = fileManager.contentsOfDirectoryAtPath(directory, null) ?: return

        for (item in contents) {
            val fullPath = "$directory/$item"
            var isDirectory: ObjCBool = false
            if (fileManager.fileExistsAtPath(fullPath, isDirectory)) {
                if (isDirectory.boolValue) {
                    scanDirectory(fullPath, extensions, results, fileManager)
                } else {
                    val ext = item.substringAfterLast(".", "").lowercase()
                    if (ext in extensions) {
                        results.add(createScannedMusicFile(fullPath, item))
                    }
                }
            }
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    private fun createScannedMusicFile(path: String, filename: String): ScannedMusicFile {
        val url = NSURL.fileURLWithPath(path)
        val asset = AVFoundation.AVURLAsset(url)

        val id = path.hashCode().toLong()
        val title = filename.substringBeforeLast(".")
        val artist = "Unknown Artist"
        val album = "Unknown Album"
        val duration = (asset.duration.seconds * 1000).toLong()
        val albumArtUri = ""

        return ScannedMusicFile(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            path = path,
            albumArtUri = albumArtUri,
            format = path.substringAfterLast(".").uppercase()
        )
    }
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
private val AVFoundation = platform.AVFoundation.AVFoundation
