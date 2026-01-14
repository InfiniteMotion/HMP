package com.example.hearablemusicplayer.domain.usecase.music

import com.example.hearablemusicplayer.domain.model.MusicLabel
import com.example.hearablemusicplayer.domain.model.enum.LabelCategory
import com.example.hearablemusicplayer.domain.model.enum.LabelName
import com.example.hearablemusicplayer.domain.repository.MusicRepository
import com.example.hearablemusicplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 音乐标签Use Case
 */
class MusicLabelUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository
) {
    /**
     * 添加单个音乐标签
     */
    suspend fun addMusicLabel(musicLabel: MusicLabel) {
        musicRepository.addMusicLabel(musicLabel)
    }

    /**
     * 批量添加音乐标签
     */
    suspend fun addMusicLabels(musicId: Long, labels: MusicLabels) {
        // 添加风格标签
        labels.genres.forEach { genreName ->
            val labelName = LabelName.match(genreName) ?: LabelName.UNKNOWN
            musicRepository.addMusicLabel(
                MusicLabel(musicId, LabelCategory.GENRE, labelName)
            )
        }
        
        // 添加情绪标签
        labels.moods.forEach { moodName ->
            val labelName = LabelName.match(moodName) ?: LabelName.UNKNOWN
            musicRepository.addMusicLabel(
                MusicLabel(musicId, LabelCategory.MOOD, labelName)
            )
        }
        
        // 添加场景标签
        labels.scenarios.forEach { scenarioName ->
            val labelName = LabelName.match(scenarioName) ?: LabelName.UNKNOWN
            musicRepository.addMusicLabel(
                MusicLabel(musicId, LabelCategory.SCENARIO, labelName)
            )
        }
        
        // 添加语言标签
        labels.language?.let { languageName ->
            val labelName = LabelName.match(languageName) ?: LabelName.UNKNOWN
            musicRepository.addMusicLabel(
                MusicLabel(musicId, LabelCategory.LANGUAGE, labelName)
            )
        }
        
        // 添加年代标签
        labels.era?.let { eraName ->
            val labelName = LabelName.match(eraName) ?: LabelName.UNKNOWN
            musicRepository.addMusicLabel(
                MusicLabel(musicId, LabelCategory.ERA, labelName)
            )
        }
    }

    /**
     * 获取音乐的所有标签
     */
    suspend fun getMusicLabels(musicId: Long): List<MusicLabel?> {
        return musicRepository.getMusicLabels(musicId)
    }

    /**
     * 获取指定类别的标签名称列表
     */
    fun getLabelNamesByType(category: LabelCategory): Flow<List<LabelName>> {
        return musicRepository.getLabelNamesByType(category)
    }

    /**
     * 获取指定标签的音乐ID列表
     */
    suspend fun getMusicIdListByLabel(labelName: LabelName): List<Long> {
        return musicRepository.getMusicIdListByType(labelName)
    }

    /**
     * 获取指定标签的音乐列表
     */
    suspend fun getMusicListByLabel(labelName: LabelName) = playlistRepository.getPlaylistByIdList(
        musicRepository.getMusicIdListByType(labelName)
    )
}

/**
 * 音乐标签数据类
 */
data class MusicLabels(
    val genres: List<String> = emptyList(),
    val moods: List<String> = emptyList(),
    val scenarios: List<String> = emptyList(),
    val language: String? = null,
    val era: String? = null
)
