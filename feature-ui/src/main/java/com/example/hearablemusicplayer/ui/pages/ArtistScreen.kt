package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonTwo
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.ui.util.Routes

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext

@OptIn(UnstableApi::class)
@Composable
fun ArtistScreen(
    playlistViewModel: PlaylistViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    playControlViewModel: PlayControlViewModel = hiltViewModel(),
    navController: NavController,
) {
    val artistName by playlistViewModel.selectedArtistName.collectAsState()
    val artistMusicList by playlistViewModel.selectedArtistMusicList.collectAsState(initial = emptyList())
    
    ArtistScreenContent(
        artistName = artistName,
        artistMusicList = artistMusicList,
        onBackClick = { navController.popBackStack() },
        onShufflePlay = {
            playControlViewModel.addAllToPlaylistByShuffle(artistMusicList)
            navController.navigate(Routes.PLAYER)
        },
        onOrderPlay = {
            playControlViewModel.addAllToPlaylistInOrder(artistMusicList)
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
fun ArtistScreenContent(
    artistName: String,
    artistMusicList: List<MusicInfo>,
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
        title = artistName
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
                musicInfoList = artistMusicList,
                navigate = onNavigate,
                playWith = playWith,
                recordPlayback = recordPlayback,
                addToPlaylist = addToPlaylist
            )
        }
    }
}
