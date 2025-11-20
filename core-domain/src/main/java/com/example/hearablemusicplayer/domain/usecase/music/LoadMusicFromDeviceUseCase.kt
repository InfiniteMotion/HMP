package com.example.hearablemusicplayer.domain.usecase.music

import com.example.hearablemusicplayer.data.repository.MusicRepository
import com.example.hearablemusicplayer.data.repository.Result
import javax.inject.Inject

/**
 * 从设备扫描音乐文件并加载到数据库
 * Use Case: 业务逻辑层,封装音乐扫描的完整流程
 */
class LoadMusicFromDeviceUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * 执行音乐扫描
     * @return Result<Unit> 扫描结果
     */
    suspend operator fun invoke(): Result<Unit> {
        return musicRepository.loadMusicFromDevice()
    }
    
    /**
     * 获取扫描状态
     */
    fun isScanning() = musicRepository.isScanning
}
