package com.example.hearablemusicplayer.data.database
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "listeningDuration")
data class ListeningDuration(
    @PrimaryKey
    val date: String,  // 使用 yyyy-MM-dd 格式存储日期
    val duration: Long, // 以毫秒为单位存储时长
    val updatedAt: Long = System.currentTimeMillis() // 最后更新时间
)

@Dao
interface ListeningDurationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(duration: ListeningDuration)
    
    @Query("SELECT * FROM listeningDuration ORDER BY date DESC LIMIT :limit")
    fun getRecentDurations(limit: Int): Flow<List<ListeningDuration>>
    
    @Query("SELECT * FROM listeningDuration WHERE date = :date")
    suspend fun getDurationByDate(date: String): ListeningDuration?
    
    @Query("UPDATE listeningDuration SET duration = duration + :additionalDuration, updatedAt = :updateTime WHERE date = :date")
    suspend fun updateDuration(date: String, additionalDuration: Long, updateTime: Long)
}
