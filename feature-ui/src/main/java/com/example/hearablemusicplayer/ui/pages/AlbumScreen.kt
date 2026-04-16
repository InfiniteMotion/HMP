package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.components.PlayControlButtonTwo
import com.example.hearablemusicplayer.ui.components.common.UiStateContent
import com.example.hearablemusicplayer.ui.components.musiclist.CurrentPlayingConfig
import com.example.hearablemusicplayer.ui.components.musiclist.EditConfig
import com.example.hearablemusicplayer.ui.components.musiclist.FullItemOptions
import com.example.hearablemusicplayer.ui.components.musiclist.HeaderConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemVariant
import com.example.hearablemusicplayer.ui.components.musiclist.MusicList
import com.example.hearablemusicplayer.ui.components.musiclist.MusicListCallbacksAdapter
import com.example.hearablemusicplayer.ui.components.musiclist.defaultMusicListConfig
import com.example.hearablemusicplayer.ui.pages.base.SubScreen
import com.example.hearablemusicplayer.ui.navigation.Routes
import com.example.hearablemusicplayer.ui.util.UiState
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaybackViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistQueueViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun AlbumScreen(
    navController: NavBackStack<NavKey>,
    albumName: String,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    playbackViewModel: PlaybackViewModel,
    playlistQueueViewModel: PlaylistQueueViewModel,
    dialogViewModel: DialogViewModel,
) {
    LaunchedEffect(albumName) {
        playlistViewModel.getSelectedAlbumMusicList(albumName)
    }
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    val displayAlbumName by playlistViewModel.selectedAlbumName.collectAsState()
    val albumMusicListState by playlistViewModel.selectedAlbumMusicListState.collectAsState()
    val currentPlayingMusic by playlistQueueViewModel.currentPlayingMusic.collectAsState(null)
    AlbumScreenContent(
        isPlaying = isPlaying,
        albumName = displayAlbumName,
        albumMusicListState = albumMusicListState,
        currentPlayingMusic = currentPlayingMusic,
        onBackClick = { navController.removeLastOrNull() },
        onShufflePlay = { musicList ->
            playlistQueueViewModel.addAllToPlaylistByShuffle(musicList)
            navController.add(Routes.Player.Player)
        },
        onOrderPlay = { musicList ->
            playlistQueueViewModel.addAllToPlaylistInOrder(musicList)
            navController.add(Routes.Player.Player)
        },
        playWith = playlistQueueViewModel::playWith,
        addToPlaylist = playlistQueueViewModel::addToPlaylist,
        onShowMusicDetailDialog = dialogViewModel::showMusicDetailDialog
    )
}

@OptIn(UnstableApi::class)
@Composable
fun AlbumScreenContent(
    isPlaying: Boolean,
    albumName: String,
    albumMusicListState: UiState<List<MusicInfo>>,
    currentPlayingMusic: MusicInfo?,
    onBackClick: () -> Unit,
    onShufflePlay: (List<MusicInfo>) -> Unit,
    onOrderPlay: (List<MusicInfo>) -> Unit,
    playWith: suspend (MusicInfo) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit,
    onShowMusicDetailDialog: (MusicInfo) -> Unit
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    SubScreen(
        onBackClick = onBackClick,
        title = albumName
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(),
        ) {
            UiStateContent(
                state = albumMusicListState,
                modifier = Modifier.weight(1f)
            ) { albumMusicList ->
                val currentPlayingIndex = albumMusicList.indexOfFirst { it.music.id == currentPlayingMusic?.music?.id }.takeIf { it >= 0 }
                val callbacks = object : MusicListCallbacksAdapter() {
                    override fun onItemClick(musicInfo: MusicInfo, index: Int) {
                        haptic.performClick()
                        scope.launch { playWith(musicInfo) }
                    }
                    override fun onAddToPlaylist(musicInfo: MusicInfo) { addToPlaylist(musicInfo) }
                    override fun onMenuClick(musicInfo: MusicInfo) { onShowMusicDetailDialog(musicInfo) }
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
                PlayControlButtonTwo(
                    onShufflePlay = { onShufflePlay(albumMusicList) },
                    onOrderPlay = { onOrderPlay(albumMusicList) }
                )
                MusicList(
                    musicInfoList = albumMusicList,
                    config = config,
                    modifier = Modifier.fillMaxWidth(),
                    isPlaying = isPlaying,
                )
            }
        }
    }
}
