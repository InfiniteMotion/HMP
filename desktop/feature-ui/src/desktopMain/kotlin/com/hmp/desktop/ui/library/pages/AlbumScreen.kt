package com.hmp.desktop.ui.library.pages
import com.hmp.desktop.ui.common.navigation.NavController

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject


import com.hmp.domain.music.MusicInfo
import com.hmp.desktop.ui.common.components.base.PlayControlButtonTwo
import com.hmp.desktop.ui.common.components.base.UiStateContent
import com.hmp.desktop.ui.library.pages.components.musiclist.CurrentPlayingConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.EditConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.FullItemOptions
import com.hmp.desktop.ui.library.pages.components.musiclist.HeaderConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.ItemConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.ItemVariant
import com.hmp.desktop.ui.library.pages.components.musiclist.MusicList
import com.hmp.desktop.ui.library.pages.components.musiclist.MusicListCallbacksAdapter
import com.hmp.desktop.ui.library.pages.components.musiclist.defaultMusicListConfig
import com.hmp.desktop.ui.common.pages.base.SubScreen
import com.hmp.desktop.ui.common.navigation.Routes
import com.hmp.desktop.ui.common.util.UiState
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.playlist.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch

@Composable
fun AlbumScreen(
    navController: NavController,
    albumName: String,
    playlistViewModel: PlaylistViewModel = koinInject(),
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
        onBackClick = { navController.popBackStack() },
        onShufflePlay = { musicList ->
            playlistQueueViewModel.addAllToPlaylistByShuffle(musicList)
            navController.navigate(Routes.Player.Player)
        },
        onOrderPlay = { musicList ->
            playlistQueueViewModel.addAllToPlaylistInOrder(musicList)
            navController.navigate(Routes.Player.Player)
        },
        playWith = playlistQueueViewModel::playWith,
        addToPlaylist = playlistQueueViewModel::addToPlaylist,
        onShowMusicDetailDialog = dialogViewModel::showMusicDetailDialog
    )
}

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
                    currentPlaying = CurrentPlayingConfig(
                        index = currentPlayingIndex,
                        autoScrollToCurrent = false
                    ),
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
