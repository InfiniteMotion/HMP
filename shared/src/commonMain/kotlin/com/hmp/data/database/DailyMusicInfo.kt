package com.hmp.data.database

import kotlinx.datetime.LocalDate

data class DailyMusicInfo(
    val date: String, // 日期，格式：YYYY-MM-DD
    val totalListeningDuration: Int, // 总收听时长（秒）
    val musicCount: Int, // 收听歌曲数量
    val playCount: Int, // 播放次数
    val mostPlayedMusicId: Long? // 最常播放的音乐ID
)
