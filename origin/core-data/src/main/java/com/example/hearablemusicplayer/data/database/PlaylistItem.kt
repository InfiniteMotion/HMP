package com.example.hearablemusicplayer.data.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

//PlaylistItem 表:存储播放列表中的歌曲信息,并通过外键关联到 Playlist 表
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
)

@Dao
interface PlaylistItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: PlaylistItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(items: List<PlaylistItem>)

    @Transaction
    @Query("""
        SELECT music.* FROM music 
        INNER JOIN playlist_item ON music.id = playlist_item.songId 
        WHERE playlist_item.playlistId = :playlistId
    """)
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>>

    @Transaction
    @Query("""
    SELECT music.* FROM music 
    INNER JOIN playlist_item ON music.id = playlist_item.songId 
    WHERE playlist_item.playlistId = :playlistId
""")
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo>

//    @Query("DELETE FROM playlist_item WHERE id = :id")
//    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM playlist_item WHERE playlistId = :playlistId")
    suspend fun deletePlaylistItem(playlistId: Long)

    @Query("DELETE FROM playlist_item WHERE songId = :musicId AND playlistId = :playlistId")
    suspend fun deleteItemByIds(musicId: Long, playlistId: Long)

    @Transaction
    suspend fun resetPlaylistItems(playlistId: Long, musicList: List<MusicInfo>) {
        deletePlaylistItem(playlistId)
        val items = musicList.map {
            PlaylistItem(
                playlistId = playlistId,
                songId = it.music.id,
                songUrl = it.music.path,
            )
        }
        insertPlaylist(items)
    }
}
