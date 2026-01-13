package com.example.hearablemusicplayer.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String
)

data class PlaylistItem(
    val songUrl: String,
    val songId: Long,
    val playlistId: Long,
)
