package com.example.hearablemusicplayer.domain.usecase.playback

import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.repository.MusicRepository
import com.example.hearablemusicplayer.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 当前播放管理
 * Use Case: 封装当前播放音乐和播放列表的管理逻辑
 */
class CurrentPlaybackUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * 获取当前播放列表中的音乐
     * @param playlistId 播放列表ID
     * @return 音乐列表Flow
     */
    fun getMusicInPlaylist(playlistId: Long): Flow<List<MusicInfo>> {
        return musicRepository.getMusicInfoInPlaylist(playlistId)
    }
    
    /**
     * 获取当前播放音乐ID
     */
    val currentMusicId: Flow<Long?> = settingsRepository.currentMusicId
    
    /**
     * 保存当前播放音乐ID
     */
    suspend fun saveCurrentMusicId(musicId: Long) {
        settingsRepository.saveCurrentMusicId(musicId)
    }
    
    /**
     * 获取音乐的标签信息
     * @param musicId 音乐ID
     * @return 标签列表
     */
    suspend fun getMusicLabels(musicId: Long) = musicRepository.getMusicLabels(musicId)
    
    /**
     * 获取音乐的歌词
     * @param musicId 音乐ID
     * @return 歌词文本
     */
    suspend fun getMusicLyrics(musicId: Long) = musicRepository.getMusicLyrics(musicId)
}
