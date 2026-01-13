package com.example.hearablemusicplayer.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.hearablemusicplayer.data.database.ListeningDuration
import com.example.hearablemusicplayer.data.database.ListeningDurationDao
import com.example.hearablemusicplayer.data.database.Music
import com.example.hearablemusicplayer.data.database.MusicAllDao
import com.example.hearablemusicplayer.data.database.MusicDao
import com.example.hearablemusicplayer.data.database.MusicExtra
import com.example.hearablemusicplayer.data.database.MusicExtraDao
import com.example.hearablemusicplayer.data.database.MusicLabelDao
import com.example.hearablemusicplayer.data.database.PlaybackHistoryDao
import com.example.hearablemusicplayer.data.database.UserInfo
import com.example.hearablemusicplayer.data.database.UserInfoDao
import com.example.hearablemusicplayer.data.database.myenum.LabelCategory as DataLabelCategory
import com.example.hearablemusicplayer.data.database.myenum.LabelName as DataLabelName
import com.example.hearablemusicplayer.domain.model.enum.LabelCategory
import com.example.hearablemusicplayer.domain.model.enum.LabelName
import com.example.hearablemusicplayer.data.mapper.toDomain
import com.example.hearablemusicplayer.data.mapper.toEntity
import com.example.hearablemusicplayer.data.network.AiApiResult
import com.example.hearablemusicplayer.domain.model.PlaybackHistory as PlaybackHistoryDomain
import com.example.hearablemusicplayer.data.network.DeepSeekAPIWrapper
import com.example.hearablemusicplayer.data.network.MultiProviderApiAdapter
import com.example.hearablemusicplayer.domain.model.AiProviderConfig
import com.example.hearablemusicplayer.domain.model.DailyMusicInfo
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.repository.MusicRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import com.example.hearablemusicplayer.domain.model.ListeningDuration as ListeningDurationDomain
import com.example.hearablemusicplayer.domain.model.MusicLabel as MusicLabelDomain

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val musicDao: MusicDao,
    private val musicExtraDao: MusicExtraDao,
    private val userInfoDao: UserInfoDao,
    private val musicAllDao: MusicAllDao,
    private val musicLabelDao: MusicLabelDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val listeningDurationDao: ListeningDurationDao,
    private val deepSeekAPIWrapper: DeepSeekAPIWrapper,
    private val multiProviderApiAdapter: MultiProviderApiAdapter,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) : MusicRepository {

    // ------------------- 音乐相关操作 -------------------

    private val musicFields = listOf("id", "title", "artist", "album", "duration")
    private val extraFields = listOf("bitRate", "sampleRate", "fileSize", "format", "language", "year")
    private val userInfoFields = listOf("liked", "disLiked", "lastPlayed", "playCount", "skippedCount", "userRating")

    override suspend fun getAllMusicInfoAsList(orderBy: String, orderType: String): List<MusicInfo> {
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
        return musicAllDao.getAllMusicInfoAsList(query).map { it.toDomain() }
    }

    override suspend fun getRandomMusicInfoWithMissingExtra(): MusicInfo? {
        return musicAllDao.getRandomMusicInfoWithMissingExtra()?.toDomain()
    }

    override suspend fun getRandomMusicInfoWithExtra(): MusicInfo? {
        return musicAllDao.getRandomMusicInfoWithExtra()?.toDomain()
    }

    override fun getMusicCount(): Flow<Int> = musicDao.getMusicCount()

    override fun getMusicWithExtraCount(): Flow<Int> = musicExtraDao.getExtraInfoNum()
    
    override fun getMusicWithMissingExtraCount(): Flow<Int> = musicAllDao.getMusicWithMissingExtraCount()

    override fun getMusicInfoById(musicId: Long): Flow<MusicInfo?> = musicAllDao.getMusicInfoById(musicId).map { it?.toDomain() }

    override suspend fun updateLikedStatus(id: Long, liked: Boolean) {
        userInfoDao.updateLikedStatus(id, liked)
    }

    override suspend fun getLikedStatus(id: Long): Boolean {
        return userInfoDao.getLikedStatus(id)
    }
    
    override suspend fun getMusicListByArtist(artistName: String): List<MusicInfo> {
        val queryString = """
            SELECT * FROM music 
            LEFT JOIN musicExtra ON music.id = musicExtra.id
            LEFT JOIN userInfo ON music.id = userInfo.id
            WHERE music.artist = ?
            ORDER BY music.title ASC
        """.trimIndent()
        
        val query = SimpleSQLiteQuery(queryString, arrayOf(artistName))
        return musicAllDao.getAllMusicInfoAsList(query).map { it.toDomain() }
    }

    override suspend fun searchMusic(query: String): List<MusicInfo> = musicAllDao.searchMusic("%$query%").map { it.toDomain() }

    override suspend fun addMusicLabel(label: MusicLabelDomain) {
        if(label.label != LabelName.UNKNOWN) musicLabelDao.insert(label.toEntity())
        else println("UNKNOWN Label!")
    }

    override fun getLabelNamesByType(type: LabelCategory): Flow<List<LabelName>> {
        val dataType = try {
            DataLabelCategory.valueOf(type.name)
        } catch (e: Exception) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        return musicLabelDao.getLabelsByType(dataType).map { list ->
            list.mapNotNull { dataLabel ->
                try {
                    LabelName.valueOf(dataLabel.name)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override suspend fun getMusicIdListByType(label: LabelName): List<Long>{
        val dataLabel = try {
            DataLabelName.valueOf(label.name)
        } catch (e: Exception) {
            return emptyList()
        }
        return musicLabelDao.getMusicIdListByType(dataLabel)
    }

    override suspend fun getMusicLabels(musicId: Long): List<MusicLabelDomain> {
        return musicLabelDao.getLabelsById(musicId).map { it.toDomain() }
    }

    override suspend fun getMusicLyrics(musicId: Long): String? {
        return musicExtraDao.getLyricsById(musicId)
    }

    override suspend fun insertMusicExtra(musicId: Long, musicExtraInfo: DailyMusicInfo) {
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

    override suspend fun getMusicExtraById(musicId: Long): DailyMusicInfo {
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

    override suspend fun fetchMusicExtraInfoWithProvider(
        providerConfig: AiProviderConfig,
        title: String,
        artist: String
    ): kotlin.Result<DailyMusicInfo> {
        val prompt = buildMusicInfoPrompt(title, artist)
        
        return when (val result = multiProviderApiAdapter.callChatApi(providerConfig, prompt)) {
            is AiApiResult.Success -> {
                try {
                    val info = gson.fromJson(result.data, DailyMusicInfo::class.java)
                    kotlin.Result.success(info)
                } catch (e: Exception) {
                    kotlin.Result.failure(e)
                }
            }
            is AiApiResult.Error -> {
                kotlin.Result.failure(Exception(result.error.toDisplayMessage()))
            }
        }
    }
    
    override suspend fun validateProviderApiKey(providerConfig: AiProviderConfig): kotlin.Result<Boolean> {
        return when (val result = multiProviderApiAdapter.testConnection(providerConfig)) {
            is AiApiResult.Success -> kotlin.Result.success(true)
            is AiApiResult.Error -> kotlin.Result.failure(Exception(result.error.toDisplayMessage()))
        }
    }
    
    private fun buildMusicInfoPrompt(title: String, artist: String): String {
        return """
            请根据由 $artist 演唱的歌曲《$title》，用中文以下面的JSON格式依据提示回复相关信息（不要添加任何其他内容）：
            {
              "genre": [
                "ROCK", "POP", "JAZZ", "CLASSICAL", "HIPHOP", "ELECTRONIC", "FOLK", "RNB", "METAL", "COUNTRY", "BLUES", "REGGAE", "PUNK", "FUNK", "SOUL", "INDIE"
              ],
              "mood": [
                "HAPPY", "SAD", "ENERGETIC", "CALM", "ROMANTIC", "ANGRY", "LONELY", "UPLIFTING", "MYSTERIOUS", "DARK", "MELANCHOLY", "HOPEFUL"
              ],
              "scenario": [
                "WORKOUT", "SLEEP", "PARTY", "DRIVING", "STUDY", "RELAX", "DINNER", "MEDITATION", "FOCUS", "TRAVEL", "MORNING", "NIGHT"
              ],
              "language":"ENGLISH/CHINESE/JAPANESE/KOREAN/OTHERS(单选)", 
              "era":"SIXTIES/SEVENTIES/EIGHTIES/NINETIES/TWO_THOUSANDS/TWENTY_TENS/TWENTY_TWENTIES(单选)",
              "rewards": "歌曲成就(若无返回-暂无)",
              "lyric": "热门歌词(两句，若无返回-暂无)",
              "singerIntroduce": "歌手介绍(100字左右)",
              "backgroundIntroduce": "创作背景(出处、创作者采访等信息，100字左右)",
              "description": "歌曲主题(主题，100字左右)",
              "relevantMusic": "类似音乐(一到两首，若无返回-暂无)",
              "errorInfo": "None"
            }
        """.trimIndent()
    }

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: Flow<Boolean> = _isScanning.asStateFlow()
    
    companion object {
        private const val BATCH_SIZE = 50 
        private const val MIN_DURATION_MS = 60000L 
    }
    
    override suspend fun loadMusicFromDevice(): kotlin.Result<Unit> = withContext(Dispatchers.IO) {
        _isScanning.value = true
        try {
            val (musicList, extraList, userInfoList) = performMusicScan()
            
            musicDao.deleteAll()
            musicExtraDao.deleteAll()
            userInfoDao.deleteAll()
            
            musicList.chunked(BATCH_SIZE).forEach { batch ->
                musicDao.insertAll(batch)
            }
            
            extraList.chunked(BATCH_SIZE).forEach { batch ->
                musicExtraDao.insertAll(batch)
            }
            
            userInfoList.chunked(BATCH_SIZE).forEach { batch ->
                userInfoDao.insertAll(batch)
            }
            
            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Music scan failed", e)
            kotlin.Result.failure(e)
        } finally {
            _isScanning.value = false
        }
    }

    private fun getLyrics(file: File): String? {
        return try {
            if (!file.exists() || !file.canRead()) {
                return null
            }
            
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag
            tag?.getFirst(FieldKey.LYRICS)
                ?: tag?.getFirst("UNSYNCEDLYRICS")
                ?: tag?.getFirst("USLT")
                ?: tag?.getFirst("LYRICS:SYNCED")
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun performMusicScan(): Triple<List<Music>, List<MusicExtra>, List<UserInfo>> = withContext(Dispatchers.IO) {
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
        val selectionArgs = arrayOf(MIN_DURATION_MS.toString())
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        val retriever = MediaMetadataRetriever()

        try {
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

                        val lyrics = File(path).let { file ->
                            if (file.exists() && file.canRead()) getLyrics(file) else null
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
                    } catch (e: Exception) {
                        Log.e("MusicRepository", "Error processing music item", e)
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
        
        Triple(musicList, musicExtraList, userInfoList)
    }

    val labelCategoryWeight = mapOf(
        LabelCategory.GENRE to 3,
        LabelCategory.MOOD to 4,
        LabelCategory.SCENARIO to 2,
        LabelCategory.LANGUAGE to 1,
        LabelCategory.ERA to 1
    )

    private fun calcSimilarity(
        baseLabels: List<MusicLabelDomain>,
        targetLabels: List<MusicLabelDomain>
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

    override suspend fun getSimilarSongsByWeightedLabels(
        musicId: Long,
        limit: Int
    ): List<MusicInfo> {
        val baseLabels = getMusicLabels(musicId)
        if (baseLabels.isEmpty()) return emptyList()

        val allMusic = getAllMusicInfoAsList("id", "ASC")
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

    // 插入一条播放记录
    override suspend fun insertPlayback(history: PlaybackHistoryDomain) {
        playbackHistoryDao.insert(history.toEntity())
    }
    
    // 记录收听时长
    override suspend fun recordListeningDuration(duration: Long) {
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
    
    override fun getRecentListeningDurations(): Flow<List<ListeningDurationDomain>> {
        return listeningDurationDao.getRecentDurations(7).map { list -> list.map { it.toDomain() } }
    }
}
