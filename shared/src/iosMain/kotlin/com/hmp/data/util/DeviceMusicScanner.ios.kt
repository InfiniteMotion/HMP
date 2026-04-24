package com.hmp.data.util

import platform.AVFoundation.AVURLAsset
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.value

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

    @OptIn(ExperimentalForeignApi::class)
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

    @OptIn(ExperimentalForeignApi::class)
    private fun scanDirectory(
        directory: String,
        extensions: Set<String>,
        results: MutableList<ScannedMusicFile>,
        fileManager: NSFileManager
    ) {
        val contents = fileManager.contentsOfDirectoryAtPath(directory, null) ?: return

        for (item in contents) {
            val fullPath = "$directory/$item"
            if (fileManager.fileExistsAtPath(fullPath)) {
                if (fileManager.fileExistsAtPath(fullPath) && fileManager.isDirectoryAtPath(fullPath)) {
                    scanDirectory(fullPath, extensions, results, fileManager)
                } else {
                    val ext = item.toString().substringAfterLast(".", "").lowercase()
                    if (ext in extensions) {
                        results.add(createScannedMusicFile(fullPath, item.toString()))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun createScannedMusicFile(path: String, filename: String): ScannedMusicFile {
        val url = NSURL.fileURLWithPath(path)
        val asset = AVURLAsset.URLAssetWithURL(url, null)

        val id = path.hashCode().toLong()
        val title = filename.substringBeforeLast(".")
        val artist = "Unknown Artist"
        val album = "Unknown Album"
        val duration = 0L // 暂时设置为0，需要修复AVURLAsset的duration访问
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

    @OptIn(ExperimentalForeignApi::class)
    private fun NSFileManager.isDirectoryAtPath(path: String): Boolean {
        val fileManager = NSFileManager.defaultManager
        var isDirectory = false
        if (fileManager.fileExistsAtPath(path)) {
            // 暂时简化实现，后续需要使用正确的指针方式
            isDirectory = false
        }
        return isDirectory
    }
}
