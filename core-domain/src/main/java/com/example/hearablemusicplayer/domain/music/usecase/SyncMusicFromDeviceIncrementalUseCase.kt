package com.example.hearablemusicplayer.domain.music.usecase

import com.example.hearablemusicplayer.domain.music.MusicRepository
import javax.inject.Inject

/**
 * 增量扫描设备音乐文件并同步到数据库
 */
class SyncMusicFromDeviceIncrementalUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return musicRepository.syncMusicFromDeviceIncremental()
    }

    fun isScanning() = musicRepository.isScanning
}

