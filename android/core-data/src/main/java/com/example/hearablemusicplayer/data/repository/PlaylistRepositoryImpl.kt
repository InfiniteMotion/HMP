package com.example.hearablemusicplayer.data.repository

import com.example.hearablemusicplayer.data.database.MusicAllDao
import com.example.hearablemusicplayer.data.database.PlaylistDao
import com.example.hearablemusicplayer.data.database.PlaylistItemDao
import com.example.hearablemusicplayer.data.mapper.toDomain
import com.example.hearablemusicplayer.data.mapper.toEntity
import com.example.hearablemusicplayer.domain.backup.PlaylistsSnapshot
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.playlist.Playlist
import com.example.hearablemusicplayer.domain.playlist.PlaylistItem
import com.example.hearablemusicplayer.domain.playlist.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao,
    private val musicAllDao: MusicAllDao,
    private val settingsRepository: com.example.hearablemusicplayer.domain.setting.SettingsRepository
) : PlaylistRepository {

    private suspend fun isCustomPlaylist(playlistId: Long): Boolean {
        val currentId = settingsRepository.getCurrentPlaylistId()
        val likedId = settingsRepository.getLikedPlaylistId()
        val recentId = settingsRepository.getRecentPlaylistId()
        return playlistId != currentId && playlistId != likedId && playlistId != recentId
    }

    override suspend fun createPlaylist(name: String): Long {
        val now = System.currentTimeMillis()
        val entity = Playlist(
            name = name,
            createdAt = now,
            updatedAt = now
        ).toEntity()
        return playlistDao.insert(entity)
    }

    override suspend fun removePlaylist(name: String) {
        playlistDao.deletePlaylist(name = name)
    }

    override suspend fun removePlaylistById(id: Long) {
        playlistDao.deletePlaylistById(id)
    }

    override suspend fun getAllPlaylists(): List<Playlist> {
        return playlistDao.getAllPlaylists().map { it.toDomain() }
    }

    override suspend fun getPlaylistMeta(id: Long): Playlist? {
        return playlistDao.getPlaylistById(id)?.toDomain()
    }

    override suspend fun renamePlaylist(id: Long, newName: String) {
        playlistDao.renamePlaylist(id, newName, System.currentTimeMillis())
    }

    override suspend fun updatePlaylistCover(id: Long, coverUri: String?) {
        playlistDao.updateCover(id, coverUri, System.currentTimeMillis())
    }

    override suspend fun updatePlaylistDescription(id: Long, description: String?) {
        playlistDao.updateDescription(id, description, System.currentTimeMillis())
    }

    override suspend fun setPlaylistPinned(id: Long, isPinned: Boolean) {
        playlistDao.setPinned(id, isPinned, System.currentTimeMillis())
    }

    override suspend fun incrementPlaylistPlayCount(id: Long) {
        playlistDao.incrementPlayCount(id)
    }

    override suspend fun setPlaylistLastPlayedAt(id: Long, timestamp: Long) {
        playlistDao.setLastPlayedAt(id, timestamp)
    }

    private suspend fun refreshPlaylistStats(playlistId: Long) {
        val list = playlistItemDao.getPlaylistById(playlistId)
        val songCount = list.size
        val totalDurationMs = list.sumOf { it.music.duration }
        playlistDao.updateStats(playlistId, songCount, totalDurationMs, System.currentTimeMillis())
        
        if (isCustomPlaylist(playlistId)) {
            val firstSongCover = list.firstOrNull()?.music?.albumArtUri
            playlistDao.updateCover(playlistId, firstSongCover, System.currentTimeMillis())
        }
    }

    override suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String) {
        val maxOrder = playlistItemDao.getMaxOrder(playlistId) ?: -1
        val item = PlaylistItem(
            songUrl = musicPath,
            songId = musicId,
            playlistId = playlistId
        )
        playlistItemDao.insert(item.toEntity(itemOrder = maxOrder + 1))
        refreshPlaylistStats(playlistId)
    }

    override suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long) {
        playlistItemDao.deleteItemByIds(musicId, playlistId)
        refreshPlaylistStats(playlistId)
    }

    override suspend fun resetPlaylistItems(playlistId: Long, musicList: List<MusicInfo>) {
        playlistItemDao.resetPlaylistItems(playlistId, musicList.map { it.toEntity() })
        refreshPlaylistStats(playlistId)
    }

    override suspend fun reorderPlaylistItems(playlistId: Long, orderedMusicIds: List<Long>) {
        orderedMusicIds.forEachIndexed { index, songId ->
            playlistItemDao.updateItemOrder(playlistId, songId, index)
        }
        refreshPlaylistStats(playlistId)
    }

    override fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>> {
        return playlistItemDao.getMusicInfoInPlaylist(playlistId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllPlaylistsFlow(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylistsFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getPlaylistById(playlistId: Long): List<MusicInfo> {
        return playlistItemDao.getPlaylistById(playlistId).map { it.toDomain() }
    }

    override suspend fun getPlaylistByIdList(playlistIdList: List<Long>): List<MusicInfo> {
        return musicAllDao.getPlaylistByIdList(playlistIdList).map { it.toDomain() }
    }

    // ==================== Snapshot Export/Import ====================
    override suspend fun exportPlaylistsSnapshot(): PlaylistsSnapshot {
        val playlists = playlistDao.getAllPlaylists().map { it.toDomain() }
        val items = playlistItemDao.getAllPlaylistItems().map {
            PlaylistItem(
                songUrl = it.songUrl,
                songId = it.songId,
                playlistId = it.playlistId
            )
        }
        return PlaylistsSnapshot(
            playlists = playlists,
            playlistItems = items
        )
    }

    override suspend fun restoreFromSnapshot(snapshot: PlaylistsSnapshot) {
        playlistDao.deleteAll()
        playlistItemDao.deleteAll()
        val playlists = snapshot.playlists.map { it.toEntity() }
        playlistDao.insertAll(playlists)
        
        // Restore items
        // Group by playlistId to assign correct order
        val groupedItems = snapshot.playlistItems.groupBy { it.playlistId }
        val finalItems = groupedItems.flatMap { (_, list) ->
            list.mapIndexed { index, it ->
                com.example.hearablemusicplayer.data.database.PlaylistItem(
                    songUrl = it.songUrl,
                    songId = it.songId,
                    playlistId = it.playlistId,
                    itemOrder = index
                )
            }
        }
        
        playlistItemDao.insertPlaylist(finalItems)
    }
}
