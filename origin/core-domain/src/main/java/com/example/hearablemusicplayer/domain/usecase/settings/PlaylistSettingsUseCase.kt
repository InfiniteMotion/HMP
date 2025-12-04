package com.example.hearablemusicplayer.domain.usecase.settings

import com.example.hearablemusicplayer.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 播放列表设置管理
 * Use Case: 封装当前播放列表、收藏列表、最近播放列表的设置
 */
class PlaylistSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    // 当前播放列表ID
    val currentPlaylistId: Flow<Long?> = settingsRepository.currentPlaylistId
    
    suspend fun getCurrentPlaylistId(): Long? {
        return settingsRepository.getCurrentPlaylistId()
    }
    
    suspend fun saveCurrentPlaylistId(id: Long) {
        settingsRepository.saveCurrentPlaylistId(id)
    }
    
    // 收藏播放列表ID
    val likedPlaylistId: Flow<Long?> = settingsRepository.likedPlaylistId
    
    suspend fun getLikedPlaylistId(): Long? {
        return settingsRepository.getLikedPlaylistId()
    }
    
    suspend fun saveLikedPlaylistId(id: Long) {
        settingsRepository.saveLikedPlaylistId(id)
    }
    
    // 最近播放列表ID
    val recentPlaylistId: Flow<Long?> = settingsRepository.recentPlaylistId
    
    suspend fun getRecentPlaylistId(): Long? {
        return settingsRepository.getRecentPlaylistId()
    }
    
    suspend fun saveRecentPlaylistId(id: Long) {
        settingsRepository.saveRecentPlaylistId(id)
    }
}
