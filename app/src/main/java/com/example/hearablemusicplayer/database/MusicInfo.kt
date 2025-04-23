package com.example.hearablemusicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "music")
data class Music(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: String?,
    val liked: Boolean = false
)

@Dao
interface MusicDao {

    // 批量插入音乐数据，如果主键冲突则替换已有数据
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(musics: List<Music>)

    // 清空音乐表中所有数据
    @Query("DELETE FROM music")
    suspend fun deleteAllMusic()

    // 获取所有音乐数据（以列表形式返回，适用于协程场景）
    @Query("SELECT * FROM music")
    suspend fun getAllAsList(): List<Music>

    // 获取所有音乐数据（按标题排序，适用于 LiveData 观察 UI）
    @Query("SELECT * FROM music")
    fun getAll(): Flow<List<Music>>

    // 通过 ID 获取单个音乐信息（Flow 用于流式数据监听）
    @Query("SELECT * FROM music WHERE id = :musicId")
    fun getMusicById(musicId: Long): Flow<Music?>

    // 根据 ID 获取音乐文件的路径
    @Query("SELECT path FROM music WHERE id = :musicId")
    suspend fun getMusicPathById(musicId: Long): String

    // 随机获取 5 首音乐（适用于“猜你喜欢”等功能）
    @Query("SELECT * FROM music ORDER BY RANDOM() LIMIT 5")
    suspend fun getRandomMusic(): List<Music>

    // 搜索音乐标题或艺术家名中包含关键词的记录
    @Query("SELECT * FROM music WHERE title LIKE :query OR artist LIKE :query")
    fun searchMusic(query: String): List<Music>

    // 更新某首歌的红心（喜欢）状态
    @Query("UPDATE music SET liked = :liked WHERE id = :id")
    suspend fun updateLikedStatus(id: Long, liked: Boolean)

    // 获取当前歌曲之后的所有歌曲，按 ID 顺序排列（用于自动加入播放列表）
    @Query("SELECT * FROM music WHERE id > :currentId ORDER BY id ASC")
    fun getSongsAfterById(currentId: Long): Flow<List<Music>>

}


