package com.hmp.data.repository

import com.hmp.data.database.AppDatabase
import com.hmp.data.database.Music
import com.hmp.data.database.MusicExtra
import com.hmp.data.database.UserInfo
import com.hmp.test.db.createTestDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlaylistRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: PlaylistRepositoryImpl

    @BeforeTest
    fun setup() {
        db = createTestDatabase()
        repo = PlaylistRepositoryImpl(db.playlistDao(), db.playlistItemDao(), db.musicAllDao())
    }

    @AfterTest
    fun teardown() { db.close() }

    private suspend fun insertMusic(id: Long, title: String = "Song$id", duration: Long = 100000L) {
        db.musicDao().insert(Music(id = id, title = title, artist = "A", album = "B", duration = duration, path = "/$id.mp3", albumArtUri = ""))
        db.musicExtraDao().insert(MusicExtra(id = id, isGetExtraInfo = true))
        db.userInfoDao().insert(UserInfo(id = id))
    }

    @Test fun createPlaylist_returnsId() = runTest { assertTrue(repo.createPlaylist("P") > 0) }

    @Test fun createAndGetAll() = runTest {
        repo.createPlaylist("A"); repo.createPlaylist("B")
        assertEquals(2, repo.getAllPlaylists().size)
    }

    @Test fun removeByName() = runTest {
        repo.createPlaylist("Del"); repo.createPlaylist("Keep")
        repo.removePlaylist("Del")
        assertEquals(1, repo.getAllPlaylists().size)
        assertEquals("Keep", repo.getAllPlaylists()[0].name)
    }

    @Test fun removeById() = runTest {
        val id = repo.createPlaylist("X"); repo.removePlaylistById(id)
        assertTrue(repo.getAllPlaylists().isEmpty())
    }

    @Test fun rename() = runTest {
        val id = repo.createPlaylist("Old"); repo.renamePlaylist(id, "New")
        assertEquals("New", repo.getPlaylistMeta(id)?.name)
    }

    @Test fun updateCover() = runTest {
        val id = repo.createPlaylist("P"); repo.updatePlaylistCover(id, "c.jpg")
        assertEquals("c.jpg", repo.getPlaylistMeta(id)?.coverUri)
    }

    @Test fun updateDescription() = runTest {
        val id = repo.createPlaylist("P"); repo.updatePlaylistDescription(id, "desc")
        assertEquals("desc", repo.getPlaylistMeta(id)?.description)
    }

    @Test fun setPinned() = runTest {
        val id = repo.createPlaylist("P"); repo.setPlaylistPinned(id, true)
        assertTrue(repo.getPlaylistMeta(id)?.isPinned == true)
    }

    @Test fun addToPlaylist_updatesSongCount() = runTest {
        val pid = repo.createPlaylist("P")
        insertMusic(1); insertMusic(2)
        repo.addToPlaylist(pid, 1, "/1.mp3"); repo.addToPlaylist(pid, 2, "/2.mp3")
        assertEquals(2, repo.getPlaylistMeta(pid)?.songCount)
    }

    @Test fun addToPlaylist_updatesTotalDuration() = runTest {
        val pid = repo.createPlaylist("P")
        insertMusic(1, duration = 60000L); insertMusic(2, duration = 120000L)
        repo.addToPlaylist(pid, 1, "/1.mp3"); repo.addToPlaylist(pid, 2, "/2.mp3")
        assertEquals(180000L, repo.getPlaylistMeta(pid)?.totalDurationMs)
    }

    @Test fun removeItem_updatesCount() = runTest {
        val pid = repo.createPlaylist("P")
        insertMusic(1); insertMusic(2)
        repo.addToPlaylist(pid, 1, "/1.mp3"); repo.addToPlaylist(pid, 2, "/2.mp3")
        repo.removeItemFromPlaylist(1, pid)
        assertEquals(1, repo.getPlaylistMeta(pid)?.songCount)
    }

    @Test fun getMusicInfoInPlaylist() = runTest {
        val pid = repo.createPlaylist("P")
        insertMusic(1, "First"); insertMusic(2, "Second")
        repo.addToPlaylist(pid, 1, "/1.mp3"); repo.addToPlaylist(pid, 2, "/2.mp3")
        val music = repo.getMusicInfoInPlaylist(pid).first()
        assertEquals(2, music.size); assertEquals("First", music[0].music.title)
    }

    @Test fun reorderItems() = runTest {
        val pid = repo.createPlaylist("P")
        insertMusic(1); insertMusic(2); insertMusic(3)
        repo.addToPlaylist(pid, 1, "/1.mp3"); repo.addToPlaylist(pid, 2, "/2.mp3"); repo.addToPlaylist(pid, 3, "/3.mp3")
        repo.reorderPlaylistItems(pid, listOf(3, 1, 2))
        val music = repo.getPlaylistById(pid)
        assertEquals(3L, music[0].music.id); assertEquals(1L, music[1].music.id); assertEquals(2L, music[2].music.id)
    }

    @Test fun incrementPlayCount() = runTest {
        val id = repo.createPlaylist("P")
        repo.incrementPlaylistPlayCount(id); repo.incrementPlaylistPlayCount(id)
        assertEquals(2, repo.getPlaylistMeta(id)?.playCount)
    }

    @Test fun exportAndRestore_roundTrip() = runTest {
        val id = repo.createPlaylist("P")
        insertMusic(1); repo.addToPlaylist(id, 1, "/1.mp3")
        val snap = repo.exportPlaylistsSnapshot()
        assertEquals(1, snap.playlists.size); assertEquals(1, snap.playlistItems.size)
        repo.removePlaylistById(id); assertTrue(repo.getAllPlaylists().isEmpty())
        repo.restoreFromSnapshot(snap)
        assertEquals(1, repo.getAllPlaylists().size); assertEquals("P", repo.getAllPlaylists()[0].name)
    }

    @Test fun getAllPlaylistsFlow() = runTest {
        repo.createPlaylist("A"); repo.createPlaylist("B")
        val flow = repo.getAllPlaylistsFlow().first()
        assertEquals(2, flow.size)
    }

    @Test fun getPlaylistMeta_nonExisting_returnsNull() = runTest {
        assertEquals(null, repo.getPlaylistMeta(999))
    }
}
