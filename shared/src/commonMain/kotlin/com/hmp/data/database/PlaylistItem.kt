package com.hmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "playlist_items")
data class PlaylistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val playlistId: Int,
    val musicId: String,
    val position: Int,
    val addedAt: Date = Date()
)
