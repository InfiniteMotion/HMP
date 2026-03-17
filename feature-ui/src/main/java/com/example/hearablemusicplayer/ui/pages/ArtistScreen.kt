package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.components.musiclist.CurrentPlayingConfig
import com.example.hearablemusicplayer.ui.components.musiclist.EditConfig
import com.example.hearablemusicplayer.ui.components.musiclist.FullItemOptions
import com.example.hearablemusicplayer.ui.components.musiclist.HeaderConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemVariant
import com.example.hearablemusicplayer.ui.components.musiclist.MusicList
import com.example.hearablemusicplayer.ui.components.musiclist.MusicListCallbacksAdapter
import com.example.hearablemusicplayer.ui.components.musiclist.defaultMusicListConfig
import com.example.hearablemusicplayer.ui.components.PlayControlButtonTwo
import kotlinx.coroutines.launch
import com.example.hearablemusicplayer.ui.pages.base.SubScreen
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
    val currentPlayingMusic by playControlViewModel.currentPlayingMusic.collectAsState(null)
    ArtistScreenContent(
        isPlaying = isPlaying,
        artistName = artistName,
        artistMusicList = artistMusicList,
        currentPlayingMusic = currentPlayingMusic,
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
        addToPlaylist = playControlViewModel::addToPlaylist
    )
}

@OptIn(UnstableApi::class)
@Composable
fun ArtistScreenContent(
    isPlaying: Boolean,
    artistName: String,
    artistMusicList: List<MusicInfo>,
    currentPlayingMusic: MusicInfo?,
    onBackClick: () -> Unit,
    onShufflePlay: () -> Unit,
    onOrderPlay: () -> Unit,
    onNavigate: (Any) -> Unit,
    playWith: suspend (MusicInfo) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val currentPlayingIndex = artistMusicList.indexOfFirst { it.music.id == currentPlayingMusic?.music?.id }.takeIf { it >= 0 }
    val callbacks = object : MusicListCallbacksAdapter() {
        override fun onItemClick(musicInfo: MusicInfo, index: Int) {
            haptic.performClick()
            scope.launch { playWith(musicInfo) }
        }
        override fun onAddToPlaylist(musicInfo: MusicInfo) { addToPlaylist(musicInfo) }
        override fun onMenuClick(musicInfo: MusicInfo) { onNavigate(Routes.SongDetail(musicInfo.music.id)) }
    }
    val config = defaultMusicListConfig(callbacks).copy(
        header = HeaderConfig.None,
        item = ItemConfig(
            variant = ItemVariant.Full,
            showIndex = true,
            fullOptions = FullItemOptions(
                showPinButton = false,
                showRemoveButton = false,
                showMenuButton = true,
                showAddToPlaylistInMenu = true,
            ),
        ),
        edit = EditConfig(enabled = false),
        currentPlaying = CurrentPlayingConfig(index = currentPlayingIndex, autoScrollToCurrent = false),
    )
    SubScreen(
        onBackClick = onBackClick,
        title = artistName
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(),
        ) {
            PlayControlButtonTwo(
                onShufflePlay = onShufflePlay,
                onOrderPlay = onOrderPlay
            )
            MusicList(
                musicInfoList = artistMusicList,
                config = config,
                modifier = Modifier.fillMaxWidth().weight(1f),
                isPlaying = isPlaying,
            )
        }
    }
}
