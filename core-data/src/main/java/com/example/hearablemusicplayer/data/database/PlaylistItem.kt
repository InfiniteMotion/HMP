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
    val itemOrder: Int = 0 // 添加排序字段，默认值为0
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
        WHERE playlist_item.playlistId = :playlistId
        ORDER BY playlist_item.itemOrder ASC
    """)
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>>

    @Transaction
    @Query("""
    SELECT music.* FROM music 
    INNER JOIN playlist_item ON music.id = playlist_item.songId 
    WHERE playlist_item.playlistId = :playlistId
    ORDER BY playlist_item.itemOrder ASC
""")
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo>

    @Query("SELECT MAX(itemOrder) FROM playlist_item WHERE playlistId = :playlistId")
    suspend fun getMaxOrder(playlistId: Long): Int?

//    @Query("DELETE FROM playlist_item WHERE id = :id")
//    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM playlist_item WHERE playlistId = :playlistId")
    suspend fun deletePlaylistItem(playlistId: Long)

    @Query("DELETE FROM playlist_item WHERE songId = :musicId AND playlistId = :playlistId")
    suspend fun deleteItemByIds(musicId: Long, playlistId: Long)

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
}
