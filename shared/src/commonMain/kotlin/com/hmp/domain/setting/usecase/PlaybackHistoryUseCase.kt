package com.hmp.domain.setting.usecase

import com.hmp.domain.music.MusicRepository
import com.hmp.domain.setting.model.PlaybackHistory
import kotlinx.coroutines.flow.Flow

/**
 * 播放历史管理
 * Use Case: 封装播放历史的记录逻辑
 */
class PlaybackHistoryUseCase(
    
    private val musicRepository: MusicRepository
) {
    /**
     * 插入播放历史记录
     * @param history 播放历史对象
     * @return 插入记录的ID
     */
    suspend fun insertPlayback(history: PlaybackHistory): Long {
        return musicRepository.insertPlayback(history)
    }

    /**
     * 开始一个新的播放会话记录
     * @param musicId 音乐ID
     * @param source 播放来源
     * @return 播放历史记录的ID
     */
    suspend fun startPlaybackSession(musicId: Long, source: String? = null): Long {
        val now = System.currentTimeMillis()
        // 更新最后播放时间
        musicRepository.updateLastPlayed(musicId, now)
        // 只要开始播放就计入播放次数
        musicRepository.incrementPlayCount(musicId)
        // 插入初始播放记录
        return musicRepository.insertPlayback(
            PlaybackHistory(
                musicId = musicId,
                playedAt = now,
                playDuration = 0,
                isCompleted = false,
                source = source
            )
        )
    }

    /**
     * 完成播放会话记录
     * @param historyId 播放历史记录ID
     * @param musicId 音乐ID
     * @param duration 实际播放时长
     */
    suspend fun completePlaybackSession(historyId: Long, musicId: Long, duration: Long) {
        musicRepository.updatePlaybackRecord(historyId, duration, true)
        // 播放次数已在开始时记录，此处不再重复增加
    }

    /**
     * 标记播放会话为跳过或部分播放
     * @param historyId 播放历史记录ID
     * @param musicId 音乐ID
     * @param duration 实际播放时长
     * @param isSkip 是否判定为跳过
     */
    suspend fun skipPlaybackSession(historyId: Long, musicId: Long, duration: Long, isSkip: Boolean) {
        musicRepository.updatePlaybackRecord(historyId, duration, false)
        if (isSkip) {
            musicRepository.incrementSkippedCount(musicId)
        }
    }

    /**
     * 记录播放历史 (旧方法保留兼容)
     * @param musicId 音乐ID
     */
    suspend fun recordPlaybackHistory(musicId: Long) {
        startPlaybackSession(musicId)
    }

    /**
     * 记录听歌时长
     * @param duration 播放时长(毫秒)
     */
    suspend fun recordListeningDuration(duration: Long) {
        musicRepository.recordListeningDuration(duration)
    }

    /**
     * 获取特定歌曲的最近播放历史
     * @param musicId 音乐ID
     * @param limit 获取记录的数量
     */
    fun getPlaybackHistory(musicId: Long, limit: Int = 5): Flow<List<PlaybackHistory>> {
        return musicRepository.getPlaybackHistory(musicId, limit)
    }
}