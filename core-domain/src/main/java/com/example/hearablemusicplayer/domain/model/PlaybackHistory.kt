package com.example.hearablemusicplayer.domain.model

data class PlaybackHistory(
    val id: Long = 0,
    val musicId: Long,
    val playedAt: Long,
    val playDuration: Long = 0,
    val isCompleted: Boolean = false,
    val source: String? = null
)
