package com.example.hearablemusicplayer.domain.model

data class ListeningDuration(
    val date: String,  // 使用 yyyy-MM-dd 格式存储日期
    val duration: Long, // 以毫秒为单位存储时长
    val updatedAt: Long = System.currentTimeMillis() // 最后更新时间
)

data class PlaybackHistory(
    val id: Long = 0,                 // 自动生成的主键,用于标识唯一记录
    val musicId: Long,               // 播放的音乐的 ID(外键,关联 Music 表)
    val playedAt: Long,              // 播放时间戳,使用 System.currentTimeMillis() 获取
    val playDuration: Long = 0,      // 播放时长(单位:毫秒),可用于分析用户是否听完整
    val isCompleted: Boolean = false,// 是否完整播放(例如播放超过 90% 可认为完整)
    val source: String? = null       // 播放来源:推荐、搜索、播放列表、收藏等
)
