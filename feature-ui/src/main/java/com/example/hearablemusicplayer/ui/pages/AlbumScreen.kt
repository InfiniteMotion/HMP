package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
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
import com.example.hearablemusicplayer.ui.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaybackViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistQueueViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

@OptIn(UnstableApi::class)
@Composable
fun AlbumScreen(
    navController: NavBackStack<NavKey>,
    albumName: String,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    playbackViewModel: PlaybackViewModel = hiltViewModel(),
    playlistQueueViewModel: PlaylistQueueViewModel = hiltViewModel(),
    dialogViewModel: DialogViewModel = hiltViewModel(),
) {
    // 手动调用 getSelectedAlbumMusicList 方法，传入 albumName
    LaunchedEffect(albumName) {
        playlistViewModel.getSelectedAlbumMusicList(albumName)
    }
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    val displayAlbumName by playlistViewModel.selectedAlbumName.collectAsState()
    val albumMusicList by playlistViewModel.selectedAlbumMusicList.collectAsState(initial = emptyList())
    val currentPlayingMusic by playlistQueueViewModel.currentPlayingMusic.collectAsState(null)
    AlbumScreenContent(
        isPlaying = isPlaying,
        albumName = displayAlbumName,
        albumMusicList = albumMusicList,
        currentPlayingMusic = currentPlayingMusic,
        onBackClick = { navController.removeLastOrNull() },
        onShufflePlay = {
            playlistQueueViewModel.addAllToPlaylistByShuffle(albumMusicList)
            navController.add(Routes.Player)
        },
        onOrderPlay = {
            playlistQueueViewModel.addAllToPlaylistInOrder(albumMusicList)
            navController.add(Routes.Player)
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
    albumMusicList: List<MusicInfo>,
    currentPlayingMusic: MusicInfo?,
    onBackClick: () -> Unit,
    onShufflePlay: () -> Unit,
    onOrderPlay: () -> Unit,
    playWith: suspend (MusicInfo) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit,
    onShowMusicDetailDialog: (MusicInfo) -> Unit
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
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
    SubScreen(
        onBackClick = onBackClick,
        title = albumName
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
                musicInfoList = albumMusicList,
                config = config,
                modifier = Modifier.fillMaxWidth().weight(1f),
                isPlaying = isPlaying,
            )
        }
    }
}