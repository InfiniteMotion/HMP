package com.hmp.domain.music.usecase

import com.hmp.domain.music.Music
import com.hmp.domain.music.MusicInfo
import com.hmp.test.fakes.FakeMusicRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchMusicUseCaseTest {

    private val repository = FakeMusicRepository()
    private val useCase = SearchMusicUseCase(repository)

    private fun music(id: Long, title: String, artist: String = "Artist", album: String = "Album") =
        MusicInfo(music = Music(id = id, title = title, artist = artist, album = album, duration = 180000, path = "/music/$id.mp3", albumArtUri = ""), extra = null, userInfo = null)

    @Test
    fun invoke_emptyRepository_returnsEmpty() = runTest {
        val result = useCase("test")
        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_matchesTitle() = runTest {
        repository.addMusic(music(1, "Hello World"))
        repository.addMusic(music(2, "Goodbye"))
        val result = useCase("hello")
        assertEquals(1, result.size)
        assertEquals("Hello World", result[0].music.title)
    }

    @Test
    fun invoke_matchesArtist() = runTest {
        repository.addMusic(music(1, "Song A", artist = "Taylor Swift"))
        repository.addMusic(music(2, "Song B", artist = "Adele"))
        val result = useCase("taylor")
        assertEquals(1, result.size)
        assertEquals("Taylor Swift", result[0].music.artist)
    }

    @Test
    fun invoke_matchesAlbum() = runTest {
        repository.addMusic(music(1, "Song A", album = "Thriller"))
        val result = useCase("thriller")
        assertEquals(1, result.size)
    }

    @Test
    fun invoke_caseInsensitive() = runTest {
        repository.addMusic(music(1, "Hello World"))
        val result = useCase("HELLO")
        assertEquals(1, result.size)
    }

    @Test
    fun invoke_multipleMatches() = runTest {
        repository.addMusic(music(1, "Love Song"))
        repository.addMusic(music(2, "Love Story"))
        repository.addMusic(music(3, "Hate"))
        val result = useCase("love")
        assertEquals(2, result.size)
    }
}
