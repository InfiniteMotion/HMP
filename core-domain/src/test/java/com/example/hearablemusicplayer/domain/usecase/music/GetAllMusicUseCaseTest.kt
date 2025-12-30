package com.example.hearablemusicplayer.domain.usecase.music

import app.cash.turbine.test
import com.example.hearablemusicplayer.data.database.Music
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.repository.MusicRepository
import com.example.hearablemusicplayer.domain.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * GetAllMusicUseCase单元测试
 * 
 * 测试范围：
 * - 获取音乐列表（默认排序和自定义排序）
 * - 获取音乐统计数据
 * - 按歌手查询音乐
 */
@ExperimentalCoroutinesApi
class GetAllMusicUseCaseTest {
    
    @get:Rule
    val coroutineRule = CoroutineTestRule()
    
    private lateinit var musicRepository: MusicRepository
    private lateinit var getAllMusicUseCase: GetAllMusicUseCase
    
    @Before
    fun setup() {
        musicRepository = mockk()
        getAllMusicUseCase = GetAllMusicUseCase(musicRepository)
    }
    
    @Test
    fun `invoke with default sort returns ascending list`() = runTest {
        // Given
        val expectedMusicList = createTestMusicList()
        coEvery { 
            musicRepository.getAllMusicInfoAsList("title", "ASC") 
        } returns expectedMusicList
        
        // When
        val result = getAllMusicUseCase()
        
        // Then
        assertEquals(expectedMusicList, result)
        coVerify { musicRepository.getAllMusicInfoAsList("title", "ASC") }
    }
    
    @Test
    fun `invoke with custom sort by artist ascending returns correct list`() = runTest {
        // Given
        val expectedMusicList = createTestMusicList()
        coEvery { 
            musicRepository.getAllMusicInfoAsList("artist", "ASC") 
        } returns expectedMusicList
        
        // When
        val result = getAllMusicUseCase(orderBy = "artist", orderType = "ASC")
        
        // Then
        assertEquals(expectedMusicList, result)
        coVerify { musicRepository.getAllMusicInfoAsList("artist", "ASC") }
    }
    
    @Test
    fun `invoke with descending sort returns correct list`() = runTest {
        // Given
        val expectedMusicList = createTestMusicList().reversed()
        coEvery { 
            musicRepository.getAllMusicInfoAsList("title", "DESC") 
        } returns expectedMusicList
        
        // When
        val result = getAllMusicUseCase(orderBy = "title", orderType = "DESC")
        
        // Then
        assertEquals(expectedMusicList, result)
        coVerify { musicRepository.getAllMusicInfoAsList("title", "DESC") }
    }
    
    @Test
    fun `invoke with album sort returns correct list`() = runTest {
        // Given
        val expectedMusicList = createTestMusicList()
        coEvery { 
            musicRepository.getAllMusicInfoAsList("album", "ASC") 
        } returns expectedMusicList
        
        // When
        val result = getAllMusicUseCase(orderBy = "album", orderType = "ASC")
        
        // Then
        assertEquals(expectedMusicList, result)
        coVerify { musicRepository.getAllMusicInfoAsList("album", "ASC") }
    }
    
    @Test
    fun `invoke with duration sort returns correct list`() = runTest {
        // Given
        val expectedMusicList = createTestMusicList()
        coEvery { 
            musicRepository.getAllMusicInfoAsList("duration", "DESC") 
        } returns expectedMusicList
        
        // When
        val result = getAllMusicUseCase(orderBy = "duration", orderType = "DESC")
        
        // Then
        assertEquals(expectedMusicList, result)
        coVerify { musicRepository.getAllMusicInfoAsList("duration", "DESC") }
    }
    
    @Test
    fun `invoke returns empty list when no music available`() = runTest {
        // Given
        coEvery { 
            musicRepository.getAllMusicInfoAsList(any(), any()) 
        } returns emptyList()
        
        // When
        val result = getAllMusicUseCase()
        
        // Then
        assertEquals(emptyList<MusicInfo>(), result)
    }
    
