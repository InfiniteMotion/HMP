package com.example.hearablemusicplayer.domain.playlist

import com.example.hearablemusicplayer.domain.music.MusicInfo
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    // Create/Delete Playlist
    suspend fun createPlaylist(name: String): Long
    suspend fun removePlaylist(name: String)
    suspend fun removePlaylistById(id: Long)

    // Playlist metadata and rename
    suspend fun getAllPlaylists(): List<Playlist>
    suspend fun getPlaylistMeta(id: Long): Playlist?
    suspend fun renamePlaylist(id: Long, newName: String)
    suspend fun updatePlaylistCover(id: Long, coverUri: String?)
    suspend fun updatePlaylistDescription(id: Long, description: String?)
    suspend fun setPlaylistPinned(id: Long, isPinned: Boolean)
    suspend fun incrementPlaylistPlayCount(id: Long)
    suspend fun setPlaylistLastPlayedAt(id: Long, timestamp: Long)

    // Manage Items
    suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String)
    suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long)
    suspend fun resetPlaylistItems(playlistId: Long, musicList: List<MusicInfo>)
    suspend fun reorderPlaylistItems(playlistId: Long, orderedMusicIds: List<Long>)

    // Query
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>>
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo>
    suspend fun getPlaylistByIdList(playlistIdList: List<Long>): List<MusicInfo>

    // Snapshot Export/Import
    suspend fun exportPlaylistsSnapshot(): com.example.hearablemusicplayer.domain.backup.PlaylistsSnapshot
    suspend fun restoreFromSnapshot(snapshot: com.example.hearablemusicplayer.domain.backup.PlaylistsSnapshot)
}