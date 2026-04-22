package com.hmp.data.repository

import com.hmp.data.database.MusicAllDao
import com.hmp.data.database.PlaylistDao
import com.hmp.data.database.PlaylistItemDao
import com.hmp.domain.backup.PlaylistsSnapshot
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.playlist.Playlist
import com.hmp.domain.playlist.PlaylistItem
import com.hmp.domain.playlist.PlaylistRepository
import com.hmp.domain.setting.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao,
    private val musicAllDao: MusicAllDao,
    private val settingsRepository: SettingsRepository
) : PlaylistRepository {

    override suspend fun createPlaylist(name: String): Long = 0L

    override suspend fun removePlaylist(name: String) {}

    override suspend fun removePlaylistById(id: Long) {}

    override suspend fun getAllPlaylists(): List<Playlist> = emptyList()

    override suspend fun getPlaylistMeta(id: Long): Playlist? = null

    override suspend fun renamePlaylist(id: Long, newName: String) {}

    override suspend fun updatePlaylistCover(id: Long, coverUri: String?) {}

    override suspend fun updatePlaylistDescription(id: Long, description: String?) {}

    override suspend fun setPlaylistPinned(id: Long, isPinned: Boolean) {}

    override suspend fun incrementPlaylistPlayCount(id: Long) {}

    override suspend fun setPlaylistLastPlayedAt(id: Long, timestamp: Long) {}

    override suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String) {}

    override suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long) {}

    override suspend fun resetPlaylistItems(playlistId: Long, musicList: List<MusicInfo>) {}

    override suspend fun reorderPlaylistItems(playlistId: Long, orderedMusicIds: List<Long>) {}

    override fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>> = flowOf(emptyList())

    override fun getAllPlaylistsFlow(): Flow<List<Playlist>> = flowOf(emptyList())

    override suspend fun getPlaylistById(playlistId: Long): List<MusicInfo> = emptyList()

    override suspend fun getPlaylistByIdList(playlistIdList: List<Long>): List<MusicInfo> = emptyList()

    override suspend fun exportPlaylistsSnapshot(): PlaylistsSnapshot = PlaylistsSnapshot(emptyList(), emptyList())

    override suspend fun restoreFromSnapshot(snapshot: PlaylistsSnapshot) {}
}
