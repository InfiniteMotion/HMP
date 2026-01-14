package com.example.hearablemusicplayer.domain.repository

import com.example.hearablemusicplayer.domain.model.*
import com.example.hearablemusicplayer.domain.model.enum.LabelCategory
import com.example.hearablemusicplayer.domain.model.enum.LabelName
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    // Music Query
    suspend fun getAllMusicInfoAsList(orderBy: String, orderType: String): List<MusicInfo>
    fun getMusicCount(): Flow<Int>
    fun getMusicWithExtraCount(): Flow<Int>
    fun getMusicWithMissingExtraCount(): Flow<Int>
    fun getMusicInfoById(musicId: Long): Flow<MusicInfo?>
    suspend fun getMusicListByArtist(artistName: String): List<MusicInfo>
    suspend fun searchMusic(query: String): List<MusicInfo>
    
    // Music Random
    suspend fun getRandomMusicInfoWithMissingExtra(): MusicInfo?
    suspend fun getRandomMusicInfoWithExtra(): MusicInfo?
    
    // Music Action (Like/Dislike)
    suspend fun updateLikedStatus(id: Long, liked: Boolean)
    suspend fun getLikedStatus(id: Long): Boolean
    
    // Labels
    suspend fun addMusicLabel(label: MusicLabel)
    fun getLabelNamesByType(type: LabelCategory): Flow<List<LabelName>>
    suspend fun getMusicIdListByType(label: LabelName): List<Long>
    suspend fun getMusicLabels(musicId: Long): List<MusicLabel>
    
    // 相似度推荐 (Similarity)
    suspend fun getSimilarSongsByWeightedLabels(musicId: Long, limit: Int = 10): List<MusicInfo>
    
    // 收听时长统计
    fun getRecentListeningDurations(limit: Int = 7): Flow<List<ListeningDuration>>
    
    // 额外信息 / AI (Extra Info / AI)
    suspend fun getMusicLyrics(musicId: Long): String?
    suspend fun insertMusicExtra(musicId: Long, musicExtraInfo: DailyMusicInfo)
    suspend fun getMusicExtraById(musicId: Long): DailyMusicInfo
    
    // Device Scan
    suspend fun loadMusicFromDevice(): kotlin.Result<Unit>
    val isScanning: Flow<Boolean>
    
    // AI / Extra Fetching
    suspend fun fetchMusicExtraInfoWithProvider(
        providerConfig: AiProviderConfig,
        title: String,
        artist: String
    ): kotlin.Result<DailyMusicInfo>
    
    suspend fun validateProviderApiKey(providerConfig: AiProviderConfig): kotlin.Result<Boolean>
    
    // Listening Duration
    suspend fun insertPlayback(history: PlaybackHistory)
    suspend fun recordListeningDuration(duration: Long)
}
