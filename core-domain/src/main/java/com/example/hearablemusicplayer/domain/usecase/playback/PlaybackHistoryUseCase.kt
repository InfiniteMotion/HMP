package com.example.hearablemusicplayer.domain.usecase.playback

import com.example.hearablemusicplayer.data.database.PlaybackHistory
import com.example.hearablemusicplayer.data.repository.MusicRepository
import javax.inject.Inject

/**
 * 播放历史管理
 * Use Case: 封装播放历史的记录逻辑
 */
class PlaybackHistoryUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * 插入播放历史记录
     * @param history 播放历史对象
     */
    suspend fun insertPlayback(history: PlaybackHistory) {
        musicRepository.insertPlayback(history)
    }
    
    /**
     * 记录播放历史
     * @param musicId 音乐ID
     */
    suspend fun recordPlaybackHistory(musicId: Long) {
        musicRepository.insertPlayback(
            PlaybackHistory(
                musicId = musicId,
                playedAt = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * 记录听歌时长
     * @param duration 播放时长(毫秒)
     */
    suspend fun recordListeningDuration(duration: Long) {
        musicRepository.recordListeningDuration(duration)
    }
}
