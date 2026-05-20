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
import com.hmp.desktop.ui.common.util.UiState
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.playlist.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch
import com.hmp.desktop.ui.common.pages.base.SubScreen
import com.hmp.desktop.ui.common.navigation.Routes

@Composable
fun ArtistScreen(
    navController: NavController,
    artistName: String,
    playlistViewModel: PlaylistViewModel = koinInject(),
    playbackViewModel: PlaybackViewModel,
    playlistQueueViewModel: PlaylistQueueViewModel,
    dialogViewModel: DialogViewModel,
) {
    LaunchedEffect(artistName) {
        playlistViewModel.getSelectedArtistMusicList(artistName)
    }
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    val displayArtistName by playlistViewModel.selectedArtistName.collectAsState()
    val artistMusicListState by playlistViewModel.selectedArtistMusicListState.collectAsState()
    val currentPlayingMusic by playlistQueueViewModel.currentPlayingMusic.collectAsState(null)
    ArtistScreenContent(
        isPlaying = isPlaying,
        artistName = displayArtistName,
        artistMusicListState = artistMusicListState,
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
fun ArtistScreenContent(
    isPlaying: Boolean,
    artistName: String,
    artistMusicListState: UiState<List<MusicInfo>>,
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
        title = artistName
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(),
        ) {
            UiStateContent(
                state = artistMusicListState,
                modifier = Modifier.weight(1f)
            ) { artistMusicList ->
                val currentPlayingIndex = artistMusicList.indexOfFirst { it.music.id == currentPlayingMusic?.music?.id }.takeIf { it >= 0 }
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
                    onShufflePlay = { onShufflePlay(artistMusicList) },
                    onOrderPlay = { onOrderPlay(artistMusicList) }
                )
                MusicList(
                    musicInfoList = artistMusicList,
                    config = config,
                    modifier = Modifier.fillMaxWidth(),
                    isPlaying = isPlaying,
                )
            }
        }
    }
}
