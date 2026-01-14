package com.example.hearablemusicplayer.domain.usecase.settings

import app.cash.turbine.test
import com.example.hearablemusicplayer.domain.repository.SettingsRepository
import com.example.hearablemusicplayer.domain.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UserSettingsUseCase单元测试
 * 
 * 测试范围：
 * - 首次启动状态管理
 * - 用户名读写
 * - 主题模式读写
 * - 头像URI读写
 * - 音乐加载状态读写
 * - DeepSeek API Key读写
 */
@ExperimentalCoroutinesApi
class UserSettingsUseCaseTest {
    
    @get:Rule
    val coroutineRule = CoroutineTestRule()
    
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var userSettingsUseCase: UserSettingsUseCase
    
    @Before
    fun setup() {
        settingsRepository = mockk(relaxed = true)
        userSettingsUseCase = UserSettingsUseCase(settingsRepository)
    }
    
    // 首次启动状态测试
    @Test
    fun `isFirstLaunch returns true when first launch`() = runTest {
        // Given
        every { settingsRepository.isFirstLaunch } returns flowOf(true)
        
        // When & Then
        userSettingsUseCase.isFirstLaunch.test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `isFirstLaunch returns false when not first launch`() = runTest {
        // Given
        every { settingsRepository.isFirstLaunch } returns flowOf(false)
        
        // When & Then
        userSettingsUseCase.isFirstLaunch.test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `saveIsFirstLaunch saves status correctly`() = runTest {
        // Given
        val status = false
        coEvery { settingsRepository.saveIsFirstLaunch(status) } returns Unit
        
        // When
        userSettingsUseCase.saveIsFirstLaunch(status)
        
        // Then
        coVerify { settingsRepository.saveIsFirstLaunch(status) }
    }
    
    // 用户名测试
    @Test
    fun `userName returns default value when no name set`() = runTest {
        // Given
        every { settingsRepository.userName } returns flowOf("User")
        
        // When & Then
        userSettingsUseCase.userName.test {
            assertEquals("User", awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `userName returns correct name when set`() = runTest {
        // Given
        val expectedName = "测试用户"
        every { settingsRepository.userName } returns flowOf(expectedName)
        
        // When & Then
        userSettingsUseCase.userName.test {
            assertEquals(expectedName, awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `saveUserName saves name correctly`() = runTest {
        // Given
        val name = "新用户名"
        coEvery { settingsRepository.saveUserName(name) } returns Unit
        
        // When
        userSettingsUseCase.saveUserName(name)
        
        // Then
        coVerify { settingsRepository.saveUserName(name) }
    }
    
    @Test
    fun `saveUserName handles empty name`() = runTest {
        // Given
        val name = ""
        coEvery { settingsRepository.saveUserName(name) } returns Unit
        
        // When
        userSettingsUseCase.saveUserName(name)
        
        // Then
        coVerify { settingsRepository.saveUserName(name) }
    }
    
    @Test
    fun `saveUserName handles special characters`() = runTest {
        // Given
        val name = "用户@#$%"
        coEvery { settingsRepository.saveUserName(name) } returns Unit
        
        // When
        userSettingsUseCase.saveUserName(name)
        
        // Then
        coVerify { settingsRepository.saveUserName(name) }
    }
    
    // 主题模式测试
    @Test
    fun `customMode returns default theme mode`() = runTest {
        // Given
        val defaultMode = "system"
        every { settingsRepository.themeMode } returns flowOf(defaultMode)
        
        // When & Then
        userSettingsUseCase.customMode.test {
            assertEquals(defaultMode, awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `customMode returns light theme mode`() = runTest {
        // Given
        val mode = "light"
        every { settingsRepository.themeMode } returns flowOf(mode)
        
        // When & Then
        userSettingsUseCase.customMode.test {
            assertEquals(mode, awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `customMode returns dark theme mode`() = runTest {
        // Given
        val mode = "dark"
        every { settingsRepository.themeMode } returns flowOf(mode)
        
        // When & Then
        userSettingsUseCase.customMode.test {
            assertEquals(mode, awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `saveThemeMode saves light mode correctly`() = runTest {
        // Given
        val mode = "light"
        coEvery { settingsRepository.saveThemeMode(mode) } returns Unit
        
        // When
        userSettingsUseCase.saveThemeMode(mode)
        
        // Then
        coVerify { settingsRepository.saveThemeMode(mode) }
    }
    
    @Test
    fun `saveThemeMode saves dark mode correctly`() = runTest {
        // Given
        val mode = "dark"
        coEvery { settingsRepository.saveThemeMode(mode) } returns Unit
        
        // When
        userSettingsUseCase.saveThemeMode(mode)
        
        // Then
        coVerify { settingsRepository.saveThemeMode(mode) }
    }
    
    @Test
    fun `saveThemeMode saves system mode correctly`() = runTest {
        // Given
        val mode = "system"
        coEvery { settingsRepository.saveThemeMode(mode) } returns Unit
        
        // When
        userSettingsUseCase.saveThemeMode(mode)
        
        // Then
        coVerify { settingsRepository.saveThemeMode(mode) }
    }
    
    // 头像URI测试
    @Test
    fun `getAvatarUri returns null when no avatar set`() = runTest {
        // Given
        coEvery { settingsRepository.getAvatarUri() } returns null
        
        // When
        val result = userSettingsUseCase.getAvatarUri()
        
        // Then
        assertNull(result)
        coVerify { settingsRepository.getAvatarUri() }
    }
    
    @Test
    fun `getAvatarUri returns correct uri when set`() = runTest {
        // Given
        val expectedUri = "content://media/external/images/1"
        coEvery { settingsRepository.getAvatarUri() } returns expectedUri
        
        // When
        val result = userSettingsUseCase.getAvatarUri()
        
        // Then
        assertEquals(expectedUri, result)
        coVerify { settingsRepository.getAvatarUri() }
    }
    
    @Test
    fun `saveAvatarUri saves uri correctly`() = runTest {
        // Given
        val uri = "content://media/external/images/1"
        coEvery { settingsRepository.saveAvatarUri(uri) } returns Unit
        
        // When
        userSettingsUseCase.saveAvatarUri(uri)
        
        // Then
        coVerify { settingsRepository.saveAvatarUri(uri) }
    }
    
    @Test
    fun `saveAvatarUri handles empty uri`() = runTest {
        // Given
        val uri = ""
        coEvery { settingsRepository.saveAvatarUri(uri) } returns Unit
        
        // When
        userSettingsUseCase.saveAvatarUri(uri)
        
        // Then
        coVerify { settingsRepository.saveAvatarUri(uri) }
    }
    
    // 音乐加载状态测试
    @Test
    fun `isLoadMusic returns false when music not loaded`() = runTest {
        // Given
        every { settingsRepository.isLoadMusic } returns flowOf(false)
        
        // When & Then
        userSettingsUseCase.isLoadMusic.test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `isLoadMusic returns true when music loaded`() = runTest {
        // Given
        every { settingsRepository.isLoadMusic } returns flowOf(true)
        
        // When & Then
        userSettingsUseCase.isLoadMusic.test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `saveIsLoadMusic saves true correctly`() = runTest {
        // Given
        val status = true
        coEvery { settingsRepository.saveIsLoadMusic(status) } returns Unit
        
        // When
        userSettingsUseCase.saveIsLoadMusic(status)
        
        // Then
        coVerify { settingsRepository.saveIsLoadMusic(status) }
    }
    
    @Test
    fun `saveIsLoadMusic saves false correctly`() = runTest {
        // Given
        val status = false
        coEvery { settingsRepository.saveIsLoadMusic(status) } returns Unit
        
        // When
        userSettingsUseCase.saveIsLoadMusic(status)
        
        // Then
        coVerify { settingsRepository.saveIsLoadMusic(status) }
    }
}
