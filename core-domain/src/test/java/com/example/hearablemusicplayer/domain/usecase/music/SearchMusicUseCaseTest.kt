package com.example.hearablemusicplayer.domain.usecase.music

import com.example.hearablemusicplayer.data.database.Music
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.repository.MusicRepository
import com.example.hearablemusicplayer.domain.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * SearchMusicUseCase单元测试
 * 
 * 测试范围：
 * - 关键词搜索音乐
 * - 处理空关键词
 * - 处理特殊字符
 * - 处理无结果情况
 */
@ExperimentalCoroutinesApi
class SearchMusicUseCaseTest {
    
    @get:Rule
    val coroutineRule = CoroutineTestRule()
    
    private lateinit var musicRepository: MusicRepository
    private lateinit var searchMusicUseCase: SearchMusicUseCase
    
    @Before
    fun setup() {
        musicRepository = mockk()
        searchMusicUseCase = SearchMusicUseCase(musicRepository)
    }
    
    @Test
    fun `invoke with title keyword returns matching music`() = runTest {
        // Given
        val query = "歌曲A"
        val expectedMusicList = listOf(
            createMusicInfo(1, "歌曲A", "歌手1"),
            createMusicInfo(2, "歌曲A之二", "歌手2")
        )
        coEvery { musicRepository.searchMusic(query) } returns expectedMusicList
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertEquals(expectedMusicList, result)
        coVerify { musicRepository.searchMusic(query) }
    }
    
    @Test
    fun `invoke with artist keyword returns matching music`() = runTest {
        // Given
        val query = "周杰伦"
        val expectedMusicList = listOf(
            createMusicInfo(1, "晴天", "周杰伦"),
            createMusicInfo(2, "稻香", "周杰伦"),
            createMusicInfo(3, "青花瓷", "周杰伦")
        )
        coEvery { musicRepository.searchMusic(query) } returns expectedMusicList
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertEquals(3, result.size)
        assertEquals(expectedMusicList, result)
        result.forEach { 
            assertEquals("周杰伦", it.music.artist)
        }
    }
    
    @Test
    fun `invoke with album keyword returns matching music`() = runTest {
        // Given
        val query = "叶惠美"
        val expectedMusicList = listOf(
            createMusicInfo(1, "以父之名", "周杰伦", "叶惠美"),
            createMusicInfo(2, "晴天", "周杰伦", "叶惠美")
        )
        coEvery { musicRepository.searchMusic(query) } returns expectedMusicList
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertEquals(2, result.size)
        assertEquals(expectedMusicList, result)
    }
    
    @Test
    fun `invoke with partial keyword returns matching music`() = runTest {
        // Given
        val query = "晴"
        val expectedMusicList = listOf(
            createMusicInfo(1, "晴天", "周杰伦"),
            createMusicInfo(2, "雨后晴空", "林俊杰")
        )
        coEvery { musicRepository.searchMusic(query) } returns expectedMusicList
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertEquals(2, result.size)
        assertEquals(expectedMusicList, result)
    }
    
    @Test
    fun `invoke with empty query returns empty list`() = runTest {
        // Given
        val query = ""
        coEvery { musicRepository.searchMusic(query) } returns emptyList()
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertTrue(result.isEmpty())
        coVerify { musicRepository.searchMusic(query) }
    }
    
    @Test
    fun `invoke with blank query returns empty list`() = runTest {
        // Given
        val query = "   "
        coEvery { musicRepository.searchMusic(query) } returns emptyList()
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `invoke with no matching results returns empty list`() = runTest {
        // Given
        val query = "不存在的歌曲"
        coEvery { musicRepository.searchMusic(query) } returns emptyList()
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertTrue(result.isEmpty())
        coVerify { musicRepository.searchMusic(query) }
    }
    
    @Test
    fun `invoke with special characters returns matching music`() = runTest {
        // Given
        val query = "Let's Go"
        val expectedMusicList = listOf(
            createMusicInfo(1, "Let's Go", "Panic at the Disco")
        )
        coEvery { musicRepository.searchMusic(query) } returns expectedMusicList
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertEquals(1, result.size)
        assertEquals(expectedMusicList, result)
    }
    
    @Test
    fun `invoke with numbers in query returns matching music`() = runTest {
        // Given
        val query = "1990"
        val expectedMusicList = listOf(
            createMusicInfo(1, "1990后的孩子", "测试歌手")
        )
        coEvery { musicRepository.searchMusic(query) } returns expectedMusicList
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertEquals(1, result.size)
    }
    
    @Test
    fun `invoke with mixed case query returns matching music`() = runTest {
        // Given
        val query = "ABC"
        val expectedMusicList = listOf(
            createMusicInfo(1, "ABC Song", "Test Artist"),
            createMusicInfo(2, "abc test", "Another Artist")
        )
        coEvery { musicRepository.searchMusic(query) } returns expectedMusicList
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertEquals(2, result.size)
    }
    
    @Test
    fun `invoke with single character query returns matching music`() = runTest {
        // Given
        val query = "A"
        val expectedMusicList = listOf(
            createMusicInfo(1, "A Song", "Artist A")
        )
        coEvery { musicRepository.searchMusic(query) } returns expectedMusicList
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertEquals(1, result.size)
    }
    
    @Test
    fun `invoke with long query returns matching music`() = runTest {
        // Given
        val query = "这是一个非常长的歌曲名称用于测试搜索功能"
        val expectedMusicList = listOf(
            createMusicInfo(1, "这是一个非常长的歌曲名称用于测试搜索功能", "测试歌手")
        )
        coEvery { musicRepository.searchMusic(query) } returns expectedMusicList
        
        // When
        val result = searchMusicUseCase(query)
        
        // Then
        assertEquals(1, result.size)
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
