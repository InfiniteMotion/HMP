package com.example.hearablemusicplayer.domain.usecase.playback

import com.example.hearablemusicplayer.data.database.myenum.PlaybackMode
import com.example.hearablemusicplayer.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 播放模式管理
 * Use Case: 封装播放模式的读写逻辑
 */
class PlaybackModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * 获取当前播放模式
     */
    val playbackMode: Flow<PlaybackMode?> = settingsRepository.playbackMode
    
    /**
     * 保存播放模式
     */
    suspend fun savePlaybackMode(mode: PlaybackMode) {
        settingsRepository.savePlaybackMode(mode)
    }
}
