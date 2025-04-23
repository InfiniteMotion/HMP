package com.example.hearablemusicplayer.database.myClass

//播放排行榜中的单条数据
data class PlayCountEntry(
    val musicId: Long,
    val playCount: Int
)
