package com.hmp.desktop.ui.common.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopDeepLinkHandlerTest {

    private lateinit var navController: NavController
    private lateinit var deepLinkHandler: DeepLinkHandler

    private fun setUp() {
        navController = NavController(Routes.Main.Tabs)
        deepLinkHandler = DeepLinkHandler(navController)
    }

    @Test
    fun handleDeepLink_validSongLink_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://song/123"))
    }

    @Test
    fun handleDeepLink_validPlaylistLink_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://playlist/favorites"))
    }

    @Test
    fun handleDeepLink_validCustomPlaylistLink_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://customPlaylist/456"))
    }

    @Test
    fun handleDeepLink_validArtistLink_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://artist/John"))
    }

    @Test
    fun handleDeepLink_validAlbumLink_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://album/GreatestHits"))
    }

    @Test
    fun handleDeepLink_validSearchLink_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://search"))
    }

    @Test
    fun handleDeepLink_searchWithQuery_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://search?q=rock"))
    }

    @Test
    fun handleDeepLink_settingsLink_returnsTrue() {
        setUp()
        // Settings maps to Main.Tabs which is the initial route
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://settings"))
    }

    @Test
    fun handleDeepLink_audioEffectsLink_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://audioEffects"))
    }

    @Test
    fun handleDeepLink_aiLink_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://ai"))
    }

    @Test
    fun handleDeepLink_userUsageDataLink_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("hearablemusicplayer://userUsageData"))
    }

    @Test
    fun handleDeepLink_httpsScheme_returnsTrue() {
        setUp()
        assertTrue(deepLinkHandler.handleDeepLink("https://song/789"))
    }

    @Test
    fun handleDeepLink_invalidScheme_returnsFalse() {
        setUp()
        assertFalse(deepLinkHandler.handleDeepLink("ftp://song/123"))
    }

    @Test
    fun handleDeepLink_invalidMusicId_returnsFalse() {
        setUp()
        assertFalse(deepLinkHandler.handleDeepLink("hearablemusicplayer://song/abc"))
    }

    @Test
    fun handleDeepLink_missingMusicId_returnsFalse() {
        setUp()
        assertFalse(deepLinkHandler.handleDeepLink("hearablemusicplayer://song"))
    }

    @Test
    fun handleDeepLink_unknownHost_returnsFalse() {
        setUp()
        assertFalse(deepLinkHandler.handleDeepLink("hearablemusicplayer://unknown"))
    }

    @Test
    fun handleDeepLink_malformedUri_returnsFalse() {
        setUp()
        assertFalse(deepLinkHandler.handleDeepLink("not-a-uri"))
    }

    @Test
    fun handleDeepLink_navigatesToCorrectRoute_song() {
        setUp()
        deepLinkHandler.handleDeepLink("hearablemusicplayer://song/42")
        assertTrue(navController.contains(Routes.Library.SongDetail(42)))
    }

    @Test
    fun handleDeepLink_navigatesToCorrectRoute_playlist() {
        setUp()
        deepLinkHandler.handleDeepLink("hearablemusicplayer://playlist/mylist")
        assertTrue(navController.contains(Routes.Playlist.Playlist("mylist")))
    }

    @Test
    fun handleDeepLink_navigatesToCorrectRoute_artist() {
        setUp()
        deepLinkHandler.handleDeepLink("hearablemusicplayer://artist/TestArtist")
        assertTrue(navController.contains(Routes.Library.Artist("TestArtist")))
    }

    @Test
    fun handleDeepLink_navigatesToCorrectRoute_album() {
        setUp()
        deepLinkHandler.handleDeepLink("hearablemusicplayer://album/TestAlbum")
        assertTrue(navController.contains(Routes.Library.Album("TestAlbum")))
    }

    @Test
    fun handleDeepLink_navigatesToCorrectRoute_search() {
        setUp()
        deepLinkHandler.handleDeepLink("hearablemusicplayer://search")
        assertTrue(navController.contains(Routes.Library.Search))
    }

    @Test
    fun handleDeepLink_navigatesToCorrectRoute_ai() {
        setUp()
        deepLinkHandler.handleDeepLink("hearablemusicplayer://ai")
        assertTrue(navController.contains(Routes.AI.AI))
    }

    @Test
    fun handleDeepLink_navigatesToCorrectRoute_audioEffects() {
        setUp()
        deepLinkHandler.handleDeepLink("hearablemusicplayer://audioEffects")
        assertTrue(navController.contains(Routes.Player.AudioEffects))
    }

    @Test
    fun handleDeepLink_navigatesToCorrectRoute_userUsageData() {
        setUp()
        deepLinkHandler.handleDeepLink("hearablemusicplayer://userUsageData")
        assertTrue(navController.contains(Routes.UserData.UserUsageData))
    }

    @Test
    fun handleDeepLink_invalidDoesNotChangeStack() {
        setUp()
        val initialSize = navController.size
        deepLinkHandler.handleDeepLink("ftp://invalid")
        assertEquals(initialSize, navController.size)
    }

    @Test
    fun handleDeepLink_customPlaylist_navigatesCorrectly() {
        setUp()
        deepLinkHandler.handleDeepLink("hearablemusicplayer://customPlaylist/789")
        assertTrue(navController.contains(Routes.Playlist.CustomPlaylist(789)))
    }

    @Test
    fun handleDeepLink_searchWithQuery_navigatesToSearch() {
        setUp()
        deepLinkHandler.handleDeepLink("hearablemusicplayer://search?q=test")
        assertTrue(navController.contains(Routes.Library.Search))
    }
}