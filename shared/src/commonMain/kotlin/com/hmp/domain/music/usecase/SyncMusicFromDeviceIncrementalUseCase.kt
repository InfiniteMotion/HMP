package com.hmp.domain.music.usecase

import com.hmp.domain.music.MusicRepository

/**
 * 增量扫描设备音乐文件并同步到数据库
 */
class SyncMusicFromDeviceIncrementalUseCase(
    
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return musicRepository.syncMusicFromDeviceIncremental()
    }

    fun isScanning() = musicRepository.isScanning
}

