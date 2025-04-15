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
    val albumArtUri: String?
)

@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(musics: List<Music>)

    @Query("DELETE FROM music")
    suspend fun deleteAllMusic()

    @Query("SELECT * FROM music ORDER BY title ASC")
    fun getAll(): LiveData<List<Music>>

    @Query("SELECT * FROM music WHERE id = :musicId")
    fun getMusicById(musicId: String): Flow<Music?>

    @Query("SELECT * FROM music ORDER BY RANDOM() LIMIT 5")
    suspend fun getRandomMusic(): List<Music>

    @Query("SELECT * FROM music WHERE title LIKE :query OR artist LIKE :query")
    fun searchMusic(query: String): List<Music>
}

@Database(entities = [Music::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}