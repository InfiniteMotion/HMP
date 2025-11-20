package com.example.hearablemusicplayer.domain.usecase.playlist

import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.repository.MusicRepository
import javax.inject.Inject

/**
 * 管理用户播放列表
 * Use Case: 封装播放列表的创建、删除、获取逻辑
 */
class ManagePlaylistUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * 创建播放列表
     * @param name 播放列表名称
     * @return 播放列表ID
     */
    suspend fun createPlaylist(name: String): Long {
        return musicRepository.createPlaylist(name)
    }
    
    /**
     * 删除播放列表
     * @param name 播放列表名称
     */
    suspend fun removePlaylist(name: String) {
        musicRepository.removePlaylist(name)
    }
    
    /**
     * 根据ID获取播放列表
     * @param playlistId 播放列表ID
     * @return 音乐列表
     */
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo> {
        return musicRepository.getPlaylistById(playlistId)
    }
}
