package com.hmp.desktop.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hmp.desktop.player.DesktopMusicController
import com.hmp.desktop.ui.components.MiniPlayerBar
import com.hmp.desktop.ui.navigation.DesktopNavigator
import com.hmp.desktop.ui.navigation.DesktopRoute
import com.hmp.desktop.ui.screens.LibraryScreen
import com.hmp.desktop.ui.screens.PlayerScreen
import com.hmp.desktop.ui.screens.PlaylistScreen
import com.hmp.desktop.ui.screens.SearchScreen
import com.hmp.desktop.ui.screens.SettingsScreen
import com.hmp.desktop.ui.theme.DesktopTheme
import org.koin.compose.koinInject

@Composable
fun DesktopApp() {
    DesktopTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val controller: DesktopMusicController = koinInject()
            val currentMusic = controller.currentPlayingMusic.collectAsState().value
            val isPlaying = controller.isPlaying.collectAsState().value
            val currentPosition = controller.currentPosition.collectAsState().value
            val duration = controller.duration.collectAsState().value

            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    // Navigation Rail
                    NavigationRail(
                        modifier = Modifier.width(80.dp).fillMaxHeight()
                    ) {
                        NavigationRailItem(
                            selected = DesktopNavigator.currentRoute == DesktopRoute.Library,
                            onClick = { DesktopNavigator.navigate(DesktopRoute.Library) },
                            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                            label = { Text("Library") }
                        )
                        NavigationRailItem(
                            selected = DesktopNavigator.currentRoute == DesktopRoute.Playlists,
                            onClick = { DesktopNavigator.navigate(DesktopRoute.Playlists) },
                            icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = "Playlists") },
                            label = { Text("Playlists") }
                        )
                        NavigationRailItem(
                            selected = DesktopNavigator.currentRoute == DesktopRoute.Player,
                            onClick = { DesktopNavigator.navigate(DesktopRoute.Player) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Player") },
                            label = { Text("Player") }
                        )
                        NavigationRailItem(
                            selected = DesktopNavigator.currentRoute == DesktopRoute.Settings,
                            onClick = { DesktopNavigator.navigate(DesktopRoute.Settings) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }

                    // Content
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (DesktopNavigator.currentRoute) {
                            DesktopRoute.Library -> LibraryScreen(
                                onMusicClick = { musicId ->
                                    val music = controller.currentPlaylist.value.find { it.music.id == musicId }
                                    if (music != null) {
                                        controller.playMusic(music)
                                    }
                                },
                                onSearchClick = { DesktopNavigator.navigate(DesktopRoute.Search) }
                            )
                            DesktopRoute.Player -> PlayerScreen()
                            DesktopRoute.Playlists -> PlaylistScreen(
                                onPlaylistClick = { /* TODO */ }
                            )
                            DesktopRoute.Settings -> SettingsScreen()
                            DesktopRoute.Search -> SearchScreen(
                                onMusicClick = { musicId ->
                                    val music = controller.currentPlaylist.value.find { it.music.id == musicId }
                                    if (music != null) {
                                        controller.playMusic(music)
                                    }
                                }
                            )
                            DesktopRoute.SongDetail -> {
                                // TODO: Song detail screen
                                Text("Song Detail - Coming Soon")
                            }
                            DesktopRoute.PlaylistDetail -> {
                                // TODO: Playlist detail screen
                                Text("Playlist Detail - Coming Soon")
                            }
                        }
                    }
                }

                // Mini Player Bar
                MiniPlayerBar(
                    musicInfo = currentMusic,
                    isPlaying = isPlaying,
                    progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                    onPlayPause = { controller.togglePlayPause() },
                    onNext = { controller.playNext() },
                    onPrev = { controller.playPrevious() },
                    onOpenPlayer = { DesktopNavigator.navigate(DesktopRoute.Player) },
                )
            }
        }
    }
}
