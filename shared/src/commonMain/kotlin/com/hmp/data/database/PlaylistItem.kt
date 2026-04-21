package com.hmp.data.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "playlist_item",
    primaryKeys = ["songId", "playlistId"],
    foreignKeys = [
        ForeignKey(
        entity = Playlist::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["playlistId"])]
)

data class PlaylistItem(
    val songUrl: String,
    val songId: Long,
    val playlistId: Long,
    val itemOrder: Int = 0
)

@Dao
interface PlaylistItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PlaylistItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(items: List<PlaylistItem>)

    @Transaction
    @Query("""
        SELECT music.* FROM music
        INNER JOIN playlist_item ON music.id = playlist_item.songId
        WHERE playlist_item.playlistId = :playlistId AND music.isDeleted = 0
        ORDER BY playlist_item.itemOrder ASC
    """)
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>>

    @Transaction
    @Query("""
    SELECT music.* FROM music
    INNER JOIN playlist_item ON music.id = playlist_item.songId
    WHERE playlist_item.playlistId = :playlistId AND music.isDeleted = 0
    ORDER BY playlist_item.itemOrder ASC
""")
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo>

    @Query("SELECT MAX(itemOrder) FROM playlist_item WHERE playlistId = :playlistId")
    suspend fun getMaxOrder(playlistId: Long): Int?

    @Query("DELETE FROM playlist_item WHERE playlistId = :playlistId")
    suspend fun deletePlaylistItem(playlistId: Long)

    @Query("DELETE FROM playlist_item WHERE songId = :musicId AND playlistId = :playlistId")
    suspend fun deleteItemByIds(musicId: Long, playlistId: Long)

    @Query("UPDATE playlist_item SET itemOrder = :itemOrder WHERE songId = :songId AND playlistId = :playlistId")
    suspend fun updateItemOrder(playlistId: Long, songId: Long, itemOrder: Int)

    @Transaction
    suspend fun resetPlaylistItems(playlistId: Long, musicList: List<MusicInfo>) {
        deletePlaylistItem(playlistId)
        val items = musicList.mapIndexed { index, musicInfo ->
            PlaylistItem(
                playlistId = playlistId,
                songId = musicInfo.music.id,
                songUrl = musicInfo.music.path,
                itemOrder = index
            )
        }
        insertPlaylist(items)
    }

    @Query("SELECT * FROM playlist_item")
    suspend fun getAllPlaylistItems(): List<PlaylistItem>

    @Query("DELETE FROM playlist_item")
    suspend fun deleteAll()
}
