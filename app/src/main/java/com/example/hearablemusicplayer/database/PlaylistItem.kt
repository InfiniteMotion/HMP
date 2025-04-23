package com.example.hearablemusicplayer.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

//PlaylistItem 表：存储播放列表中的歌曲信息，并通过外键关联到 Playlist 表
@Entity(tableName = "playlist_item", foreignKeys = [
    ForeignKey(
        entity = Playlist::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["playlistId"])]
)
data class PlaylistItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songUrl: String,
    val songId: Long,
    val playlistId: Long, // 外键，关联到 Playlist 表
    val playedAt: Long    // 时间戳
)

@Dao
interface PlaylistItemDao {
    @Insert
    suspend fun insert(item: PlaylistItem): Long

    @Insert
    suspend fun insertPlaylist(items: List<PlaylistItem>)

    @Query("""
        SELECT music.* FROM music 
        INNER JOIN playlist_item ON music.id = playlist_item.songId 
        WHERE playlist_item.playlistId = :playlistId
    """)
    fun getMusicInPlaylist(playlistId: Long): Flow<List<Music>>

    @Query("""
    SELECT music.* FROM music 
    INNER JOIN playlist_item ON music.id = playlist_item.songId 
    WHERE playlist_item.playlistId = :playlistId
    ORDER BY playlist_item.id DESC
    LIMIT :limit
""")
    fun getMusicInPlaylistLimit(playlistId: Long,limit:Int): Flow<List<Music>>

    @Query("DELETE FROM playlist_item WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM playlist_item WHERE playlistId = :playlistId")
    suspend fun deletePlaylistItem(playlistId: Long)

    @Query("DELETE FROM playlist_item WHERE songId = :musicId AND playlistId = :playlistId")
    suspend fun deleteItemByIds(musicId: Long, playlistId: Long)

    @Transaction
    suspend fun resetPlaylistItems(playlistId: Long, musicList: List<Music>) {
        deletePlaylistItem(playlistId)
        val items = musicList.map {
            PlaylistItem(
                playlistId = playlistId,
                songId = it.id,
                songUrl = it.path,
                playedAt = System.currentTimeMillis()
            )
        }
        insertPlaylist(items)
    }
}