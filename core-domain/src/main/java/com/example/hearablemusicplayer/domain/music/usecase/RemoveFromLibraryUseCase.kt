package com.example.hearablemusicplayer.domain.music.usecase

import com.example.hearablemusicplayer.domain.music.MusicRepository
import javax.inject.Inject

/**
 * 从曲库移除：对指定 id 列表执行软删除（markDeleted），曲库/播放列表等查询将不再返回这些项。
 */
class RemoveFromLibraryUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(ids: List<Long>) {
        if (ids.isEmpty()) return
        musicRepository.removeFromLibrary(ids)
    }
}
