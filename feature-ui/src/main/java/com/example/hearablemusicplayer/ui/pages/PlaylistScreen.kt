package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonTwo
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext

@OptIn(UnstableApi::class)
@Composable
fun PlaylistScreen(
    playlistViewModel: PlaylistViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    playControlViewModel: PlayControlViewModel = hiltViewModel(),
    navController: NavController,
) {
    val playlistName by playlistViewModel.selectedPlaylistName.collectAsState()
    val playlist by playlistViewModel.selectedPlaylist.collectAsState(initial = emptyList())
    
    PlaylistScreenContent(
        playlistName = playlistName,
        playlist = playlist,
        onBackClick = { navController.popBackStack() },
        onShufflePlay = {
            playControlViewModel.addAllToPlaylistByShuffle(playlist)
            navController.navigate(Routes.PLAYER)
        },
        onOrderPlay = {
            playControlViewModel.addAllToPlaylistInOrder(playlist)
            navController.navigate(Routes.PLAYER)
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
    playlistName: String,
    playlist: List<MusicInfo>,
    onBackClick: () -> Unit,
    onShufflePlay: () -> Unit,
    onOrderPlay: () -> Unit,
    onNavigate: (String) -> Unit,
    playWith: suspend (MusicInfo) -> Unit,
    recordPlayback: (Long, String) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit
) {
    SubScreen(
        onBackClick = onBackClick,
        title = playlistName
    ) {
        PlayControlButtonTwo(
            onShufflePlay = onShufflePlay,
            onOrderPlay = onOrderPlay
        )
        MusicList(
            musicInfoList = playlist,
            navigate = onNavigate,
            playWith = playWith,
            recordPlayback = recordPlayback,
            addToPlaylist = addToPlaylist
        )
    }
}
