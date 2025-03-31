package com.example.hearablemusicplayer.repository

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.example.hearablemusicplayer.model.Music

class MusicRepository {
    suspend fun loadMusicFromMediaStore(context: Context): List<Music> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.TRACK
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > ?"
        val selectionArgs = arrayOf("60000") // 仅获取时长大于 60 秒的音乐
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        val musicList = mutableListOf<Music>()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val musicUri = getMusicUri(context, id)
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val year = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                val albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                val artistId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID))
                val isMusic = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC))
                val composer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
                val genre = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE))
                val track = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK))
                val albumCover = getEmbeddedCoverArt(context,musicUri)
                musicList.add(
                    Music(
                        id = id,
                        musicUri=musicUri,
                        title = title,
                        artist = artist,
                        album = album,
                        year = year,
                        duration = duration,
                        size = size,
                        path = path,
                        displayName = displayName,
                        albumId = albumId,
                        artistId = artistId,
                        isMusic = isMusic == 1,
                        composer = composer,
                        genre = genre,
                        track = track,
                        albumCover = albumCover
                    )
                )
            }
        }

        return musicList
    }

    private fun getMusicUri(context: Context, musicId: Long): Uri? {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val selection = "${MediaStore.Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(musicId.toString())
        val sortOrder = null

        val resolver = context.contentResolver
        val cursor = resolver.query(collection, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            if (it.moveToFirst()) {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val id = it.getLong(idColumn)
                return ContentUris.withAppendedId(collection, id)
            }
        }
        return null
    }

    fun getEmbeddedCoverArt(context: Context, musicUri: Uri?): Bitmap? {
        val retriever = MediaMetadataRetriever()
        try {
            // 设置数据源为音乐文件的路径或Uri
            retriever.setDataSource(context, musicUri)
            val coverBytes = retriever.embeddedPicture
            if (coverBytes != null) {
                // 将字节数组转换为Bitmap
                return BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return null
    }
}
