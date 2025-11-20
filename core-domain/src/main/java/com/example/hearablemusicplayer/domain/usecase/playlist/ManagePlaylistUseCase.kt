package com.example.hearablemusicplayer.domain.usecase.playlist

import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 播放列表管理Use Case
 * 
 * 负责播放列表的完整生命周期管理，包括：
 * - 创建和删除播放列表
 * - 查询播放列表内容
 * - 添加和移除播放列表中的音乐
 * - 重置播放列表全部内容
 * 
 * @property musicRepository 音乐数据仓库
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
    
    /**
     * 获取播放列表中的音乐(Flow)
     * @param playlistId 播放列表ID
     * @return 音乐列表Flow
     */
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>> {
        return musicRepository.getMusicInfoInPlaylist(playlistId)
    }
    
    /**
     * 添加音乐到播放列表
     * @param playlistId 播放列表ID
     * @param musicId 音乐ID
     * @param musicPath 音乐路径
     */
    suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String) {
        musicRepository.addToPlaylist(playlistId, musicId, musicPath)
    }
    
    /**
     * 从播放列表中移除音乐
     * @param musicId 音乐ID
     * @param playlistId 播放列表ID
     */
    suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long) {
        musicRepository.removeItemFromPlaylist(musicId, playlistId)
    }
    
    /**
     * 重置播放列表内容
     * @param playlistId 播放列表ID
     * @param playlist 新的音乐列表
     */
    suspend fun resetPlaylistItems(playlistId: Long, playlist: List<MusicInfo>) {
        musicRepository.resetPlaylistItems(playlistId, playlist)
    }
}
