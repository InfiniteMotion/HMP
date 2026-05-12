package com.hmp.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File

actual object DeviceMusicScanner {
    private val _isScanning = MutableStateFlow(false)
    private val musicExtensions = setOf("mp3", "flac", "m4a", "wav", "aac", "ogg", "wma", "alac", "opus")

    private val scanDirectories = mutableListOf<File>()
    private val blockedDirectories = mutableSetOf<String>()

    fun addScanDirectory(dir: File) {
        scanDirectories.add(dir)
    }

    fun setScanDirectories(dirs: List<File>) {
        scanDirectories.clear()
        scanDirectories.addAll(dirs)
    }

    fun setBlockedDirectories(dirs: List<String>) {
        blockedDirectories.clear()
        blockedDirectories.addAll(dirs.map {
            try { File(it).canonicalPath } catch (_: Exception) { File(it).absolutePath }
        })
    }

    actual fun isScanning(): Boolean = _isScanning.value

    actual suspend fun scanMusic(): List<ScannedMusicFile> = withContext(Dispatchers.IO) {
        _isScanning.value = true
        try {
            val dirsToScan = if (scanDirectories.isEmpty()) {
                getDefaultMusicDirectories()
            } else {
                scanDirectories
            }
            val results = mutableListOf<ScannedMusicFile>()
            for (dir in dirsToScan) {
                if (dir.exists() && dir.isDirectory) {
                    scanDirectory(dir, results)
                }
            }
            results
        } finally {
            _isScanning.value = false
        }
    }

    private fun getDefaultMusicDirectories(): List<File> {
        val home = File(System.getProperty("user.home"))
        return listOfNotNull(
            File(home, "Music"),
            File(home, "Downloads"),
        ).filter { it.exists() && it.isDirectory }
    }

    private fun scanDirectory(dir: File, results: MutableList<ScannedMusicFile>) {
        val canonicalPath = try { dir.canonicalPath } catch (_: Exception) { dir.absolutePath }
        if (blockedDirectories.any { canonicalPath.startsWith(it) }) return
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                scanDirectory(file, results)
            } else if (file.extension.lowercase() in musicExtensions) {
                results.add(createScannedMusicFile(file))
            }
        }
    }

    private fun createScannedMusicFile(file: File): ScannedMusicFile {
        val metadata = MusicTagParser.parseMetadata(file.absolutePath)
        val albumArtUri = metadata?.albumArt?.let { saveAlbumArt(it, file.absolutePath) } ?: ""
        return ScannedMusicFile(
            id = generateStableId(file.absolutePath),
            title = metadata?.title ?: file.nameWithoutExtension,
            artist = metadata?.artist ?: "Unknown Artist",
            album = metadata?.album ?: "Unknown Album",
            duration = metadata?.duration ?: 0L,
            path = file.absolutePath,
            albumArtUri = albumArtUri,
            bitRate = metadata?.bitRate,
            sampleRate = metadata?.sampleRate,
            fileSize = file.length(),
            format = metadata?.format ?: file.extension.uppercase(),
            lyrics = metadata?.lyrics
        )
    }

    private fun getCoversDirectory(): File {
        val dir = File(System.getProperty("user.home"), ".hmp/covers")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun saveAlbumArt(data: ByteArray, filePath: String): String? {
        return try {
            val coversDir = getCoversDirectory()
            val id = generateStableId(filePath)
            val coverFile = File(coversDir, "cover_$id.jpg")
            if (!coverFile.exists()) {
                coverFile.writeBytes(data)
            }
            coverFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun generateStableId(path: String): Long {
        var hash = 0xcbf29ce484222325UL
        for (byte in path.encodeToByteArray()) {
            hash = hash xor byte.toULong()
            hash *= 0x100000001b3UL
        }
        return hash.toLong()
    }
}
