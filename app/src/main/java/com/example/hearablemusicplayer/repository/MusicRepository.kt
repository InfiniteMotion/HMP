package com.example.hearablemusicplayer.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.hearablemusicplayer.database.Music
import com.example.hearablemusicplayer.database.MusicDao
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val musicDao: MusicDao,
    private val context: Context
) {
    suspend fun loadMusicFromDevice(): List<Music> {
        val musicList = mutableListOf<Music>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > ?"
        val selectionArgs = arrayOf("60000") // 仅获取时长大于 60 秒的音乐
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                ).toString()

                musicList.add(
                    Music(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        path = path,
                        albumArtUri = albumArtUri
                    )
                )
            }
        }

        return musicList
    }

    // 向数据库插入音乐
    suspend fun saveMusicToDatabase(musics: List<Music>) {
        musicDao.insertAll(musics)
    }

    // 清空数据库中的音乐列表
    suspend fun clearMusicDatabase() {
        musicDao.deleteAllMusic()
    }

    // 获取数据库中的所有音乐
    fun getAllMusic(): LiveData<List<Music>> {
        return musicDao.getAll()
    }

    // 依据id获取数据库中的音乐
    fun getMusicById(musicId: String): Flow<Music?> {
        return musicDao.getMusicById(musicId)
    }

    // 随机顺序与数量获取数据库中的音乐列表
    suspend fun getRandomMusic(): List<Music> {
        return musicDao.getRandomMusic()
    }

    // 根据关键词搜索音乐
    fun searchMusic(query: String): List<Music> {
        return musicDao.searchMusic(query)
    }
}