    @Test
    fun `getMusicCount returns correct count flow`() = runTest {
        // Given
        val expectedCount = 10
        every { musicRepository.getMusicCount() } returns flowOf(expectedCount)
        
        // When & Then
        getAllMusicUseCase.getMusicCount().test {
            assertEquals(expectedCount, awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `getMusicCount returns zero when no music`() = runTest {
        // Given
        every { musicRepository.getMusicCount() } returns flowOf(0)
        
        // When & Then
        getAllMusicUseCase.getMusicCount().test {
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `getMusicWithExtraCount returns correct count flow`() = runTest {
        // Given
        val expectedCount = 5
        every { musicRepository.getMusicWithExtraCount() } returns flowOf(expectedCount)
        
        // When & Then
        getAllMusicUseCase.getMusicWithExtraCount().test {
            assertEquals(expectedCount, awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `getMusicWithExtraCount returns zero when no music with extra info`() = runTest {
        // Given
        every { musicRepository.getMusicWithExtraCount() } returns flowOf(0)
        
        // When & Then
        getAllMusicUseCase.getMusicWithExtraCount().test {
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `getMusicListByArtist returns correct music list`() = runTest {
        // Given
        val artistName = "测试歌手"
        val expectedMusicList = createTestMusicList().filter { it.music.artist == artistName }
        coEvery { 
            musicRepository.getMusicListByArtist(artistName) 
        } returns expectedMusicList
        
        // When
        val result = getAllMusicUseCase.getMusicListByArtist(artistName)
        
        // Then
        assertEquals(expectedMusicList, result)
        coVerify { musicRepository.getMusicListByArtist(artistName) }
    }
    
    @Test
    fun `getMusicListByArtist returns empty list for non-existent artist`() = runTest {
        // Given
        val artistName = "不存在的歌手"
        coEvery { 
            musicRepository.getMusicListByArtist(artistName) 
        } returns emptyList()
        
        // When
        val result = getAllMusicUseCase.getMusicListByArtist(artistName)
        
        // Then
        assertEquals(emptyList<MusicInfo>(), result)
        coVerify { musicRepository.getMusicListByArtist(artistName) }
    }
    
    @Test
    fun `getMusicListByArtist returns all music for specific artist`() = runTest {
        // Given
        val artistName = "歌手A"
        val allMusic = listOf(
            createMusicInfo(1, "歌曲1", "歌手A"),
            createMusicInfo(2, "歌曲2", "歌手B"),
            createMusicInfo(3, "歌曲3", "歌手A"),
            createMusicInfo(4, "歌曲4", "歌手A")
        )
        val expectedList = allMusic.filter { it.music.artist == artistName }
        coEvery { 
            musicRepository.getMusicListByArtist(artistName) 
        } returns expectedList
        
        // When
        val result = getAllMusicUseCase.getMusicListByArtist(artistName)
        
        // Then
        assertEquals(3, result.size)
        assertEquals(expectedList, result)
    }
    
    // 辅助方法：创建测试用的音乐列表
    private fun createTestMusicList(): List<MusicInfo> {
        return listOf(
            createMusicInfo(1, "歌曲A", "测试歌手", "专辑1"),
            createMusicInfo(2, "歌曲B", "测试歌手", "专辑2"),
            createMusicInfo(3, "歌曲C", "其他歌手", "专辑1")
        )
    }
    
    // 辅助方法：创建单个MusicInfo对象
    private fun createMusicInfo(
        id: Long,
        title: String,
        artist: String = "测试歌手",
        album: String = "测试专辑"
    ): MusicInfo {
        val music = Music(
            id = id,
            path = "/storage/emulated/0/Music/song_$id.mp3",
            title = title,
            artist = artist,
            album = album,
            duration = 180000L,
            albumArtUri = ""
        )
        return MusicInfo(
            music = music,
            extra = null,
            userInfo = null
        )
    }
}
