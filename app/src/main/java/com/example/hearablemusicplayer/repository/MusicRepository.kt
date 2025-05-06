package com.example.hearablemusicplayer.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.hearablemusicplayer.database.Music
import com.example.hearablemusicplayer.database.MusicAllDao
import com.example.hearablemusicplayer.database.MusicDao
import com.example.hearablemusicplayer.database.MusicExtra
import com.example.hearablemusicplayer.database.MusicExtraDao
import com.example.hearablemusicplayer.database.MusicInfo
import com.example.hearablemusicplayer.database.MusicLabel
import com.example.hearablemusicplayer.database.MusicLabelDao
import com.example.hearablemusicplayer.database.PlaybackHistory
import com.example.hearablemusicplayer.database.PlaybackHistoryDao
import com.example.hearablemusicplayer.database.Playlist
import com.example.hearablemusicplayer.database.PlaylistDao
import com.example.hearablemusicplayer.database.PlaylistItem
import com.example.hearablemusicplayer.database.PlaylistItemDao
import com.example.hearablemusicplayer.database.UserInfo
import com.example.hearablemusicplayer.database.UserInfoDao
import com.example.hearablemusicplayer.database.myenum.LabelName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

class MusicRepository(
    private val musicDao: MusicDao,
    private val musicExtraDao: MusicExtraDao,
    private val userInfoDao: UserInfoDao,
    private val musicAllDao: MusicAllDao,
    private val musicLabelDao: MusicLabelDao,
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val context: Context
) {

    // ------------------- 音乐相关操作 -------------------

    // 随机获取一首歌曲
    suspend fun getRandomMusicInfo(): MusicInfo? {
        return musicAllDao.getRandomMusicInfo()
    }

    // 获取所有音乐 Flow
    fun getAllMusic(): Flow<List<MusicInfo>> = musicAllDao.getAllMusicInfoAsFlow()

    // 获取所有音乐 Flow
    suspend fun getAllMusicInfoAsList(): List<MusicInfo> = musicAllDao.getAllMusicInfo()

    // 根据 ID 获取音乐信息（用于状态监听）
    fun getMusicById(musicId: Long): Flow<Music?> = musicDao.getMusicById(musicId)
    fun getMusicInfoById(musicId: Long): Flow<MusicInfo?> = musicAllDao.getMusicInfoById(musicId)

    // 更新音乐的红心状态（用户喜欢与否）
    suspend fun updateLikedStatus(id: Long, liked: Boolean) {
        userInfoDao.updateLikedStatus(id, liked)
    }

    // 获取音乐的红心状态
    suspend fun getLikedStatus(id: Long): Boolean {
        return userInfoDao.getLikedStatus(id)
    }

    // 根据关键词搜索音乐（匹配标题或艺术家名）
    fun searchMusic(query: String): List<MusicInfo> = musicAllDao.searchMusic(query)

    // 添加音乐标签
    suspend fun addMusicLabel(label: MusicLabel) {
        if(label.label!=LabelName.UNKNOWN) musicLabelDao.insert(label)
        else println("UNKNOWN Label!")
    }

    // 获取音乐标签
    suspend fun getMusicLabels(musicId: Long): List<MusicLabel> {
        return musicLabelDao.getLabelsById(musicId)
    }

    // 获取音乐歌词
    suspend fun getMusicLyrics(musicId: Long): String? {
        return musicExtraDao.getLyricsById(musicId)
    }

    // 从设备本地加载音乐文件（过滤掉时长少于 1 分钟的）
    // 用 StateFlow 通知外部扫描状态
    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun loadMusicFromDevice(){
        _isScanning.value = true
        try {
            val result = performMusicScan()
            musicDao.deleteAll()
            musicExtraDao.deleteAll()
            userInfoDao.deleteAll()
            musicDao.insertAll(result.first)
            musicExtraDao.insertAll(result.second)
            userInfoDao.insertAll(result.third)
        } finally {
            _isScanning.value = false // 确保无论成功失败都会结束
        }
    }

    fun getLyrics(file: File): String? {
        return try {
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag
            // 尝试多种歌词标签读取
            tag?.getFirst(FieldKey.LYRICS)                  // 标准标签
                ?: tag?.getFirst("UNSYNCEDLYRICS")          // 异步歌词标签
                ?: tag?.getFirst("USLT")                    // ID3v2 Lyrics Frame
                ?: tag?.getFirst("LYRICS:SYNCED")           // 部分FLAC特殊标签
        } catch (e: Exception) {
            Log.e("JAudioTagger", "读取歌词失败", e)
            null
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun performMusicScan(): Triple<List<Music>, List<MusicExtra>, List<UserInfo>> {
        val musicList = mutableListOf<Music>()
        val musicExtraList = mutableListOf<MusicExtra>()
        val userInfoList = mutableListOf<UserInfo>()

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
        val selectionArgs = arrayOf("60000") // 大于 1 分钟
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        val retriever = MediaMetadataRetriever()

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
                    e.printStackTrace()
                }

                val lyrics = File(path).let { file ->
                    if (file.exists()) getLyrics(file) else null
                }

                musicList.add(
                    Music(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        path = path,
                    )
                )

                musicExtraList.add(
                    MusicExtra(
                        id = id,
                        albumArtUri = albumArtUri,
                        lyrics = lyrics,
                        bitRate = bitRate,
                        sampleRate = sampleRate,
                        fileSize = fileSize,
                        format = mimeType,
                    )
                )

                userInfoList.add(
                    UserInfo(
                        id = id,
                    )
                )
            }
        }
        retriever.release()
        return Triple(musicList,musicExtraList,userInfoList)
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

    // 从播放列表中移除一项（通过歌曲与播放列表的 ID）
    suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long) {
        playlistItemDao.deleteItemByIds(musicId,playlistId)
    }

    // 更新播放列表
    suspend fun resetPlaylistItems(playlistId: Long, musicList: List<MusicInfo>) {
        playlistItemDao.resetPlaylistItems(playlistId, musicList)
    }

    // 获取播放列表
    fun getMusicInPlaylist(playlistId: Long): Flow<List<Music>> {
        return playlistItemDao.getMusicInPlaylist(playlistId)
    }
    fun getMusicInPlaylist(playlistId: Long,limit:Int): Flow<List<Music>> {
        return playlistItemDao.getMusicInPlaylistLimit(playlistId,limit)
    }
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>> {
        return playlistItemDao.getMusicInfoInPlaylist(playlistId)
    }
    fun getMusicInfoInPlaylist(playlistId: Long,limit:Int): Flow<List<MusicInfo>> {
        return playlistItemDao.getMusicInfoInPlaylistLimit(playlistId,limit)
    }



    // ------------------- 用户播放记录相关操作 -------------------

    //插入一条播放记录
    suspend fun insertPlayback(history: PlaybackHistory) {
        playbackHistoryDao.insert(history)
    }
//
//    //获取最近播放记录
//    fun getRecentHistory(limit: Int): Flow<List<PlaybackHistory>> {
//        return playbackHistoryDao.getRecentHistory(limit)
//    }
//
//    //获取某首歌曲的所有播放记录
//    fun getHistoryForMusic(musicId: Long): Flow<List<PlaybackHistory>> {
//        return playbackHistoryDao.getHistoryForMusic(musicId)
//    }
//
//    //获取播放次数最多的歌曲排行
//    fun getTopPlayed(): Flow<List<PlayCountEntry>> {
//        return playbackHistoryDao.getTopPlayed()
//    }
}

