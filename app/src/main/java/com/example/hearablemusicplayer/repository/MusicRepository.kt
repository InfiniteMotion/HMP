package com.example.hearablemusicplayer.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.hearablemusicplayer.database.DailyMusicInfo
import com.example.hearablemusicplayer.database.ListeningDuration
import com.example.hearablemusicplayer.database.ListeningDurationDao
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
import com.example.hearablemusicplayer.database.myenum.LabelCategory
import com.example.hearablemusicplayer.database.myenum.LabelName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val musicDao: MusicDao,
    private val musicExtraDao: MusicExtraDao,
    private val userInfoDao: UserInfoDao,
    private val musicAllDao: MusicAllDao,
    private val musicLabelDao: MusicLabelDao,
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val listeningDurationDao: ListeningDurationDao,
    @ApplicationContext private val context: Context
) {

    // ------------------- 音乐相关操作 -------------------

    // 排序音乐
    private val musicFields = listOf("id", "title", "artist", "album", "duration")
    private val extraFields = listOf("bitRate", "sampleRate", "fileSize", "format", "language", "year")
    private val userInfoFields = listOf("liked", "disLiked", "lastPlayed", "playCount", "skippedCount", "userRating")
    suspend fun getAllMusicInfoAsList(orderBy: String = "id", orderType: String = "ASC"): List<MusicInfo> {
        val safeOrderType = if (orderType.uppercase() == "DESC") "DESC" else "ASC"
        val tablePrefix = when {
            musicFields.contains(orderBy) -> "music"
            extraFields.contains(orderBy) -> "musicExtra"
            userInfoFields.contains(orderBy) -> "userInfo"
            else -> "music"
        }

        val queryString = """
            SELECT * FROM music 
            LEFT JOIN musicExtra ON music.id = musicExtra.id
            LEFT JOIN userInfo ON music.id = userInfo.id
            ORDER BY $tablePrefix.$orderBy $safeOrderType
        """.trimIndent()

        val query = SimpleSQLiteQuery(queryString)
        return musicAllDao.getAllMusicInfoAsList(query)
    }

    // 随机获取一首歌曲
    suspend fun getRandomMusicInfoWithMissingExtra(): MusicInfo? {
        return musicAllDao.getRandomMusicInfoWithMissingExtra()
    }

    suspend fun getRandomMusicInfoWithExtra(): MusicInfo? {
        return musicAllDao.getRandomMusicInfoWithExtra()
    }

    // 获取当前数据库音乐数量
    fun getMusicCount(): Flow<Int> = musicDao.getMusicCount()

    // 获取当前已获得额外信息的音乐数量
    fun getMusicWithExtraCount(): Flow<Int> = musicExtraDao.getExtraInfoNum()

    val musicWithoutExtraCount : Flow<Int> = musicExtraDao.getMusicWithoutExtraCount()

    // 根据 ID 获取音乐信息（用于状态监听）
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
    suspend fun searchMusic(query: String): List<MusicInfo> = musicAllDao.searchMusic("%$query%")

    // 添加音乐标签
    suspend fun addMusicLabel(label: MusicLabel) {
        if(label.label!=LabelName.UNKNOWN) musicLabelDao.insert(label)
        else println("UNKNOWN Label!")
    }

    // 依据 LabelType获取 LabelName
    fun getLabelNamesByType(type: LabelCategory): Flow<List<LabelName>> {
        return musicLabelDao.getLabelsByType(type)
    }

    // 依据 LabelName 获取 Music
    suspend fun getMusicIdListByType(label: LabelName): List<Long>{
        return musicLabelDao.getMusicIdListByType(label)
    }

    // 获取音乐标签
    suspend fun getMusicLabels(musicId: Long): List<MusicLabel> {
        return musicLabelDao.getLabelsById(musicId)
    }

    // 获取音乐歌词
    suspend fun getMusicLyrics(musicId: Long): String? {
        return musicExtraDao.getLyricsById(musicId)
    }

    // 插入音乐额外信息
    suspend fun insertMusicExtra(musicId:Long, musicExtraInfo: DailyMusicInfo) {
        musicExtraDao.updateExtraFieldsById(
            id = musicId,
            rewards = musicExtraInfo.rewards,
            popLyric = musicExtraInfo.lyric,
            singerIntroduce = musicExtraInfo.singerIntroduce,
            backgroundIntroduce = musicExtraInfo.backgroundIntroduce,
            description = musicExtraInfo.description,
            relevantMusic = musicExtraInfo.relevantMusic
        )
    }
    // 获取音乐额外信息
    suspend fun getMusicExtraById(musicId:Long):DailyMusicInfo {
        val info = musicExtraDao.getExtraFieldsById(musicId)
        return DailyMusicInfo(
            genre = emptyList(),
            mood = emptyList(),
            scenario = emptyList(),
            language = "",
            era = "",
            rewards = info?.rewards ?: "",
            lyric = info?.popLyric ?: "",
            singerIntroduce = info?.singerIntroduce ?: "",
            backgroundIntroduce = info?.backgroundIntroduce ?: "",
            description = info?.description ?: "",
            relevantMusic = info?.relevantMusic ?: "",
            errorInfo = "None"
        )
    }

    // 从设备本地加载音乐文件（过滤掉时长少于 1 分钟的）
    // 用 StateFlow 通知外部扫描状态
    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    /**
     * 从设备加载音乐，使用 Result 封装错误
     */
    suspend fun loadMusicFromDevice(): Result<Unit> = safeCall {
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
            _isScanning.value = false
        }
    }

    private fun getLyrics(file: File): String? {
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
                        albumArtUri = albumArtUri
                    )
                )

                musicExtraList.add(
                    MusicExtra(
                        id = id,
                        lyrics = lyrics,
                        bitRate = bitRate,
                        sampleRate = sampleRate,
                        fileSize = fileSize,
                        format = mimeType,
                        isGetExtraInfo = false
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

    // 删除所有信息
    suspend fun clearAllDataBase() {
        musicDao.deleteAll()
        musicExtraDao.deleteAll()
        userInfoDao.deleteAll()
        playlistDao.deleteAll()
        playbackHistoryDao.deleteAll()
    }


    // ------------------- 播放列表相关操作 -------------------

    // 创建一个新的播放列表并返回其 ID
    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insert(Playlist(name = name))
    }

    // 删除一个播放列表
    suspend fun removePlaylist(name: String){
        playlistDao.deletePlaylist(name = name)
    }

    // 向指定播放列表中添加一首音乐
    suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String) {
        val item = PlaylistItem(
            songUrl = musicPath,
            songId = musicId,
            playlistId = playlistId,
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
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>> {
        return playlistItemDao.getMusicInfoInPlaylist(playlistId)
    }
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo> {
        return playlistItemDao.getPlaylistById(playlistId)
    }
    suspend fun getPlaylistByIdList(playlistIdList: List<Long>): List<MusicInfo> {
        return musicAllDao.getPlaylistByIdList(playlistIdList)
    }

    val labelCategoryWeight = mapOf(
        LabelCategory.GENRE to 3,
        LabelCategory.MOOD to 4,
        LabelCategory.SCENARIO to 2,
        LabelCategory.LANGUAGE to 1,
        LabelCategory.ERA to 1
    )

    private fun calcSimilarity(
        baseLabels: List<MusicLabel>,
        targetLabels: List<MusicLabel>
    ): Int {
        var score = 0
        for (base in baseLabels) {
            for (target in targetLabels) {
                if (base.label == target.label) {
                    score += labelCategoryWeight[base.type] ?: 1
                } else if (base.type == target.type) {
                    score += 0
                }
            }
        }
        return score
    }

    suspend fun getSimilarSongsByWeightedLabels(
        musicId: Long,
        limit: Int = 10
    ): List<MusicInfo> {
        val baseLabels = getMusicLabels(musicId)
        if (baseLabels.isEmpty()) return emptyList()

        val allMusic = getAllMusicInfoAsList()
        return allMusic
            .filter { it.music.id != musicId }
            .map { musicInfo ->
                val targetLabels = getMusicLabels(musicInfo.music.id)
                val similarity = calcSimilarity(baseLabels, targetLabels)
                musicInfo to similarity
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
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

    // 记录收听时长
    suspend fun recordListeningDuration(duration: Long) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = listeningDurationDao.getDurationByDate(today)
        
        if (existing == null) {
            listeningDurationDao.insert(
                ListeningDuration(
                    date = today,
                    duration = duration
                )
            )
        } else {
            listeningDurationDao.updateDuration(
                date = today,
                additionalDuration = duration,
                updateTime = System.currentTimeMillis()
            )
        }
    }
    
    // 获取最近7天的收听记录
    fun getRecentListeningDurations(): Flow<List<ListeningDuration>> {
        return listeningDurationDao.getRecentDurations(7)
    }
}

