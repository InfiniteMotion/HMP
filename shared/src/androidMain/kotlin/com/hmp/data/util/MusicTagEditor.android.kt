package com.hmp.data.util

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import com.hmp.domain.music.EditableMusicTags
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File

actual object MusicTagEditor {

    @Volatile
    private var appContext: Context? = null

    /**
     * 由 Application 启动时调用，用于 MediaStore 读写与媒体库刷新。
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun writeTags(filePath: String, tags: EditableMusicTags): Result<Unit> {
        val context = appContext
            ?: return Result.failure(
                IllegalStateException("MusicTagEditor has not been initialized")
            )
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return Result.failure(IllegalStateException("Music file not found: $filePath"))
            }
            if (file.canWrite()) {
                // 已具备直接写权限（如已授予“所有文件访问”）
                writeTagsToFile(file, tags)
            } else {
                // 分区存储：先写临时副本，再通过 MediaStore 流写回
                writeViaMediaStore(context, file, tags)
            }
            // 通知媒体库刷新，重建专辑封面等派生数据
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun writeViaMediaStore(context: Context, file: File, tags: EditableMusicTags) {
        val resolver = context.contentResolver
        val uri = queryContentUri(resolver, file.absolutePath)
            ?: throw IllegalStateException("Cannot find media store entry for: ${file.absolutePath}")

        val tempFile = File.createTempFile("hmp_tag_edit_", ".tmp", context.cacheDir)
        try {
            file.copyTo(tempFile, overwrite = true)
            writeTagsToFile(tempFile, tags)

            resolver.openOutputStream(uri, "w")?.use { out ->
                tempFile.inputStream().use { it.copyTo(out) }
            } ?: throw IllegalStateException("Cannot open output stream for: $uri")

            val values = ContentValues().apply {
                tags.title?.takeIf { it.isNotBlank() }?.let { put(MediaStore.Audio.Media.TITLE, it) }
                tags.artist?.takeIf { it.isNotBlank() }?.let { put(MediaStore.Audio.Media.ARTIST, it) }
                tags.album?.takeIf { it.isNotBlank() }?.let { put(MediaStore.Audio.Media.ALBUM, it) }
                tags.year?.takeIf { it.isNotBlank() }?.let {
                    put(MediaStore.Audio.Media.YEAR, it.toIntOrNull() ?: 0)
                }
                tags.genre?.takeIf { it.isNotBlank() }?.let { put(MediaStore.Audio.Media.GENRE, it) }
                tags.track?.takeIf { it.isNotBlank() }?.let {
                    put(MediaStore.Audio.Media.TRACK, it.toIntOrNull() ?: 0)
                }
            }
            if (values.size() > 0) {
                resolver.update(uri, values, null, null)
            }
        } finally {
            tempFile.delete()
        }
    }

    private fun queryContentUri(resolver: ContentResolver, path: String): Uri? {
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val selection = "${MediaStore.Audio.Media.DATA} = ?"
        resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            arrayOf(path),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
            }
        }
        return null
    }

    private fun writeTagsToFile(file: File, tags: EditableMusicTags) {
        val audioFile = AudioFileIO.read(file)
        val tag = audioFile.tagOrCreateAndSetDefault
        tags.title?.takeIf { it.isNotBlank() }?.let { tag.setField(FieldKey.TITLE, it) }
        tags.artist?.takeIf { it.isNotBlank() }?.let { tag.setField(FieldKey.ARTIST, it) }
        tags.album?.takeIf { it.isNotBlank() }?.let { tag.setField(FieldKey.ALBUM, it) }
        tags.year?.takeIf { it.isNotBlank() }?.let { tag.setField(FieldKey.YEAR, it) }
        tags.genre?.takeIf { it.isNotBlank() }?.let { tag.setField(FieldKey.GENRE, it) }
        tags.track?.takeIf { it.isNotBlank() }?.let { tag.setField(FieldKey.TRACK, it) }
        tags.lyrics?.takeIf { it.isNotBlank() }?.let { tag.setField(FieldKey.LYRICS, it) }
        tags.albumArt?.let { art ->
            tag.deleteArtworkField()
            if (art.isNotEmpty()) {
                tag.setField(artworkFromBytes(art))
            }
        }
        AudioFileIO.write(audioFile)
    }

    private fun artworkFromBytes(bytes: ByteArray): Artwork {
        return ArtworkFactory.getNew().apply {
            binaryData = bytes
            mimeType = detectImageMimeType(bytes)
            description = ""
        }
    }

    private fun detectImageMimeType(bytes: ByteArray): String {
        return when {
            bytes.size >= 8 &&
                bytes[0] == 0x89.toByte() &&
                bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x4E.toByte() &&
                bytes[3] == 0x47.toByte() -> "image/png"
            bytes.size >= 3 &&
                bytes[0] == 0xFF.toByte() &&
                bytes[1] == 0xD8.toByte() &&
                bytes[2] == 0xFF.toByte() -> "image/jpeg"
            else -> "image/jpeg"
        }
    }
}
