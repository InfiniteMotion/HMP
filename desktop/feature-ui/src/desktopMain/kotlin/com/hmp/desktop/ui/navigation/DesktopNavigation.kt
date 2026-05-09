package com.hmp.desktop.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class DesktopRoute {
    Library,
    Player,
    Playlists,
    Settings,
    Search,
    SongDetail,
    PlaylistDetail
}

object DesktopNavigator {
    var currentRoute by mutableStateOf(DesktopRoute.Library)
        private set

    var selectedMusicId by mutableStateOf<Long?>(null)
        private set

    var selectedPlaylistId by mutableStateOf<Long?>(null)
        private set

    private val backStack = mutableStateListOf<DesktopRoute>()

    fun navigate(route: DesktopRoute) {
        if (currentRoute != route) {
            backStack.add(currentRoute)
            currentRoute = route
        }
    }

    fun navigateToSongDetail(musicId: Long) {
        selectedMusicId = musicId
        navigate(DesktopRoute.SongDetail)
    }

    fun navigateToPlaylistDetail(playlistId: Long) {
        selectedPlaylistId = playlistId
        navigate(DesktopRoute.PlaylistDetail)
    }

    fun popBackStack(): Boolean {
        if (backStack.isEmpty()) return false
        currentRoute = backStack.removeLast()
        return true
    }

    fun canGoBack(): Boolean = backStack.isNotEmpty()
}
