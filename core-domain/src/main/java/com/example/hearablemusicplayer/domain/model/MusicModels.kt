package com.example.hearablemusicplayer.domain.model

import com.example.hearablemusicplayer.domain.model.enum.LabelCategory
import com.example.hearablemusicplayer.domain.model.enum.LabelName

data class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: String,
)

data class MusicExtra(
    val id: Long,
    val lyrics: String? = null,
    val bitRate: Int? = null,           // 比特率 kbps
    val sampleRate: Int? = null,        // 采样率 Hz
    val fileSize: Long? = null,         // 文件大小 Byte
    val format: String? = null,         // 文件格式 mp3/flac
    val language: String? = null,       // 语言
    val year: Int? = null,              // 年份
    val recommendationIds: String? = null,  // 推荐关联的音乐ID列表
    // 其他额外信息
    val isGetExtraInfo : Boolean,
    val rewards : String? = null,
    val popLyric : String? = null,
    val singerIntroduce : String? = null,
    val backgroundIntroduce : String? = null,
    val description : String? = null,
    val relevantMusic : String? = null
)

data class MusicInfo(
    val music: Music,
    val extra: MusicExtra?,
    val userInfo: UserInfo?
)

data class MusicLabel(
    val musicId: Long,
    val type: LabelCategory,
    val label: LabelName
)

data class UserInfo(
    val id: Long,
    val liked: Boolean = false,
    val disLiked: Boolean = false,
    val lastPlayed: Int? = null,
    val playCount: Int? = null,
    val skippedCount: Int? = null,
    val userRating: Int? = null,
    val inCustomPlaylistCount: Int? = null,
)
