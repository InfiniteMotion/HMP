package com.hmp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    // Playlist operations
    @Insert
    suspend fun insertPlaylist(playlist: PlayList): Long

    @Update
    suspend fun updatePlaylist(playlist: PlayList)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Int)

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlayList>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Int): PlayList?

    @Query("SELECT * FROM playlists WHERE isSystem = 1")
    fun getSystemPlaylists(): Flow<List<PlayList>>

    // PlaylistItem operations
    @Insert
    suspend fun insertPlaylistItem(item: PlaylistItem): Long

    @Update
    suspend fun updatePlaylistItem(item: PlaylistItem)

    @Query("DELETE FROM playlist_items WHERE id = :id")
    suspend fun deletePlaylistItem(id: Int)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun deleteAllPlaylistItems(playlistId: Int)

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getPlaylistItems(playlistId: Int): Flow<List<PlaylistItem>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId AND musicId = :musicId")
    suspend fun getPlaylistItemByMusicId(playlistId: Int, musicId: String): PlaylistItem?

    @Query("UPDATE playlist_items SET position = position - 1 WHERE playlistId = :playlistId AND position > :position")
    suspend fun updatePositionsAfterDeletion(playlistId: Int, position: Int)

    @Query("UPDATE playlist_items SET position = :newPosition WHERE playlistId = :playlistId AND id = :itemId")
    suspend fun updateItemPosition(playlistId: Int, itemId: Int, newPosition: Int)
}
