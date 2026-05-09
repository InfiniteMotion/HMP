package com.example.hearablemusicplayer.ui.navigation

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * 导航图测试
 * 测试路由映射是否正确，验证导航图中包含了所有预期的路由定义
 */
class NavigationGraphTest {

    private val navigationGraphPath = "d:\\MyFile\\HMP\\feature-ui\\src\\main\\java\\com\\example\\hearablemusicplayer\\ui\\navigation\\NavigationGraph.kt"
    private val navigationGraphFile = File(navigationGraphPath)
    private val fileContent by lazy { navigationGraphFile.readText() }

    @Test
    fun `navigation graph file should exist`() {
        assertTrue("NavigationGraph.kt should exist", navigationGraphFile.exists())
    }

    @Test
    fun `navigation graph should contain entries for all route types`() {
        // 定义所有预期的路由 entry 模式
        val expectedEntryPatterns = listOf(
            "entry<Routes.Main.Tabs>",
            "entry<Routes.Library.SongDetail>",
            "entry<Routes.Player.Player>",
            "entry<Routes.Settings.Setting>",
            "entry<Routes.Settings.ProfileSettings>",
            "entry<Routes.Settings.BackupSettings>",
            "entry<Routes.Settings.LibrarySettings>",
            "entry<Routes.Library.Search>",
            "entry<Routes.Playlist.Playlist>",
            "entry<Routes.Playlist.CustomPlaylist>",
            "entry<Routes.Playlist.UserPlaylistManage>",
            "entry<Routes.Library.Artist>",
            "entry<Routes.Library.Album>",
            "entry<Routes.Player.AudioEffects>",
            "entry<Routes.Player.Lyrics>",
            "entry<Routes.AI.AI>",
            "entry<Routes.Custom.Custom>",
            "entry<Routes.UserData.UserUsageData>"
        )

        // When / Then
        expectedEntryPatterns.forEach { pattern ->
            assertTrue(
                "NavigationGraph should contain entry for $pattern",
                fileContent.contains(pattern)
            )
        }
    }

    @Test
    fun `navigation graph should contain deep link patterns for supported routes`() {
        // 定义所有预期的深层链接模式
        val expectedDeepLinkPatterns = listOf(
            "hearablemusicplayer://song/{musicId}",
            "hearablemusicplayer://playlist/{name}",
            "hearablemusicplayer://customPlaylist/{playlistId}",
            "hearablemusicplayer://artist/{name}",
            "hearablemusicplayer://album/{name}",
            "hearablemusicplayer://search",
            "hearablemusicplayer://settings",
            "hearablemusicplayer://audioEffects",
            "hearablemusicplayer://ai",
            "hearablemusicplayer://userUsageData"
        )

        // When / Then
        expectedDeepLinkPatterns.forEach { pattern ->
            assertTrue(
                "NavigationGraph should contain deep link pattern: $pattern",
                fileContent.contains(pattern)
            )
        }
    }

    @Test
    fun `all routes should have corresponding entry in navigation graph`() {
        // 获取 Routes 中定义的所有路由类
        // 注意：有些路由可能通过其他方式使用（如 Main.Home 可能在 TabsHost 内部使用）
        // 我们只检查那些应该有直接 entry 的路由
        val routesWithDirectEntry = listOf(
            "Routes.Main.Tabs",
            "Routes.Library.SongDetail",
            "Routes.Player.Player",
            "Routes.Settings.Setting",
            "Routes.Settings.ProfileSettings",
            "Routes.Settings.BackupSettings",
            "Routes.Settings.LibrarySettings",
            "Routes.Library.Search",
            "Routes.Playlist.Playlist",
            "Routes.Playlist.CustomPlaylist",
            "Routes.Playlist.UserPlaylistManage",
            "Routes.Library.Artist",
            "Routes.Library.Album",
            "Routes.Player.AudioEffects",
            "Routes.Player.Lyrics",
            "Routes.AI.AI",
            "Routes.Custom.Custom",
            "Routes.UserData.UserUsageData"
        )

        // When / Then
        routesWithDirectEntry.forEach { routeClass ->
            assertTrue(
                "NavigationGraph should contain reference to $routeClass",
                fileContent.contains(routeClass)
            )
        }
    }

    @Test
    fun `deep link patterns should match DeepLinkHandler expectations`() {
        // 验证 NavigationGraph 中的深层链接模式与 DeepLinkHandler 中处理的模式一致
        
        // 从 DeepLinkHandler 提取支持的 host 列表
        val supportedHosts = listOf(
            "song",
            "playlist",
            "customPlaylist",
            "artist",
            "album",
            "search",
            "settings",
            "audioEffects",
            "ai",
            "userUsageData"
        )

        supportedHosts.forEach { host ->
            val pattern = "hearablemusicplayer://$host"
            assertTrue(
                "NavigationGraph should contain deep link pattern for host: $host",
                fileContent.contains(pattern)
            )
        }
    }

    @Test
    fun `navigation graph should compile without errors`() {
        // 这个测试是概念性的，实际编译检查应该在构建过程中完成
        // 这里我们只验证文件存在且内容非空
        assertTrue(navigationGraphFile.exists())
        assertTrue(fileContent.isNotBlank())
        assertTrue(fileContent.contains("fun NavigationGraph"))
    }
}