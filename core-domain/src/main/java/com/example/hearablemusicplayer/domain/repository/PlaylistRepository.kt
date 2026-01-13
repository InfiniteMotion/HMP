package com.example.hearablemusicplayer.domain.repository

import com.example.hearablemusicplayer.domain.model.MusicInfo
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    // Create/Delete Playlist
    suspend fun createPlaylist(name: String): Long
    suspend fun removePlaylist(name: String)
    
    // Manage Items
    suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String)
    suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long)
    suspend fun resetPlaylistItems(playlistId: Long, musicList: List<MusicInfo>)
    
    // Query
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>>
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo>
    suspend fun getPlaylistByIdList(playlistIdList: List<Long>): List<MusicInfo>
}
