package com.hmp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.datetime.Instant

@Entity(tableName = "music")
data class Music(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "artist") val artist: String,
    @ColumnInfo(name = "album") val album: String,
    @ColumnInfo(name = "duration") val duration: Long,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "size") val size: Long,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "modified_at") val modifiedAt: Instant,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "play_count") val playCount: Int = 0,
    @ColumnInfo(name = "last_played") val lastPlayed: Instant? = null
)

@Entity(tableName = "music_extra")
data class MusicExtra(
    @PrimaryKey val musicId: String,
    @ColumnInfo(name = "lyrics") val lyrics: String? = null,
    @ColumnInfo(name = "cover_uri") val coverUri: String? = null,
    @ColumnInfo(name = "genre") val genre: String? = null,
    @ColumnInfo(name = "year") val year: Int? = null,
    @ColumnInfo(name = "bitrate") val bitrate: Int? = null,
    @ColumnInfo(name = "sample_rate") val sampleRate: Int? = null
)

@Entity(tableName = "user_info")
data class UserInfo(
    @PrimaryKey val id: String = "user",
    @ColumnInfo(name = "name") val name: String? = null,
    @ColumnInfo(name = "email") val email: String? = null,
    @ColumnInfo(name = "last_sync") val lastSync: Instant? = null,
    @ColumnInfo(name = "preferences") val preferences: String? = null
)

@Dao
interface MusicDao {
    @Insert
    suspend fun insert(music: Music)

    @Insert
    suspend fun insertAll(musicList: List<Music>)

    @Update
    suspend fun update(music: Music)

    @Delete
    suspend fun delete(music: Music)

    @Query("SELECT * FROM music")
    suspend fun getAll(): List<Music>

    @Query("SELECT * FROM music WHERE id = :id")
    suspend fun getById(id: String): Music?

    @Query("SELECT * FROM music WHERE is_favorite = 1")
    suspend fun getFavorites(): List<Music>

    @Query("UPDATE music SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE music SET play_count = play_count + 1, last_played = :lastPlayed WHERE id = :id")
    suspend fun updatePlayCount(id: String, lastPlayed: Instant)
}

@Dao
interface MusicExtraDao {
    @Insert
    suspend fun insert(musicExtra: MusicExtra)

    @Update
    suspend fun update(musicExtra: MusicExtra)

    @Delete
    suspend fun delete(musicExtra: MusicExtra)

    @Query("SELECT * FROM music_extra WHERE music_id = :musicId")
    suspend fun getByMusicId(musicId: String): MusicExtra?
}

@Dao
interface UserInfoDao {
    @Insert
    suspend fun insert(userInfo: UserInfo)

    @Update
    suspend fun update(userInfo: UserInfo)

    @Query("SELECT * FROM user_info WHERE id = :id")
    suspend fun getById(id: String): UserInfo?

    @Query("SELECT * FROM user_info LIMIT 1")
    suspend fun getFirst(): UserInfo?
}