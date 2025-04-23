package com.example.hearablemusicplayer.repository

import android.net.Uri
import android.content.Context
import android.provider.MediaStore
import android.content.ContentUris
import kotlinx.coroutines.flow.Flow
import com.example.hearablemusicplayer.database.Music
import com.example.hearablemusicplayer.database.MusicDao
import com.example.hearablemusicplayer.database.PlaybackHistory
import com.example.hearablemusicplayer.database.PlaybackHistoryDao
import com.example.hearablemusicplayer.database.Playlist
import com.example.hearablemusicplayer.database.PlaylistDao
import com.example.hearablemusicplayer.database.PlaylistItem
import com.example.hearablemusicplayer.database.PlaylistItemDao
import com.example.hearablemusicplayer.database.myClass.PlayCountEntry


class MusicRepository(
    private val musicDao: MusicDao,
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val context: Context
) {

    // ------------------- 音乐相关操作 -------------------

    // 将音乐数据保存到数据库，同时保留用户之前标记的“红心”状态
    suspend fun saveMusicToDatabase(musics: List<Music>) {
        val existingMusic = musicDao.getAllAsList()
        val likedMap = existingMusic.associateBy({ it.id }, { it.liked })

        val updatedList = musics.map { newMusic ->
            newMusic.copy(liked = likedMap[newMusic.id] ?: false)
        }

        musicDao.insertAll(updatedList)
    }

    // 清空音乐数据库
    suspend fun clearMusicDatabase() {
        musicDao.deleteAllMusic()
    }

    // 获取所有音乐 Flow
    fun getAllMusic(): Flow<List<Music>> = musicDao.getAll()

    // 获取所有音乐 Flow
    suspend fun getAllMusicAsList(): List<Music> = musicDao.getAllAsList()

    // 根据 ID 获取音乐信息（用于状态监听）
    fun getMusicById(musicId: Long): Flow<Music?> = musicDao.getMusicById(musicId)

    // 获取指定音乐的路径
    suspend fun getMusicPathById(musicId: Long): String = musicDao.getMusicPathById(musicId)

    // 获取随机的 5 首音乐
    suspend fun getRandomMusic(): List<Music> = musicDao.getRandomMusic()

    // 更新音乐的红心状态（用户喜欢与否）
    suspend fun updateLikedStatus(id: Long, liked: Boolean) {
        musicDao.updateLikedStatus(id, liked)
    }

    // 根据关键词搜索音乐（匹配标题或艺术家名）
    fun searchMusic(query: String): List<Music> = musicDao.searchMusic(query)

    // 从设备本地加载音乐文件（过滤掉时长少于 1 分钟的）
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
        val selectionArgs = arrayOf("60000") // 大于 1 分钟
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
                        albumArtUri = albumArtUri,
                        liked = false
                    )
                )
            }
        }
        return musicList
    }

    // ------------------- 播放列表相关操作 -------------------

    // 创建一个新的播放列表并返回其 ID
    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insert(Playlist(name = name))
    }

    // 向指定播放列表中添加一首音乐
    suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String) {
        val item = PlaylistItem(
            songUrl = musicPath,
            songId = musicId,
            playlistId = playlistId,
            playedAt = System.currentTimeMillis()
        )
        playlistItemDao.insert(item)
    }

    // 向指定播放列表中添加一个播放项
    suspend fun addPlaylistItem(item: PlaylistItem?) {
        if (item != null) {
            playlistItemDao.insert(item)
        }
    }

    // 从播放列表中移除一项（通过 item 的 ID）
    suspend fun removeItemFromPlaylist(id: Long) {
        playlistItemDao.deleteItem(id)
    }

    // 从播放列表中移除一项（通过歌曲与播放列表的 ID）
    suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long) {
        playlistItemDao.deleteItemByIds(musicId,playlistId)
    }

    // 删除某个播放列表
    suspend fun deletePlaylist(id: Long) {
        playlistDao.deletePlaylist(id)
    }

    // 更新播放列表
    suspend fun resetPlaylistItems(playlistId: Long, musicList: List<Music>) {
        playlistItemDao.resetPlaylistItems(playlistId, musicList)
    }

    // 获取播放列表
    fun getSongsAfterById(currentId: Long): Flow<List<Music>> {
        return musicDao.getSongsAfterById(currentId)
    }
    fun getMusicInPlaylist(playlistId: Long): Flow<List<Music>> {
        return playlistItemDao.getMusicInPlaylist(playlistId)
    }
    fun getMusicInPlaylist(playlistId: Long,limit:Int): Flow<List<Music>> {
        return playlistItemDao.getMusicInPlaylistLimit(playlistId,limit)
    }



    // ------------------- 用户播放记录相关操作 -------------------

    //插入一条播放记录
    suspend fun insertPlayback(history: PlaybackHistory) {
        playbackHistoryDao.insert(history)
    }

    //获取最近播放记录
    fun getRecentHistory(limit: Int): Flow<List<PlaybackHistory>> {
        return playbackHistoryDao.getRecentHistory(limit)
    }

    //获取某首歌曲的所有播放记录
    fun getHistoryForMusic(musicId: Long): Flow<List<PlaybackHistory>> {
        return playbackHistoryDao.getHistoryForMusic(musicId)
    }

    //获取播放次数最多的歌曲排行
    fun getTopPlayed(): Flow<List<PlayCountEntry>> {
        return playbackHistoryDao.getTopPlayed()
    }
}

