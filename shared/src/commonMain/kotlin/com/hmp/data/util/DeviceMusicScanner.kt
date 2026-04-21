package com.hmp.data.util

data class ScannedMusicFile(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: String,
    val bitRate: Int? = null,
    val sampleRate: Int? = null,
    val fileSize: Long? = null,
    val format: String? = null,
    val lyrics: String? = null
)

expect object DeviceMusicScanner {
    suspend fun scanMusic(): List<ScannedMusicFile>
    fun isScanning(): Boolean
}
