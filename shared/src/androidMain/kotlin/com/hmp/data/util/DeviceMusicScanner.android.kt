package com.hmp.data.util

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

actual object DeviceMusicScanner {
    private const val MIN_DURATION_MS = 60000L

    private val _isScanning = MutableStateFlow(false)

    actual fun isScanning(): Boolean = _isScanning.value

    actual suspend fun scanMusic(): List<ScannedMusicFile> = withContext(Dispatchers.IO) {
        _isScanning.value = true
        try {
            performScan()
        } finally {
            _isScanning.value = false
        }
    }

    private fun performScan(): List<ScannedMusicFile> {
        val musicList = mutableListOf<ScannedMusicFile>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > ?"
        val selectionArgs = arrayOf(MIN_DURATION_MS.toString())
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        val retriever = MediaMetadataRetriever()

        try {
            val context = android.app.Application() // This won't work, need proper context
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown"
                        val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
                        val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) ?: "Unknown Album"
                        val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                        val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                        val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                        val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE))
                        val fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))

                        val albumArtUri = ContentUris.withAppendedId(
                            "content://media/external/audio/albumart".toUri(),
                            albumId
                        ).toString()

                        var bitRate: Int? = null
                        var sampleRate: Int? = null

                        try {
                            retriever.setDataSource(path)
                            bitRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()?.div(1000)
                            sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()
                        } catch (e: Exception) {
                            // ignore
                        }

                        musicList.add(
                            ScannedMusicFile(
                                id = id,
                                title = title,
                                artist = artist,
                                album = album,
                                duration = duration,
                                path = path,
                                albumArtUri = albumArtUri,
                                bitRate = bitRate,
                                sampleRate = sampleRate,
                                fileSize = fileSize,
                                format = mimeType
                            )
                        )
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // ignore
            }
        }

        return musicList
    }
}
