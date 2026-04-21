package com.hmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Entity(tableName = "playback_history")
data class PlaybackHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val musicId: Long, // 关联 Music 表的 id
    val timestamp: Long, // 播放时间戳
    val durationPlayed: Int, // 播放时长（秒）
    val isCompleted: Boolean // 是否播放完成
)

@Dao
interface PlaybackHistoryDao {
    @Insert
    suspend fun insert(playbackHistory: PlaybackHistory)

    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentPlaybackHistory(limit: Int): List<PlaybackHistory>

    @Query("SELECT * FROM playback_history WHERE musicId = :musicId ORDER BY timestamp DESC")
    suspend fun getPlaybackHistoryByMusicId(musicId: Long): List<PlaybackHistory>

    @Query("DELETE FROM playback_history WHERE timestamp < :thresholdTimestamp")
    suspend fun deleteOlderThan(thresholdTimestamp: Long)

    @Query("DELETE FROM playback_history WHERE musicId = :musicId")
    suspend fun deleteByMusicId(musicId: Long)

    @Query("SELECT COUNT(*) FROM playback_history WHERE date(timestamp / 1000, 'unixepoch') = date('now')")
    suspend fun getTodayPlayCount(): Int
}
