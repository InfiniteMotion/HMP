package com.hmp.data.util

import com.hmp.domain.music.EditableMusicTags

/**
 * 单曲标签（ID3 元数据）写入器。
 * 将标题/艺术家/专辑写入音乐文件的标签，成功后返回 Result.success。
 */
expect object MusicTagEditor {
    fun writeTags(filePath: String, tags: EditableMusicTags): Result<Unit>
}
