package com.example.hearablemusicplayer.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.hearablemusicplayer.database.myClass.PlayCountEntry
import kotlinx.coroutines.flow.Flow

//用户每次播放音乐的记录实体类
@Entity
data class PlaybackHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                 // 自动生成的主键，用于标识唯一记录
    val musicId: Long,               // 播放的音乐的 ID（外键，关联 Music 表）
    val playedAt: Long,              // 播放时间戳，使用 System.currentTimeMillis() 获取
    val playDuration: Long = 0,      // 播放时长（单位：毫秒），可用于分析用户是否听完整
    val isCompleted: Boolean = false,// 是否完整播放（例如播放超过 90% 可认为完整）
    val source: String? = null       // 播放来源：推荐、搜索、播放列表、收藏等
)

@Dao
interface PlaybackHistoryDao {
    //插入一条播放记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playback: PlaybackHistory)

    //获取最近播放的记录（按播放时间倒序排列）
    @Query("SELECT * FROM PlaybackHistory ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<PlaybackHistory>>

    //获取某首音乐的播放历史（按播放时间倒序）
    @Query("SELECT * FROM PlaybackHistory WHERE musicId = :musicId ORDER BY playedAt DESC")
    fun getHistoryForMusic(musicId: Long): Flow<List<PlaybackHistory>>

    //获取播放次数最多的音乐（排行榜）
    @Query("SELECT musicId, COUNT(*) as playCount FROM PlaybackHistory GROUP BY musicId ORDER BY playCount DESC")
    fun getTopPlayed(): Flow<List<PlayCountEntry>>

}
