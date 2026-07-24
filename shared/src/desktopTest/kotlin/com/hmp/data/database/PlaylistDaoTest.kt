package com.hmp.data.database

import com.hmp.test.db.createTestDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlaylistDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var playlistDao: PlaylistDao
    private lateinit var playlistItemDao: PlaylistItemDao
    private lateinit var musicDao: MusicDao

    @BeforeTest
    fun setup() {
        db = createTestDatabase()
        playlistDao = db.playlistDao()
        playlistItemDao = db.playlistItemDao()
        musicDao = db.musicDao()
    }

    @AfterTest
    fun teardown() {
        db.close()
    }

    private fun music(id: Long, title: String = "Song$id", path: String = "/$id.mp3") =
        Music(id = id, title = title, artist = "A", album = "B", duration = 100L, path = path, albumArtUri = "")

    // ===== PlaylistDao =====

    @Test
    fun insert_returnsId() = runTest {
        val id = playlistDao.insert(Playlist(name = "My Playlist"))
        assertTrue(id > 0)
    }

    @Test
    fun getPlaylistById() = runTest {
        val id = playlistDao.insert(Playlist(name = "Test"))
        val result = playlistDao.getPlaylistById(id)
        assertNotNull(result)
        assertEquals("Test", result.name)
    }

    @Test
    fun getAllPlaylists() = runTest {
        playlistDao.insert(Playlist(name = "A"))
        playlistDao.insert(Playlist(name = "B"))
        val all = playlistDao.getAllPlaylists()
        assertEquals(2, all.size)
    }

    @Test
    fun deletePlaylist_byName() = runTest {
        playlistDao.insert(Playlist(name = "ToDelete"))
        playlistDao.insert(Playlist(name = "ToKeep"))
        playlistDao.deletePlaylist("ToDelete")
        val all = playlistDao.getAllPlaylists()
        assertEquals(1, all.size)
        assertEquals("ToKeep", all[0].name)
    }

    @Test
    fun deletePlaylistById() = runTest {
        val id = playlistDao.insert(Playlist(name = "Test"))
        playlistDao.deletePlaylistById(id)
        assertNull(playlistDao.getPlaylistById(id))
    }

    @Test
    fun renamePlaylist() = runTest {
        val id = playlistDao.insert(Playlist(name = "Old"))
        playlistDao.renamePlaylist(id, "New", 1000L)
        assertEquals("New", playlistDao.getPlaylistById(id)?.name)
    }

    @Test
    fun updateCover() = runTest {
        val id = playlistDao.insert(Playlist(name = "Test"))
        playlistDao.updateCover(id, "cover.jpg", 1000L)
        assertEquals("cover.jpg", playlistDao.getPlaylistById(id)?.coverUri)
    }

    @Test
    fun incrementPlayCount() = runTest {
        val id = playlistDao.insert(Playlist(name = "Test", playCount = 0))
        playlistDao.incrementPlayCount(id)
        playlistDao.incrementPlayCount(id)
        assertEquals(2, playlistDao.getPlaylistById(id)?.playCount)
    }

    @Test
    fun setPinned() = runTest {
        val id = playlistDao.insert(Playlist(name = "Test"))
        playlistDao.setPinned(id, true, 1000L)
        assertTrue(playlistDao.getPlaylistById(id)?.isPinned == true)
    }

    @Test
    fun updateStats() = runTest {
        val id = playlistDao.insert(Playlist(name = "Test"))
        playlistDao.updateStats(id, 10, 300000L, 1000L)
        val result = playlistDao.getPlaylistById(id)!!
        assertEquals(10, result.songCount)
        assertEquals(300000L, result.totalDurationMs)
    }

    @Test
    fun getAllPlaylistsFlow() = runTest {
        playlistDao.insert(Playlist(name = "A"))
        playlistDao.insert(Playlist(name = "B"))
        val flow = playlistDao.getAllPlaylistsFlow().first()
        assertEquals(2, flow.size)
    }

    // ===== PlaylistItemDao =====

    @Test
    fun item_insertAndRetrieve() = runTest {
        val playlistId = playlistDao.insert(Playlist(name = "Test"))
        musicDao.insert(music(1))
        playlistItemDao.insert(PlaylistItem(songUrl = "/1.mp3", songId = 1, playlistId = playlistId, itemOrder = 0))
        val items = playlistItemDao.getPlaylistById(playlistId)
        assertEquals(1, items.size)
        assertEquals(1L, items[0].music.id)
    }

    @Test
    fun item_getMaxOrder() = runTest {
        val playlistId = playlistDao.insert(Playlist(name = "Test"))
        musicDao.insertAll(listOf(music(1), music(2), music(3)))
        playlistItemDao.insert(PlaylistItem(songUrl = "/1.mp3", songId = 1, playlistId = playlistId, itemOrder = 0))
        playlistItemDao.insert(PlaylistItem(songUrl = "/2.mp3", songId = 2, playlistId = playlistId, itemOrder = 1))
        playlistItemDao.insert(PlaylistItem(songUrl = "/3.mp3", songId = 3, playlistId = playlistId, itemOrder = 2))
        assertEquals(2, playlistItemDao.getMaxOrder(playlistId))
    }

    @Test
    fun item_deleteItemByIds() = runTest {
        val playlistId = playlistDao.insert(Playlist(name = "Test"))
        musicDao.insertAll(listOf(music(1), music(2)))
        playlistItemDao.insert(PlaylistItem(songUrl = "/1.mp3", songId = 1, playlistId = playlistId, itemOrder = 0))
        playlistItemDao.insert(PlaylistItem(songUrl = "/2.mp3", songId = 2, playlistId = playlistId, itemOrder = 1))
        playlistItemDao.deleteItemByIds(1, playlistId)
        val items = playlistItemDao.getPlaylistById(playlistId)
        assertEquals(1, items.size)
        assertEquals(2L, items[0].music.id)
    }

    @Test
    fun item_updateItemOrder() = runTest {
        val playlistId = playlistDao.insert(Playlist(name = "Test"))
        musicDao.insertAll(listOf(music(1), music(2)))
        playlistItemDao.insert(PlaylistItem(songUrl = "/1.mp3", songId = 1, playlistId = playlistId, itemOrder = 0))
        playlistItemDao.insert(PlaylistItem(songUrl = "/2.mp3", songId = 2, playlistId = playlistId, itemOrder = 1))
        playlistItemDao.updateItemOrder(playlistId, 2, 0)
        playlistItemDao.updateItemOrder(playlistId, 1, 1)
        // Order should now be: song2 (order=0), song1 (order=1)
        val items = playlistItemDao.getPlaylistById(playlistId)
        assertEquals(2L, items[0].music.id)
        assertEquals(1L, items[1].music.id)
    }

    @Test
    fun item_cascadeDelete_onPlaylistDelete() = runTest {
        val playlistId = playlistDao.insert(Playlist(name = "Test"))
        musicDao.insert(music(1))
        playlistItemDao.insert(PlaylistItem(songUrl = "/1.mp3", songId = 1, playlistId = playlistId, itemOrder = 0))
        playlistDao.deletePlaylistById(playlistId)
        val items = playlistItemDao.getPlaylistById(playlistId)
        assertTrue(items.isEmpty())
    }

    @Test
    fun item_getAllPlaylistItems() = runTest {
        val p1 = playlistDao.insert(Playlist(name = "A"))
        val p2 = playlistDao.insert(Playlist(name = "B"))
        musicDao.insertAll(listOf(music(1), music(2), music(3)))
        playlistItemDao.insert(PlaylistItem(songUrl = "/1.mp3", songId = 1, playlistId = p1, itemOrder = 0))
        playlistItemDao.insert(PlaylistItem(songUrl = "/2.mp3", songId = 2, playlistId = p2, itemOrder = 0))
        playlistItemDao.insert(PlaylistItem(songUrl = "/3.mp3", songId = 3, playlistId = p1, itemOrder = 1))
        assertEquals(3, playlistItemDao.getAllPlaylistItems().size)
    }
}
