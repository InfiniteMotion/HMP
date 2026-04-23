package com.hmp.data.util

import platform.AVFoundation.AVURLAsset
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.cinterop.ObjCBool
import kotlinx.cinterop.ExperimentalForeignApi

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
            val isDir = ObjCBool(false)
            if (fileManager.fileExistsAtPath(fullPath, isDir)) {
                if (isDir.value) {
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

    private fun createScannedMusicFile(path: String, filename: String): ScannedMusicFile {
        val url = NSURL.fileURLWithPath(path)
        val asset = AVURLAsset(URL = url)

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
