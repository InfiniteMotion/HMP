package com.example.hearablemusicplayer.ui.pages

import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonTwo
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

@OptIn(UnstableApi::class)
@Composable
fun PlaylistScreen(
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel(),
    navController: NavController,
) {
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    val playlistName by playlistViewModel.selectedPlaylistName.collectAsState()
    val playlist by playlistViewModel.selectedPlaylist.collectAsState(initial = emptyList())
    
    PlaylistScreenContent(
        isPlaying = isPlaying,
        playlistName = playlistName,
        playlist = playlist,
        onBackClick = { navController.popBackStack() },
        onShufflePlay = {
            playControlViewModel.addAllToPlaylistByShuffle(playlist)
            navController.navigate(Routes.Player)
        },
        onOrderPlay = {
            playControlViewModel.addAllToPlaylistInOrder(playlist)
            navController.navigate(Routes.Player)
        },
        onNavigate = navController::navigate,
        playWith = playControlViewModel::playWith,
        recordPlayback = playControlViewModel::recordPlayback,
        addToPlaylist = playControlViewModel::addToPlaylist
    )
}

@OptIn(UnstableApi::class)
@Composable
fun PlaylistScreenContent(
    isPlaying: Boolean,
    playlistName: String,
    playlist: List<MusicInfo>,
    onBackClick: () -> Unit,
    onShufflePlay: () -> Unit,
    onOrderPlay: () -> Unit,
    onNavigate: (Any) -> Unit,
    playWith: suspend (MusicInfo) -> Unit,
    recordPlayback: (Long, String) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit
) {
    val haptic = rememberHapticFeedback()
    SubScreen(
        onBackClick = onBackClick,
        title = playlistName
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            PlayControlButtonTwo(
                onShufflePlay = onShufflePlay,
                onOrderPlay = onOrderPlay
            )
            MusicList(
                musicInfoList = playlist,
                onItemClick = { music ->
                    haptic.performClick()
                    onNavigate(Routes.SongDetail(music.music.id))
                },
                onAddToPlaylist = { _ -> },
                onMenuClick = { _ -> },
                showAddButton = false,
                showMenuButton = true,
                isPlaying = isPlaying,
            )
        }
    }
}
