package com.hmp.domain.playlist.usecase

import com.hmp.domain.music.MusicInfo
import com.hmp.domain.playlist.AlgorithmType
import com.hmp.domain.playlist.ExtensionConfig
import com.hmp.domain.playlist.WeightTemplate
import com.hmp.domain.playlist.algorithm.core.WeightManager
import com.hmp.domain.playlist.algorithm.PlaylistAlgorithms

import com.hmp.domain.music.MusicRepository
import com.hmp.domain.setting.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * 播放列表生成用例
 * 整合算法工厂和自适应截断功能的核心业务用例
 */
class GeneratePlaylistUseCase(
    
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend fun getSavedAlgorithmType(): AlgorithmType {
        return try {
            AlgorithmType.valueOf(settingsRepository.getDefaultAlgorithmType())
        } catch (e: Exception) {
            AlgorithmType.OPTIMIZED_SIMILARITY
        }
    }

    suspend fun getSavedWeightTemplate(): WeightTemplate {
        return try {
            WeightTemplate.valueOf(settingsRepository.getDefaultWeightTemplate())
        } catch (e: Exception) {
            WeightTemplate.BALANCED
        }
    }

    /**
     * 使用持久化配置生成播放列表
     * 如果没有提供具体参数，则使用持久化的默认配置
     */
    suspend fun execute(
        seedMusicId: Long,
        minLength: Int = 15,
    ): GeneratePlaylistResult {
        // 从持久化配置获取默认值
        val defaultAlgorithm = getSavedAlgorithmType()

        val defaultTemplate = getSavedWeightTemplate()


        // 使用传入参数或默认值
        val effectiveAlgorithm = defaultAlgorithm
        val effectiveTemplate = defaultTemplate
        val effectiveConfig = ExtensionConfig.BALANCED

        return try {
            // 验证输入
            if (!validateMusicExists(seedMusicId)) {
                return GeneratePlaylistResult.Error("种子音乐不存在")
            }

            // 使用权重模板配置
            val effectiveWeights = WeightManager.convertWeightTemplate(effectiveTemplate)

            // 获取算法实例
            val algorithm = createAlgorithm(effectiveAlgorithm, musicRepository)

            // 生成初始推荐列表
            val initialPlaylist = algorithm.generate(
                seedMusicId,
                effectiveAlgorithm,
                minLength * 3,
                effectiveWeights
            )

            // 应用自适应截断
            val finalPlaylist = applyAdaptiveTruncation(
                initialPlaylist,
                effectiveConfig
            )

            GeneratePlaylistResult.Success(
                playlist = finalPlaylist,
                algorithmUsed = algorithm.algorithmName,
                actualLength = finalPlaylist.size
            )
        } catch (e: Exception) {
            GeneratePlaylistResult.Error("生成播放列表失败: ${e.message}")
        }
    }

    /**
     * 应用自适应截断
     */
    private fun applyAdaptiveTruncation(
        initialPlaylist: List<MusicInfo>,
        config: ExtensionConfig
    ): List<MusicInfo> {
        return initialPlaylist.take(config.getMaxExtendedLength())
    }
    
    private suspend fun validateMusicExists(musicId: Long): Boolean {
        return musicRepository.getMusicInfoById(musicId).firstOrNull() != null
    }
}

/**
 * 生成播放列表结果密封类
 */
sealed class GeneratePlaylistResult {
    data class Success(
        val playlist: List<MusicInfo>,
        val algorithmUsed: String,
        val actualLength: Int
    ) : GeneratePlaylistResult()
    
    data class Error(val message: String) : GeneratePlaylistResult()
}

// 私有辅助方法
private fun createAlgorithm(type: AlgorithmType, repository: MusicRepository): PlaylistAlgorithms {
    return PlaylistAlgorithms(repository)
}

