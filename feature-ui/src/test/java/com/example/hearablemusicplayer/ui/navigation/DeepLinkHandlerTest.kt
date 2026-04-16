package com.example.hearablemusicplayer.ui.navigation

import android.net.Uri
import io.mockk.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * DeepLinkHandler 单元测试
 * 测试深层链接解析和转换逻辑
 */
class DeepLinkHandlerTest {

    private lateinit var mockRouter: RouteNavigator
    private lateinit var deepLinkHandler: DeepLinkHandler

    @Before
    fun setUp() {
        mockRouter = mockk()
        deepLinkHandler = DeepLinkHandler(mockRouter)
    }

    @Test
    fun `handleDeepLink should return true for valid song deep link`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://song/123")
        val expectedRoute = Routes.Library.SongDetail(123)
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should handle https scheme for song deep link`() {
        // Given
        val uri = Uri.parse("https://song/456")
        val expectedRoute = Routes.Library.SongDetail(456)
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should return false for invalid scheme`() {
        // Given
        val uri = Uri.parse("invalid://song/123")

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertFalse(result)
    }

    @Test
    fun `handleDeepLink should return false for invalid music ID`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://song/not-a-number")

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertFalse(result)
    }

    @Test
    fun `handleDeepLink should return false for missing music ID`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://song")

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertFalse(result)
    }

    @Test
    fun `handleDeepLink should handle playlist deep link`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://playlist/favorites")
        val expectedRoute = Routes.Playlist.Playlist("favorites")
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should handle custom playlist deep link`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://customPlaylist/789")
        val expectedRoute = Routes.Playlist.CustomPlaylist(789)
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should handle artist deep link`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://artist/John%20Doe")
        val expectedRoute = Routes.Library.Artist("John Doe")
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should handle album deep link`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://album/Greatest%20Hits")
        val expectedRoute = Routes.Library.Album("Greatest Hits")
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should handle search deep link without query`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://search")
        val expectedRoute = Routes.Library.Search
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should handle search deep link with query`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://search?q=rock")
        val expectedRoute = Routes.Library.Search
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should handle settings deep link`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://settings")
        val expectedRoute = Routes.Settings.Setting
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should handle audio effects deep link`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://audioEffects")
        val expectedRoute = Routes.Player.AudioEffects
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should handle AI deep link`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://ai")
        val expectedRoute = Routes.AI.AI
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should handle user usage data deep link`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://userUsageData")
        val expectedRoute = Routes.UserData.UserUsageData
        every { mockRouter.navigateTo(expectedRoute) } just runs

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertTrue(result)
        verify { mockRouter.navigateTo(expectedRoute) }
    }

    @Test
    fun `handleDeepLink should return false for unsupported host`() {
        // Given
        val uri = Uri.parse("hearablemusicplayer://unknown")

        // When
        val result = deepLinkHandler.handleDeepLink(uri)

        // Then
        assertFalse(result)
    }

    @Test
    fun `fromJson should parse valid song deep link JSON`() {
        // Given
        val json = """{"type":"com.example.hearablemusicplayer.ui.navigation.DeepLink.Song","musicId":123}"""

        // When
        val deepLink = DeepLinkHandler.fromJson(json)

        // Then
        assertTrue(deepLink is DeepLink.Song)
        assertEquals(123L, (deepLink as DeepLink.Song).musicId)
    }

    @Test
    fun `fromJson should parse valid playlist deep link JSON`() {
        // Given
        val json = """{"type":"com.example.hearablemusicplayer.ui.navigation.DeepLink.Playlist","name":"favorites"}"""

        // When
        val deepLink = DeepLinkHandler.fromJson(json)

        // Then
        assertTrue(deepLink is DeepLink.Playlist)
        assertEquals("favorites", (deepLink as DeepLink.Playlist).name)
    }

    @Test
    fun `fromJson should return null for invalid JSON`() {
        // Given
        val json = """{"invalid":"data"}"""

        // When
        val deepLink = DeepLinkHandler.fromJson(json)

        // Then
        assertNull(deepLink)
    }

    @Test
    fun `toJson should serialize song deep link`() {
        // Given
        val deepLink = DeepLink.Song(123)

        // When
        val json = DeepLinkHandler.toJson(deepLink)

        // Then
        val parsed = Json.decodeFromString<DeepLink>(json)
        assertEquals(deepLink, parsed)
    }

    @Test
    fun `toJson should serialize settings deep link`() {
        // Given
        val deepLink = DeepLink.Settings

        // When
        val json = DeepLinkHandler.toJson(deepLink)

        // Then
        val parsed = Json.decodeFromString<DeepLink>(json)
        assertEquals(deepLink, parsed)
    }

    @Test
    fun `parseUri should throw for unsupported scheme`() {
        // Given
        val uri = Uri.parse("invalid://song/123")

        // When / Then
        assertThrows(IllegalArgumentException::class.java) {
            // 使用反射调用私有方法
            val method = DeepLinkHandler::class.java.getDeclaredMethod("parseUri", Uri::class.java)
            method.isAccessible = true
            method.invoke(deepLinkHandler, uri)
        }
    }

    @Test
    fun `convertToRoute should convert all deep link types correctly`() {
        // Given
        val testCases = listOf(
            DeepLink.Song(123) to Routes.Library.SongDetail(123),
            DeepLink.Playlist("favorites") to Routes.Playlist.Playlist("favorites"),
            DeepLink.CustomPlaylist(456) to Routes.Playlist.CustomPlaylist(456),
            DeepLink.Artist("Artist Name") to Routes.Library.Artist("Artist Name"),
            DeepLink.Album("Album Name") to Routes.Library.Album("Album Name"),
            DeepLink.Search() to Routes.Library.Search,
            DeepLink.Settings to Routes.Settings.Setting,
            DeepLink.AudioEffects to Routes.Player.AudioEffects,
            DeepLink.AI to Routes.AI.AI,
            DeepLink.UserUsageData to Routes.UserData.UserUsageData
        )

        // When / Then
        testCases.forEach { (deepLink, expectedRoute) ->
            // 使用反射调用私有方法
            val method = DeepLinkHandler::class.java.getDeclaredMethod("convertToRoute", DeepLink::class.java)
            method.isAccessible = true
            val actualRoute = method.invoke(deepLinkHandler, deepLink) as NavKey
            
            assertEquals("Failed for $deepLink", expectedRoute, actualRoute)
        }
    }
}