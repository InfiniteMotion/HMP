package com.example.hearablemusicplayer.domain.setting.usecase

import com.example.hearablemusicplayer.domain.music.MusicRepository
import com.example.hearablemusicplayer.domain.setting.model.UserUsageAnalytics
import javax.inject.Inject

/**
 * 获取用户使用数据分析结果，供「用户使用数据」页展示。
 */
class GetUserUsageDataUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend fun getAnalytics(): UserUsageAnalytics {
        return musicRepository.getUserUsageAnalytics()
    }
}
