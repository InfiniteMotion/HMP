package com.example.hearablemusicplayer.domain.usecase.playlist

import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.database.myenum.LabelCategory
import com.example.hearablemusicplayer.data.database.myenum.LabelName
import com.example.hearablemusicplayer.data.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取标签播放列表
 * Use Case: 封装根据标签获取音乐列表的逻辑
 */
class GetLabelPlaylistUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * 根据标签类型获取标签名称列表
     */
    fun getLabelNamesByCategory(category: LabelCategory): Flow<List<LabelName>> {
        return musicRepository.getLabelNamesByType(category)
    }
    
    /**
     * 根据标签获取音乐ID列表
     */
    suspend fun getMusicIdListByLabel(label: LabelName): List<Long> {
        return musicRepository.getMusicIdListByType(label)
    }
    
    /**
     * 根据音乐ID列表获取音乐详情列表
     */
    suspend fun getPlaylistByIdList(idList: List<Long>): List<MusicInfo> {
        return musicRepository.getPlaylistByIdList(idList)
    }
}
