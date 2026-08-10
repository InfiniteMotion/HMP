package com.hmp.desktop.ui.common.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DesktopRoutesTest {

    @Test
    fun songDetail_hasCorrectMusicId() {
        val route = Routes.Library.SongDetail(123)
        assertEquals(123L, route.musicId)
    }

    @Test
    fun songDetail_equality() {
        val r1 = Routes.Library.SongDetail(123)
        val r2 = Routes.Library.SongDetail(123)
        assertEquals(r1, r2)
    }

    @Test
    fun songDetail_inequality() {
        val r1 = Routes.Library.SongDetail(123)
        val r2 = Routes.Library.SongDetail(456)
        assertNotEquals(r1, r2)
    }

    @Test
    fun artist_hasCorrectName() {
        val route = Routes.Library.Artist("Test Artist")
        assertEquals("Test Artist", route.name)
    }

    @Test
    fun album_hasCorrectName() {
        val route = Routes.Library.Album("Test Album")
        assertEquals("Test Album", route.name)
    }

    @Test
    fun playlist_hasCorrectName() {
        val route = Routes.Playlist.Playlist("My Playlist")
        assertEquals("My Playlist", route.name)
    }

    @Test
    fun customPlaylist_hasCorrectId() {
        val route = Routes.Playlist.CustomPlaylist(789)
        assertEquals(789L, route.playlistId)
    }

    @Test
    fun objectRoutes_areSingletons() {
        val t1 = Routes.Main.Tabs
        val t2 = Routes.Main.Tabs
        assertEquals(t1, t2)
    }

    @Test
    fun differentObjectRoutes_areNotEqual() {
        val r1: NavKey = Routes.Main.Tabs
        val r2: NavKey = Routes.Player.Player
        assertNotEquals(r1, r2)
    }

    @Test
    fun allRoutes_implementNavKey() {
        val routes: List<NavKey> = listOf(
            Routes.Main.Tabs,
            Routes.Player.Player,
            Routes.Player.Lyrics,
            Routes.Player.AudioEffects,
            Routes.Library.Search,
            Routes.Library.SongDetail(1),
            Routes.Library.Artist("a"),
            Routes.Library.Album("a"),
            Routes.Playlist.Playlist("p"),
            Routes.Playlist.CustomPlaylist(1),
            Routes.Playlist.UserPlaylistManage,
            Routes.Settings.ProfileSettings,
            Routes.Settings.BackupSettings,
            Routes.Settings.LibrarySettings,
            Routes.AI.AI,
            Routes.Custom.Custom,
            Routes.UserData.UserUsageData
        )
        routes.forEach { route ->
            assertTrue(route is NavKey, "$route should be NavKey")
        }
    }

    @Test
    fun songDetail_hashCode_consistent() {
        val r1 = Routes.Library.SongDetail(42)
        val r2 = Routes.Library.SongDetail(42)
        assertEquals(r1.hashCode(), r2.hashCode())
    }

    @Test
    fun artist_toString_containsName() {
        val route = Routes.Library.Artist("MyArtist")
        assertTrue(route.toString().contains("MyArtist"))
    }

    @Test
    fun songDetail_toString_containsId() {
        val route = Routes.Library.SongDetail(999)
        assertTrue(route.toString().contains("999"))
    }
}