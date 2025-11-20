package com.example.hearablemusicplayer.domain.usecase.music

import com.example.hearablemusicplayer.data.database.MusicLabel
import com.example.hearablemusicplayer.data.database.myenum.LabelCategory
import com.example.hearablemusicplayer.data.database.myenum.LabelName
import com.example.hearablemusicplayer.data.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 音乐标签Use Case
 * 
 * 负责音乐标签的完整管理，包括：
 * - 添加单个或批量添加标签（风格、情绪、场景、语言、年代）
 * - 查询音乐的所有标签
 * - 根据标签类型获取所有标签名
 * - 根据标签查询音乐列表
 * 
 * @property musicRepository 音乐数据仓库
 */
class MusicLabelUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * 添加单个音乐标签
     * 
     * @param musicLabel 要添加的标签
     */
    suspend fun addMusicLabel(musicLabel: MusicLabel) {
        musicRepository.addMusicLabel(musicLabel)
    }

    /**
     * 批量添加音乐标签
     * 
     * 从AI生成的标签数据中批量添加音乐标签，
     * 包括风格、情绪、场景、语言和年代标签。
     * 
     * @param musicId 音乐ID
     * @param labels 所有类型的标签数据
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
    suspend fun getMusicListByLabel(labelName: LabelName) = musicRepository.getPlaylistByIdList(
        musicRepository.getMusicIdListByType(labelName)
    )
}

/**
 * 音乐标签数据类
 * 
 * 用于批量添加标签时传递所有类型的标签。
 * 通常由AI生成后使用。
 * 
 * @property genres 风格标签列表（如ROCK, POP, JAZZ）
 * @property moods 情绪标签列表（如HAPPY, SAD, CALM）
 * @property scenarios 场景标签列表（如WORKOUT, SLEEP, PARTY）
 * @property language 语言标签（如CHINESE, ENGLISH）
 * @property era 年代标签（如1970S, 1980S）
 */
data class MusicLabels(
    val genres: List<String> = emptyList(),
    val moods: List<String> = emptyList(),
    val scenarios: List<String> = emptyList(),
    val language: String? = null,
    val era: String? = null
)
