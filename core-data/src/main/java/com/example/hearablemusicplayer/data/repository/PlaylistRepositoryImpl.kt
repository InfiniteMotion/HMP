package com.example.hearablemusicplayer.data.repository

import com.example.hearablemusicplayer.data.database.MusicAllDao
import com.example.hearablemusicplayer.data.database.PlaylistDao
import com.example.hearablemusicplayer.data.database.PlaylistItemDao
import com.example.hearablemusicplayer.data.mapper.toDomain
import com.example.hearablemusicplayer.data.mapper.toEntity
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.model.Playlist
import com.example.hearablemusicplayer.domain.model.PlaylistItem
import com.example.hearablemusicplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao,
    private val musicAllDao: MusicAllDao
) : PlaylistRepository {

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insert(Playlist(name = name).toEntity())
    }

    override suspend fun removePlaylist(name: String) {
        playlistDao.deletePlaylist(name = name)
    }

    override suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String) {
        val maxOrder = playlistItemDao.getMaxOrder(playlistId) ?: -1
        val item = PlaylistItem(
            songUrl = musicPath,
            songId = musicId,
            playlistId = playlistId
        )
        playlistItemDao.insert(item.toEntity(itemOrder = maxOrder + 1))
    }

    override suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long) {
        playlistItemDao.deleteItemByIds(musicId, playlistId)
    }

    override suspend fun resetPlaylistItems(playlistId: Long, musicList: List<MusicInfo>) {
        playlistItemDao.resetPlaylistItems(playlistId, musicList.map { it.toEntity() })
    }

    override fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>> {
        return playlistItemDao.getMusicInfoInPlaylist(playlistId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getPlaylistById(playlistId: Long): List<MusicInfo> {
        return playlistItemDao.getPlaylistById(playlistId).map { it.toDomain() }
    }

    override suspend fun getPlaylistByIdList(playlistIdList: List<Long>): List<MusicInfo> {
        return musicAllDao.getPlaylistByIdList(playlistIdList).map { it.toDomain() }
    }
}
