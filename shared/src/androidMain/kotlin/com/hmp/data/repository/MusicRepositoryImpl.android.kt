package com.hmp.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hmp.data.database.ListeningDuration
import com.hmp.data.database.ListeningDurationDao
import com.hmp.data.database.Music
import com.hmp.data.database.MusicAllDao
import com.hmp.data.database.MusicDao
import com.hmp.data.database.MusicExtra
import com.hmp.data.database.MusicExtraDao
import com.hmp.data.database.MusicLabelDao
import com.hmp.data.database.PlaybackHistoryDao
import com.hmp.data.database.PlaylistDao
import com.hmp.data.database.PlaylistItemDao
import com.hmp.data.database.UserInfo
import com.hmp.data.database.UserInfoDao
import com.hmp.data.database.myenum.LabelCategory as DataLabelCategory
import com.hmp.data.database.myenum.LabelName as DataLabelName
import com.hmp.domain.enum.LabelCategory
import com.hmp.domain.enum.LabelName
import com.hmp.data.mapper.toDomain
import com.hmp.data.mapper.toEntity
import com.hmp.data.util.stringToPinyinSortKey
import com.hmp.data.network.AiApiResult
import com.hmp.data.network.MultiProviderApiAdapter
import com.hmp.data.network.dto.MusicInfoResponse
import com.hmp.domain.setting.model.ArtistCountEntry
import com.hmp.domain.setting.model.LabelCountEntry
import com.hmp.domain.setting.model.RecentPlaybackEntry
import com.hmp.domain.setting.model.TopPlayedEntry
import com.hmp.domain.setting.model.UserUsageAnalytics
import com.hmp.domain.setting.model.PlaybackHistory as PlaybackHistoryDomain
import com.hmp.domain.backup.ListeningStatsSnapshot
import com.hmp.domain.backup.MusicExtraUserSnapshot
import com.hmp.domain.backup.MusicLabelSnapshot
import com.hmp.domain.backup.MusicUserStateSnapshot
import com.hmp.domain.backup.UserInfoSnapshot
import com.hmp.domain.setting.model.AiProviderConfig
import com.hmp.domain.setting.model.DailyMusicInfo
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicRepository
import kotlinx.serialization.json.Json
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
import com.hmp.domain.setting.model.ListeningDuration as ListeningDurationDomain
import com.hmp.domain.music.MusicLabel as MusicLabelDomain

