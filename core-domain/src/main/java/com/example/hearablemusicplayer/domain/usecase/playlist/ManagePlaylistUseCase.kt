package com.example.hearablemusicplayer.domain.usecase.playlist

import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 播放列表管理Use Case
 */
class ManagePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    /**
     * 创建播放列表
     */
    suspend fun createPlaylist(name: String): Long {
        return playlistRepository.createPlaylist(name)
    }
    
    /**
     * 删除播放列表
     */
    suspend fun removePlaylist(name: String) {
        playlistRepository.removePlaylist(name)
    }
    
    /**
     * 根据ID获取播放列表
     */
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo> {
        return playlistRepository.getPlaylistById(playlistId)
    }
    
    /**
     * 获取播放列表中的音乐(Flow)
     */
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>> {
        return playlistRepository.getMusicInfoInPlaylist(playlistId)
    }
    
    /**
     * 添加音乐到播放列表
     */
    suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String) {
        playlistRepository.addToPlaylist(playlistId, musicId, musicPath)
    }
    
    /**
     * 从播放列表中移除音乐
     */
    suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long) {
        playlistRepository.removeItemFromPlaylist(musicId, playlistId)
    }
    
    /**
     * 重置播放列表内容
     */
    suspend fun resetPlaylistItems(playlistId: Long, playlist: List<MusicInfo>) {
        playlistRepository.resetPlaylistItems(playlistId, playlist)
    }
}
