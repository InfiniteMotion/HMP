package com.example.hearablemusicplayer.data.mapper

import com.example.hearablemusicplayer.data.database.Playlist as PlaylistEntity
import com.example.hearablemusicplayer.data.database.PlaylistItem as PlaylistItemEntity
import com.hmp.domain.playlist.Playlist
import com.hmp.domain.playlist.PlaylistItem

// Playlist
fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    name = name,
    coverUri = coverUri,
    playCount = playCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPlayedAt = lastPlayedAt,
    description = description,
    songCount = songCount,
    totalDurationMs = totalDurationMs,
    isPinned = isPinned
)

fun Playlist.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id,
    name = name,
    coverUri = coverUri,
    playCount = playCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPlayedAt = lastPlayedAt,
    description = description,
    songCount = songCount,
    totalDurationMs = totalDurationMs,
    isPinned = isPinned
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
