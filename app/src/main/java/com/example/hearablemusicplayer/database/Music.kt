package com.example.hearablemusicplayer.database

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "music")
data class Music(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
)

@Entity(tableName = "musicExtra")
data class MusicExtra(
    @PrimaryKey val id: Long,
    val albumArtUri: String? = null,
    val lyrics: String? = null,
    val bitRate: Int? = null,           // 比特率 kbps
    val sampleRate: Int? = null,        // 采样率 Hz
    val fileSize: Long? = null,         // 文件大小 Byte
    val format: String? = null,         // 文件格式 mp3/flac
    val language: String? = null,       // 语言
    val year: Int? = null,              // 年份
    val recommendationIds: String? = null  // 推荐关联的音乐ID列表
)

@Entity(tableName = "userInfo")
data class UserInfo(
    @PrimaryKey val id: Long,
    val liked: Boolean = false,
    val disLiked: Boolean = false,
    val lastPlayed: Int? = null,
    val playCount: Int? = null,
    val skippedCount: Int? = null,
    val userRating: Int? = null,
    val inCustomPlaylistCount: Int? = null,
)

data class MusicInfo(
    @Embedded val music: Music,

    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val extra: MusicExtra?,

    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val userInfo: UserInfo?
)

data class MusicAll(
    val music: Music,
    val extra: MusicExtra?,
    val userInfo: UserInfo?
)

@Dao
interface MusicDao {

    // 批量插入音乐数据,如果主键冲突则替换已有数据
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(musics: List<Music>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(music: Music)

    @Query("SELECT * FROM music WHERE id = :id")
    fun getMusicById(id: Long): Flow<Music?>

    // 删除
    @Query("DELETE FROM music WHERE id IN (:ids)")
    suspend fun deleteMusicByIds(ids: List<Long>)

    // 清空音乐表中所有数据
    @Query("DELETE FROM music")
    suspend fun deleteAll()

//    // 根据 ID 获取音乐文件的路径
//    @Query("SELECT path FROM music WHERE id = :musicId")
//    suspend fun getMusicPathById(musicId: Long): String
//
//    // 随机获取 5 首音乐（适用于“猜你喜欢”等功能）
//    @Query("SELECT * FROM music ORDER BY RANDOM() LIMIT 5")
//    suspend fun getRandomMusic(): List<Music>

//    // 获取当前歌曲之后的所有歌曲，按 ID 顺序排列（用于自动加入播放列表）
//    @Query("SELECT * FROM music WHERE id > :currentId ORDER BY id ASC")
//    fun getSongsAfterById(currentId: Long): Flow<List<Music>>

}

@Dao
interface MusicExtraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(extras: List<MusicExtra>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(extra: MusicExtra)

    @Query("SELECT * FROM musicExtra WHERE id = :id")
    suspend fun getExtraById(id: Long): MusicExtra?

    @Query("SELECT lyrics FROM musicExtra WHERE id = :id")
    suspend fun getLyricsById(id: Long): String?

    @Query("DELETE FROM musicExtra WHERE id IN (:ids)")
    suspend fun deleteExtraByIds(ids: List<Long>)

    @Query("DELETE FROM musicExtra")
    suspend fun deleteAll()
}

@Dao
interface UserInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(userInfos: List<UserInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userInfo: UserInfo)

    //更新某首歌的红心（喜欢）状态
    @Query("UPDATE userInfo SET liked = :liked WHERE id = :id")
    suspend fun updateLikedStatus(id: Long, liked: Boolean)

    @Query("SELECT * FROM userInfo WHERE id = :id")
    suspend fun getUserInfoById(id: Long): UserInfo?

    @Query("SELECT liked FROM userInfo WHERE id = :id")
    suspend fun getLikedStatus(id: Long): Boolean

    @Query("DELETE FROM userInfo WHERE id IN (:ids)")
    suspend fun deleteUserInfoByIds(ids: List<Long>)

    // 清空音乐表中所有数据
    @Query("DELETE FROM userInfo")
    suspend fun deleteAll()
}

@Dao
interface MusicAllDao {

    @Transaction
    @Query("SELECT * FROM music")
    suspend fun getAllMusicInfo(): List<MusicInfo>

    @Transaction
    @Query("SELECT * FROM music")
    fun getAllMusicInfoAsFlow(): Flow<List<MusicInfo>>

    @Transaction
    @Query("SELECT * FROM music WHERE id = :id")
    fun getMusicInfoById(id: Long): Flow<MusicInfo?>

    // 搜索音乐标题或艺术家名中包含关键词的记录
    @Transaction
    @Query("SELECT * FROM music WHERE title LIKE :query OR artist LIKE :query")
    fun searchMusic(query: String): List<MusicInfo>

    @Transaction
    @Query("SELECT * FROM music ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomMusicInfo(): MusicInfo?
}
