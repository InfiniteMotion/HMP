package com.hmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "playlists")
data class PlayList(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val coverUri: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isSystem: Boolean = false,
    val isPublic: Boolean = false
)
