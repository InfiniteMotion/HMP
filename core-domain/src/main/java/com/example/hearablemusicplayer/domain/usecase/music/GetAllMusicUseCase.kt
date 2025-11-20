package com.example.hearablemusicplayer.domain.usecase.music

import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取所有音乐
 * Use Case: 封装获取音乐列表的逻辑,支持排序
 */
class GetAllMusicUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * 获取所有音乐列表
     * @param orderBy 排序字段 (title, artist, album, duration等)
     * @param orderType 排序方式 (ASC, DESC)
     * @return 音乐列表
     */
    suspend operator fun invoke(orderBy: String = "title", orderType: String = "ASC"): List<MusicInfo> {
        return musicRepository.getAllMusicInfoAsList(orderBy, orderType)
    }
    
    /**
     * 获取音乐总数
     */
    fun getMusicCount(): Flow<Int> = musicRepository.getMusicCount()
    
    /**
     * 获取已处理额外信息的音乐数量
     */
    fun getMusicWithExtraCount(): Flow<Int> = musicRepository.getMusicWithExtraCount()
}
