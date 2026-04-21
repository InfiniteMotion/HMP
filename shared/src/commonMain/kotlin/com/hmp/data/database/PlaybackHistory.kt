package com.hmp.data.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity
data class PlaybackHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val musicId: Long,
    val playedAt: Long,
    val playDuration: Long = 0,
    val isCompleted: Boolean = false,
    val source: String? = null
)

@Dao
interface PlaybackHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playback: PlaybackHistory): Long

    @Query("UPDATE PlaybackHistory SET playDuration = :duration, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updatePlaybackRecord(id: Long, duration: Long, isCompleted: Boolean)

    @Query("SELECT * FROM PlaybackHistory ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int): List<PlaybackHistory>

    @Query("SELECT * FROM PlaybackHistory WHERE musicId = :musicId ORDER BY playedAt DESC LIMIT :limit")
    fun getHistoryForMusic(musicId: Long, limit: Int): Flow<List<PlaybackHistory>>

    @Query("DELETE FROM PlaybackHistory")
    suspend fun deleteAll()

    @Query("SELECT * FROM PlaybackHistory")
    suspend fun getAllHistory(): List<PlaybackHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(history: List<PlaybackHistory>)
}
