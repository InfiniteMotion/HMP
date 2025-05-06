package com.example.hearablemusicplayer.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey


//Playlist 表：存储播放列表的基本信息（如播放列表名称、ID 等）
@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
@Dao
interface PlaylistDao {
    @Insert
    suspend fun insert(playlist: Playlist): Long

//    @Query("DELETE FROM playlist WHERE id = :id")
//    suspend fun deletePlaylist(id: Long)
}