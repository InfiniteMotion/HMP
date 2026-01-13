package com.example.hearablemusicplayer.domain.model

data class ListeningDuration(
    val date: String,  // 使用 yyyy-MM-dd 格式存储日期
    val duration: Long, // 以毫秒为单位存储时长
    val updatedAt: Long = System.currentTimeMillis() // 最后更新时间
)

