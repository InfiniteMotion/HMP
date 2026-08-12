package com.hmp.data.util

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import com.hmp.domain.music.EditableMusicTags
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File

/**
 * 文件不可直接写入（Android 分区存储下的用户共享媒体），
 * 需要通过 SAF 授权后使用 [MusicTagEditor.writeTags] 的 Uri 重载写入。
 */
class NeedsStorageAccessException(
    filePath: String
) : IllegalStateException("Storage access required to edit: $filePath")

actual object MusicTagEditor {

    @Volatile
    private var appContext: Context? = null

    /**
     * 由 Application 启动时调用，用于 SAF 读写与媒体库刷新。
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
            if (!file.canWrite()) {
                // 分区存储下无直接写权限，需通过 SAF 授权后调用 writeTags(Uri, ...)
                return Result.failure(NeedsStorageAccessException(filePath))
            }
            writeTagsToFile(file, tags)
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 通过 SAF 授权的内容 Uri 写入标签（Android 特有）。
     *
     * 文件内容先复制到带正确音频扩展名的临时文件，由 jaudiotagger 改写后写回 [uri]；
     * 完成后按 [scanPath]（原始文件路径）刷新媒体库索引。
     */
    fun writeTags(uri: Uri, tags: EditableMusicTags, scanPath: String? = null): Result<Unit> {
        val context = appContext
            ?: return Result.failure(
                IllegalStateException("MusicTagEditor has not been initialized")
            )
        val resolver = context.contentResolver
        return try {
            val tempFile = createTempFileFor(scanPath, context)
            try {
                resolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                } ?: return Result.failure(
                    IllegalStateException("Cannot open input stream for: $uri")
                )
                writeTagsToFile(tempFile, tags)
                resolver.openOutputStream(uri, "w")?.use { output ->
                    tempFile.inputStream().use { input -> input.copyTo(output) }
                } ?: return Result.failure(
                    IllegalStateException("Cannot open output stream for: $uri")
                )
            } finally {
                tempFile.delete()
            }
            scanPath?.let { path ->
                MediaScannerConnection.scanFile(context, arrayOf(path), null, null)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * jaudiotagger 按文件扩展名识别音频格式，临时文件必须保留原扩展名。
     */
    private fun createTempFileFor(originalPath: String?, context: Context): File {
        val extension = originalPath
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.takeIf { it.isNotBlank() && it.length <= 8 }
            ?: "bin"
        return File.createTempFile("hmp_tag_edit_", ".$extension", context.cacheDir)
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
