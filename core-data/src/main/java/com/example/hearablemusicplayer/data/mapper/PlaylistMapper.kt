package com.example.hearablemusicplayer.data.mapper

import com.example.hearablemusicplayer.data.database.Playlist as PlaylistEntity
import com.example.hearablemusicplayer.data.database.PlaylistItem as PlaylistItemEntity
import com.example.hearablemusicplayer.domain.model.Playlist
import com.example.hearablemusicplayer.domain.model.PlaylistItem

// Playlist
fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    name = name
)

fun Playlist.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id,
    name = name
)

// PlaylistItem
fun PlaylistItemEntity.toDomain(): PlaylistItem = PlaylistItem(
    songUrl = songUrl,
    songId = songId,
    playlistId = playlistId
)

fun PlaylistItem.toEntity(itemOrder: Int = 0): PlaylistItemEntity = PlaylistItemEntity(
    songUrl = songUrl,
    songId = songId,
    playlistId = playlistId,
    itemOrder = itemOrder
)
