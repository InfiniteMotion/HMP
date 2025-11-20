package com.example.hearablemusicplayer.domain.usecase.music

import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.repository.MusicRepository
import javax.inject.Inject

/**
 * 搜索音乐
 * Use Case: 封装音乐搜索逻辑
 */
class SearchMusicUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * 根据关键词搜索音乐
     * @param query 搜索关键词
     * @return 匹配的音乐列表
     */
    suspend operator fun invoke(query: String): List<MusicInfo> {
        return musicRepository.searchMusic(query)
    }
}
