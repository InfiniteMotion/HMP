package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
fun ArtistScreen(
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel(),
    navController: NavController,
) {
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    val artistName by playlistViewModel.selectedArtistName.collectAsState()
    val artistMusicList by playlistViewModel.selectedArtistMusicList.collectAsState(initial = emptyList())
    
    ArtistScreenContent(
        isPlaying = isPlaying,
        artistName = artistName,
        artistMusicList = artistMusicList,
        onBackClick = { navController.popBackStack() },
        onShufflePlay = {
            playControlViewModel.addAllToPlaylistByShuffle(artistMusicList)
            navController.navigate(Routes.Player)
        },
        onOrderPlay = {
            playControlViewModel.addAllToPlaylistInOrder(artistMusicList)
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
fun ArtistScreenContent(
    isPlaying: Boolean,
    artistName: String,
    artistMusicList: List<MusicInfo>,
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
                onItemClick = {
                    haptic.performClick()
                    playWith(it)
                    onNavigate(Routes.Player) },
                onAddToPlaylist = addToPlaylist,
                onMenuClick = {onNavigate(Routes.SongDetail(it.music.id))},
                showAddButton = true,
                showMenuButton = true,
                isPlaying = isPlaying,
            )
        }
    }
}
