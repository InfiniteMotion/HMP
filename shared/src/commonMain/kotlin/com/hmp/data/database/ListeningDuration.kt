package com.hmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.datetime.LocalDate

@Entity(tableName = "listening_durations")
data class ListeningDuration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // 使用字符串存储日期，格式：YYYY-MM-DD
    val totalDuration: Int, // 单位：秒
    val musicCount: Int, // 听歌数量
    val timestamp: Long // 创建时间戳
)

@Dao
interface ListeningDurationDao {
    @Insert
    suspend fun insert(listeningDuration: ListeningDuration)

    @Query("SELECT * FROM listening_durations ORDER BY date DESC")
    suspend fun getAllListeningDurations(): List<ListeningDuration>

    @Query("SELECT * FROM listening_durations WHERE date = :date")
    suspend fun getListeningDurationByDate(date: String): ListeningDuration?

    @Query("UPDATE listening_durations SET totalDuration = :totalDuration, musicCount = :musicCount, timestamp = :timestamp WHERE date = :date")
    suspend fun updateListeningDuration(date: String, totalDuration: Int, musicCount: Int, timestamp: Long)

    @Query("DELETE FROM listening_durations WHERE date < :thresholdDate")
    suspend fun deleteOlderThan(thresholdDate: String)
}