class MusicRepositoryImpl(
    private val musicDao: MusicDao,
    private val musicExtraDao: MusicExtraDao,
    private val userInfoDao: UserInfoDao,
    private val musicAllDao: MusicAllDao,
    private val musicLabelDao: MusicLabelDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val listeningDurationDao: ListeningDurationDao,
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao,
    private val multiProviderApiAdapter: MultiProviderApiAdapter,
    private val json: Json,
    private val context: Context
) : MusicRepository {

    private val musicFields = listOf("id", "title", "artist", "album", "duration")
    private val extraFields = listOf("bitRate", "sampleRate", "fileSize", "format", "language", "date")
    private val userInfoFields = listOf("liked", "disLiked", "lastPlayed", "playCount", "skippedCount", "userRating")

    override suspend fun getAllMusicInfoAsList(orderBy: String, orderType: String): List<MusicInfo> {
        val safeOrderType = if (orderType.uppercase() == "DESC") "DESC" else "ASC"
        // Text-based sorts (title/artist/album) always use in-memory pinyin-aware sorting
        if (orderBy == "title" || orderBy == "artist" || orderBy == "album") {
            val list = musicAllDao.getAllMusicInfoAsListById().map { it.toDomain() }
            val keyFn: (MusicInfo) -> String = when (orderBy) {
                "title" -> { info -> stringToPinyinSortKey(info.music.title) }
                "artist" -> { info -> stringToPinyinSortKey(info.music.artist) }
                else -> { info -> stringToPinyinSortKey(info.music.album) }
            }
            val rawFn: (MusicInfo) -> String = when (orderBy) {
                "title" -> { info -> info.music.title }
                "artist" -> { info -> info.music.artist }
                else -> { info -> info.music.album }
            }
            return if (safeOrderType == "DESC") {
                list.sortedWith(compareByDescending(keyFn).thenByDescending(rawFn))
            } else {
                list.sortedWith(compareBy(keyFn).thenBy(rawFn))
            }
        }
        // Numeric/date sorts use SQL-level sorting
        val tablePrefix = when {
            musicFields.contains(orderBy) -> "music"
            extraFields.contains(orderBy) -> "musicExtra"
            userInfoFields.contains(orderBy) -> "userInfo"
            else -> "music"
        }
        val baseList = when {
            tablePrefix == "music" && orderBy == "id" -> musicAllDao.getAllMusicInfoAsListById()
            tablePrefix == "music" && orderBy == "duration" -> musicAllDao.getAllMusicInfoAsListByDuration()
            else -> musicAllDao.getAllMusicInfoAsListById()
        }
        val mappedList = baseList.map { it.toDomain() }
        return if (safeOrderType == "DESC") {
            when(orderBy) {
                "duration" -> mappedList.sortedByDescending { it.music.duration }
                "playCount" -> mappedList.sortedByDescending { it.userInfo?.playCount ?: 0 }
                else -> mappedList.sortedByDescending { it.music.id }
            }
        } else {
            mappedList
        }
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

    override suspend fun removeFromLibrary(ids: List<Long>) {
        if (ids.isEmpty()) return
        musicDao.markDeletedByIds(ids)
        musicExtraDao.markDeletedByIds(ids)
        userInfoDao.markDeletedByIds(ids)
    }

    override suspend fun restoreToLibrary(ids: List<Long>) {
        if (ids.isEmpty()) return
        musicDao.markActiveByIds(ids)
        musicExtraDao.markActiveByIds(ids)
        userInfoDao.markActiveByIds(ids)
    }

    override suspend fun getDeletedMusicIdsGroupedByFolder(): List<Pair<String, List<Long>>> {
        val list = musicDao.getDeletedMusicIdAndPath()
        return list
            .groupBy { (_, path) ->
                try {
                    File(path).parent ?: "Unknown"
                } catch (e: Exception) {
                    "Unknown"
                }
            }
            .map { (path, entries) -> path to entries.map { it.id } }
            .sortedByDescending { it.second.size }
    }

    override suspend fun getMusicListByArtist(artistName: String): List<MusicInfo> {
        val list = musicAllDao.getMusicInfoByArtist(artistName).map { it.toDomain() }
        return list.sortedWith(
            compareBy<MusicInfo> { stringToPinyinSortKey(it.music.title) }.thenBy { it.music.title }
        )
    }

    override suspend fun getMusicListByAlbum(albumName: String): List<MusicInfo> {
        val list = musicAllDao.getMusicInfoByAlbum(albumName).map { it.toDomain() }
        return list.sortedWith(
            compareBy<MusicInfo> { stringToPinyinSortKey(it.music.title) }.thenBy { it.music.title }
        )
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
                    val response = json.decodeFromString<MusicInfoResponse>(result.data)
                    val info = DailyMusicInfo(
                        genre = response.genre,
                        mood = response.mood,
                        scenario = response.scenario,
                        language = response.language,
                        era = response.era,
                        rewards = response.rewards,
                        lyric = response.lyric,
                        singerIntroduce = response.singerIntroduce,
                        backgroundIntroduce = response.backgroundIntroduce,
                        description = response.description,
                        relevantMusic = response.relevantMusic,
                        errorInfo = response.errorInfo
                    )
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
                你是一位专业的音乐编辑，精通各类音乐风格、流派发展历史和艺术家背景。

                请分析以下歌曲信息，如果需要可以通过网络搜索获取更准确的信息。

                请严格按照以下JSON格式回复，不要包含任何解释、注释或其他额外文本：
                {
                "genre": ["ROCK", "POP", "JAZZ", "CLASSICAL", "HIPHOP", "ELECTRONIC", "FOLK", "RNB", "METAL", "COUNTRY", "BLUES", "REGGAE", "PUNK", "FUNK", "SOUL", "INDIE"],
                "mood": ["HAPPY", "SAD", "ENERGETIC", "CALM", "ROMANTIC", "ANGRY", "LONELY", "UPLIFTING", "MYSTERIOUS", "DARK", "MELANCHOLY", "HOPEFUL"],
                "scenario": ["WORKOUT", "SLEEP", "PARTY", "DRIVING", "STUDY", "RELAX", "DINNER", "MEDITATION", "FOCUS", "TRAVEL", "MORNING", "NIGHT"],
                "language":"ENGLISH/CHINESE/JAPANESE/KOREAN/OTHERS",
                "era":"SIXTIES/SEVENTIES/EIGHTIES/NINETIES/TWO_THOUSANDS/TWENTY_TENS/TWENTY_TWENTIES",
                "rewards": "歌手或歌曲获得的重要奖项，如格莱美、金曲奖等，若无则返回-暂无",
                "lyric": "该歌曲最广为人知的一到两句歌词，若无则返回-暂无",
                "singerIntroduce": "歌手的背景、主要成就、音乐风格和代表作品介绍",
                "backgroundIntroduce": "歌曲的创作背景、灵感来源、发布时的反响或背后的故事",
                "description": "歌曲表达的主题、情感、核心内容或想要传达的信息",
                "relevantMusic": "与该歌曲风格、流派或情感相似的其他知名歌曲",
                "errorInfo": "None"
                }

                要求：
                1. 只返回上述JSON格式，不要添加任何markdown标记或解释
                2. genre、mood、scenario为多选，用逗号分隔
                3. language和era为单选，直接输出选项值
                4. 如果无法确定某字段，回复"UNKNOWN"
                5. 歌词必须是该歌曲的真实热门歌词，不要编造
                6. 相似歌曲推荐必须是真正与该歌曲风格相似的知名歌曲
                7. 优先通过网络搜索确认信息的准确性

                歌曲信息：$artist 演唱的《$title》
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
            println("MusicRepository: Music scan failed: ${e.message}")
            kotlin.Result.failure(e)
        } finally {
            _isScanning.value = false
        }
    }

    override suspend fun syncMusicFromDeviceIncremental(): kotlin.Result<Unit> = withContext(Dispatchers.IO) {
        _isScanning.value = true
        try {
            val (scannedMusic, scannedExtra, scannedUserInfo) = performMusicScan()

            val existingIds = musicDao.getAllActiveIds().toSet()
            val scannedIds = scannedMusic.map { it.id }.toSet()

            val newIds = scannedIds - existingIds
            val commonIds = scannedIds.intersect(existingIds)
            val missingIds = existingIds - scannedIds

            if (newIds.isNotEmpty()) {
                val newMusic = scannedMusic.filter { it.id in newIds }
                val newExtra = scannedExtra.filter { it.id in newIds }
                val newUserInfo = scannedUserInfo.filter { it.id in newIds }

                newMusic.chunked(BATCH_SIZE).forEach { batch -> musicDao.insertAll(batch) }
                newExtra.chunked(BATCH_SIZE).forEach { batch -> musicExtraDao.insertAll(batch) }
                newUserInfo.chunked(BATCH_SIZE).forEach { batch -> userInfoDao.insertAll(batch) }
            }

            if (commonIds.isNotEmpty()) {
                val commonMusicById = scannedMusic.filter { it.id in commonIds }.associateBy { it.id }
                val commonExtraById = scannedExtra.filter { it.id in commonIds }.associateBy { it.id }

                commonIds.chunked(BATCH_SIZE).forEach { idBatch ->
                    idBatch.forEach { id ->
                        val scannedMusicItem = commonMusicById[id]
                        if (scannedMusicItem != null) {
                            musicDao.insert(
                                scannedMusicItem.copy(isDeleted = false)
                            )
                        }

                        val scannedExtraItem = commonExtraById[id]
                        if (scannedExtraItem != null) {
                            val existingExtra = musicExtraDao.getExtraFieldsById(id)
                            val mergedExtra = existingExtra?.copy(
                                lyrics = scannedExtraItem.lyrics ?: existingExtra.lyrics,
                                bitRate = scannedExtraItem.bitRate ?: existingExtra.bitRate,
                                sampleRate = scannedExtraItem.sampleRate ?: existingExtra.sampleRate,
                                fileSize = scannedExtraItem.fileSize ?: existingExtra.fileSize,
                                format = scannedExtraItem.format ?: existingExtra.format,
                                isDeleted = false
                            )
                                ?: scannedExtraItem.copy(isDeleted = false)
                            musicExtraDao.insert(mergedExtra)
                        }

                        val existingUserInfo = userInfoDao.getUserInfoById(id)
                        if (existingUserInfo == null) {
                            userInfoDao.insert(UserInfo(id = id))
                        } else if (existingUserInfo.isDeleted) {
                            userInfoDao.insert(existingUserInfo.copy(isDeleted = false))
                        }
                    }
                }
            }

            if (missingIds.isNotEmpty()) {
                musicDao.markDeletedByIds(missingIds.toList())
                musicExtraDao.markDeletedByIds(missingIds.toList())
                userInfoDao.markDeletedByIds(missingIds.toList())
            }

            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            println("MusicRepository: Incremental music scan failed: ${e.message}")
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
                        println("MusicRepository: Error processing music item: ${e.message}")
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
        val existingDates = musicExtraDao.getAllIdAndDate().associate { it.id to it.date }
        val extrasWithDate = musicExtraList.map { e ->
            e.copy(date = existingDates[e.id] ?: System.currentTimeMillis())
        }
        Triple(musicList, extrasWithDate, userInfoList)
    }

    private val labelCategoryWeight = mapOf(
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

    override suspend fun insertPlayback(history: PlaybackHistoryDomain): Long {
        return playbackHistoryDao.insert(history.toEntity())
    }

    override suspend fun updatePlaybackRecord(id: Long, duration: Long, isCompleted: Boolean) {
        playbackHistoryDao.updatePlaybackRecord(id, duration, isCompleted)
    }

    override suspend fun recordListeningDuration(duration: Long) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = listeningDurationDao.getDurationByDate(today)

        if (existing == null) {
            listeningDurationDao.insert(
                ListeningDuration(
                    date = today,
                    duration = duration,
                    updatedAt = System.currentTimeMillis()
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

    override fun getRecentListeningDurations(limit: Int): Flow<List<ListeningDurationDomain>> {
        return listeningDurationDao.getRecentDurations(limit).map { list -> list.map { it.toDomain() } }
    }

    override fun getPlaybackHistory(musicId: Long, limit: Int): Flow<List<PlaybackHistoryDomain>> {
        return playbackHistoryDao.getHistoryForMusic(musicId, limit).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getRecentPlaybackHistoryGlobal(limit: Int): List<PlaybackHistoryDomain> =
        withContext(Dispatchers.IO) {
            playbackHistoryDao.getRecentHistory(limit).map { it.toDomain() }
        }

    override suspend fun getUserUsageAnalytics(): UserUsageAnalytics = withContext(Dispatchers.IO) {
        val userInfos = userInfoDao.getAllUserInfos().filter { !it.isDeleted }
        val allHistory = playbackHistoryDao.getAllHistory()
        val recentHistory = playbackHistoryDao.getRecentHistory(10)
        val allDurations = listeningDurationDao.getAllDurations()
        val allLabels = musicLabelDao.getAllLabels()
        val allMusic = getAllMusicInfoAsList("playCount", "DESC")
        val playlists = playlistDao.getAllPlaylists()
        val playlistItems = playlistItemDao.getAllPlaylistItems()

        val totalPlayCount = userInfos.sumOf { (it.playCount ?: 0).toLong() }.toInt()
        val totalSkipCount = userInfos.sumOf { (it.skippedCount ?: 0).toLong() }.toInt()
        val totalPlayPlusSkip = totalPlayCount + totalSkipCount
        val skipRate = if (totalPlayPlusSkip > 0) totalSkipCount.toFloat() / totalPlayPlusSkip else 0f
        val likedCount = userInfos.count { it.liked }

        val completedCount = allHistory.count { it.isCompleted }
        val totalSessions = allHistory.size
        val completionRate = if (totalSessions > 0) completedCount.toFloat() / totalSessions else 0f
        val totalListeningMs = allHistory.sumOf { it.playDuration }
        val totalListeningMinutes = totalListeningMs / 60_000
        val averageSessionMinutes = if (totalSessions > 0) totalListeningMs / 60_000.0 / totalSessions else 0.0

        val playSourceBreakdown = allHistory
            .mapNotNull { it.source }
            .groupingBy { it }
            .eachCount()

        val weekMs = 7 * 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        val thisWeekStart = now - weekMs
        val lastWeekStart = now - 2 * weekMs
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val thisWeekMinutes = allDurations
            .filter { d -> try { val t = dateFormat.parse(d.date)?.time ?: 0L; t in thisWeekStart..now } catch (_: Exception) { false } }
            .sumOf { it.duration } / 60_000
        val lastWeekMinutes = allDurations
            .filter { d -> try { val t = dateFormat.parse(d.date)?.time ?: 0L; t in lastWeekStart until thisWeekStart } catch (_: Exception) { false } }
            .sumOf { it.duration } / 60_000

        val userInfoMap = userInfos.associateBy { it.id }
        val topByPlay = userInfos
            .sortedByDescending { it.playCount ?: 0 }
            .take(5)
            .mapNotNull { ui -> ui.id }
        val topMusicIds = topByPlay
        val topMusicInfos = if (topMusicIds.isEmpty()) emptyList() else musicAllDao.getPlaylistByIdList(topMusicIds)
        val topMusicInfoMap = topMusicInfos.associateBy { it.music.id }
        val topPlayedSongs = topMusicIds.mapNotNull { id ->
            val info = topMusicInfoMap[id] ?: return@mapNotNull null
            TopPlayedEntry(
                musicId = id,
                title = info.music.title,
                artist = info.music.artist,
                playCount = userInfoMap[id]?.playCount ?: 0
            )
        }

        val recentMusicIds = recentHistory.map { it.musicId }.distinct()
        val recentMusicInfos = if (recentMusicIds.isEmpty()) emptyList() else musicAllDao.getPlaylistByIdList(recentMusicIds)
        val recentMusicInfoMap = recentMusicInfos.associateBy { it.music.id }
        val recentPlaybackWithTitle = recentHistory.map { h ->
            val info = recentMusicInfoMap[h.musicId]
            RecentPlaybackEntry(
                musicId = h.musicId,
                title = info?.music?.title ?: "",
                artist = info?.music?.artist ?: "",
                playedAt = h.playedAt,
                playDuration = h.playDuration,
                isCompleted = h.isCompleted,
                source = h.source
            )
        }

        val playCountByMusicId = userInfos.associate { it.id to (it.playCount ?: 0).toLong() }
        val labelToCount = mutableMapOf<Pair<DataLabelCategory, DataLabelName>, Long>()
        allLabels.forEach { ml ->
            val key = ml.type to ml.label
            val add = playCountByMusicId[ml.musicId] ?: 0L
            labelToCount[key] = (labelToCount[key] ?: 0L) + add
        }
        val topGenres = labelToCount
            .filter { it.key.first == DataLabelCategory.GENRE }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { LabelCountEntry(labelDisplayName = it.first.second.name, count = it.second.toInt()) }
        val topMoods = labelToCount
            .filter { it.key.first == DataLabelCategory.MOOD }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { LabelCountEntry(labelDisplayName = it.first.second.name, count = it.second.toInt()) }
        val topScenarios = labelToCount
            .filter { it.key.first == DataLabelCategory.SCENARIO }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { LabelCountEntry(labelDisplayName = it.first.second.name, count = it.second.toInt()) }

        val artistToCount = allMusic
            .filter { it.userInfo != null }
            .groupBy { it.music.artist }
            .mapValues { (_, list) -> list.sumOf { (it.userInfo?.playCount ?: 0).toLong() } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        val topArtists = artistToCount.map { ArtistCountEntry(artistName = it.first, playCount = it.second.toInt()) }

        val customPlaylistCount = playlists.size

        val songIdToPlaylistCount = playlistItems.groupingBy { it.songId }.eachCount()
        val topSongIdsInPlaylists = songIdToPlaylistCount.toList().sortedByDescending { it.second }.take(5).map { it.first }
        val topSongsInPlaylistsInfos = if (topSongIdsInPlaylists.isEmpty()) emptyList() else musicAllDao.getPlaylistByIdList(topSongIdsInPlaylists)
        val topSongsInPlaylistsMap = topSongsInPlaylistsInfos.associateBy { it.music.id }
        val topSongsInPlaylists = topSongIdsInPlaylists.mapNotNull { id ->
            val info = topSongsInPlaylistsMap[id] ?: return@mapNotNull null
            TopPlayedEntry(
                musicId = id,
                title = info.music.title,
                artist = info.music.artist,
                playCount = songIdToPlaylistCount[id] ?: 0
            )
        }

        UserUsageAnalytics(
            totalPlayCount = totalPlayCount,
            totalSkipCount = totalSkipCount,
            likedCount = likedCount,
            totalListeningMinutes = totalListeningMinutes,
            averageSessionMinutes = averageSessionMinutes,
            completionRate = completionRate,
            skipRate = skipRate,
            thisWeekMinutes = thisWeekMinutes,
            lastWeekMinutes = lastWeekMinutes,
            topPlayedSongs = topPlayedSongs,
            recentPlaybackWithTitle = recentPlaybackWithTitle,
            playSourceBreakdown = playSourceBreakdown,
            topGenres = topGenres,
            topMoods = topMoods,
            topScenarios = topScenarios,
            topArtists = topArtists,
            customPlaylistCount = customPlaylistCount,
            topSongsInPlaylists = topSongsInPlaylists
        )
    }

    override suspend fun incrementPlayCount(musicId: Long) = withContext(Dispatchers.IO) {
        userInfoDao.incrementPlayCount(musicId)
    }

    override suspend fun incrementSkippedCount(musicId: Long) = withContext(Dispatchers.IO) {
        userInfoDao.incrementSkippedCount(musicId)
    }

    override suspend fun updateLastPlayed(musicId: Long, timestamp: Long) = withContext(Dispatchers.IO) {
        userInfoDao.updateLastPlayed(musicId, timestamp)
    }

    override suspend fun exportMusicUserStateSnapshot(): MusicUserStateSnapshot {
        val userInfos = userInfoDao.getAllUserInfos().map {
            UserInfoSnapshot(
                id = it.id,
                liked = it.liked,
                disLiked = it.disLiked,
                lastPlayed = it.lastPlayed,
                playCount = it.playCount,
                skippedCount = it.skippedCount,
                userRating = it.userRating,
                inCustomPlaylistCount = it.inCustomPlaylistCount
            )
        }

        val extras = musicExtraDao.getAllExtras().map {
            MusicExtraUserSnapshot(
                id = it.id,
                isGetExtraInfo = it.isGetExtraInfo,
                rewards = it.rewards,
                popLyric = it.popLyric,
                singerIntroduce = it.singerIntroduce,
                backgroundIntroduce = it.backgroundIntroduce,
                description = it.description,
                relevantMusic = it.relevantMusic
            )
        }

        val labels = musicLabelDao.getAllLabels().map {
            MusicLabelSnapshot(
                musicId = it.musicId,
                label = LabelName.valueOf(it.label.name),
                category = LabelCategory.valueOf(it.type.name)
            )
        }

        return MusicUserStateSnapshot(
            userInfos = userInfos,
            extras = extras,
            labels = labels
        )
    }

    override suspend fun restoreMusicUserState(snapshot: MusicUserStateSnapshot) {
        val userInfos = snapshot.userInfos.map {
            UserInfo(
                id = it.id,
                liked = it.liked,
                disLiked = it.disLiked,
                lastPlayed = it.lastPlayed,
                playCount = it.playCount,
                skippedCount = it.skippedCount,
                userRating = it.userRating,
                inCustomPlaylistCount = it.inCustomPlaylistCount,
                isDeleted = false
            )
        }
        userInfoDao.insertAll(userInfos)

        val existingExtrasMap = musicExtraDao.getAllExtras().associateBy { it.id }

        val mergedExtras = snapshot.extras.map { snapshotExtra ->
            val existing = existingExtrasMap[snapshotExtra.id]
            existing?.copy(
                isGetExtraInfo = snapshotExtra.isGetExtraInfo,
                rewards = snapshotExtra.rewards,
                popLyric = snapshotExtra.popLyric,
                singerIntroduce = snapshotExtra.singerIntroduce,
                backgroundIntroduce = snapshotExtra.backgroundIntroduce,
                description = snapshotExtra.description,
                relevantMusic = snapshotExtra.relevantMusic
            )
                ?: MusicExtra(
                    id = snapshotExtra.id,
                    isGetExtraInfo = snapshotExtra.isGetExtraInfo,
                    rewards = snapshotExtra.rewards,
                    popLyric = snapshotExtra.popLyric,
                    singerIntroduce = snapshotExtra.singerIntroduce,
                    backgroundIntroduce = snapshotExtra.backgroundIntroduce,
                    description = snapshotExtra.description,
                    relevantMusic = snapshotExtra.relevantMusic,
                    isDeleted = false
                )
        }
        musicExtraDao.insertAll(mergedExtras)

        val musicLabels = snapshot.labels.map {
            com.hmp.data.database.MusicLabel(
                musicId = it.musicId,
                label = DataLabelName.valueOf(it.label.name),
                type = DataLabelCategory.valueOf(it.category.name)
            )
        }
        musicLabelDao.insertAll(musicLabels)
    }

    override suspend fun exportListeningStatsSnapshot(): ListeningStatsSnapshot {
        val durations = listeningDurationDao.getAllDurations().map {
            ListeningDurationDomain(
                date = it.date,
                duration = it.duration
            )
        }

        val history = playbackHistoryDao.getAllHistory().map {
            PlaybackHistoryDomain(
                musicId = it.musicId,
                playedAt = it.playedAt,
                playDuration = it.playDuration,
                isCompleted = it.isCompleted,
                source = it.source
            )
        }

        return ListeningStatsSnapshot(
            listeningDurations = durations,
            playbackHistories = history
        )
    }

    override suspend fun restoreListeningStats(snapshot: ListeningStatsSnapshot) {
        val durations = snapshot.listeningDurations.map {
            ListeningDuration(
                date = it.date,
                duration = it.duration,
                updatedAt = System.currentTimeMillis()
            )
        }
        listeningDurationDao.insertAll(durations)

        val history = snapshot.playbackHistories.map {
            com.hmp.data.database.PlaybackHistory(
                musicId = it.musicId,
                playedAt = it.playedAt,
                playDuration = it.playDuration,
                isCompleted = it.isCompleted,
                source = it.source
            )
        }
        playbackHistoryDao.insertAll(history)
    }
}
