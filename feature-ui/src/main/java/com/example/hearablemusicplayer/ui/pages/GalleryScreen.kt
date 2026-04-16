
package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.playlist.Playlist
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.musiclist.CurrentPlayingConfig
import com.example.hearablemusicplayer.ui.components.musiclist.EditConfig
import com.example.hearablemusicplayer.ui.components.musiclist.GalleryItemOptions
import com.example.hearablemusicplayer.ui.components.musiclist.HeaderConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemVariant
import com.example.hearablemusicplayer.ui.components.musiclist.ListConfig
import com.example.hearablemusicplayer.ui.components.musiclist.MusicList
import com.example.hearablemusicplayer.ui.components.musiclist.MusicListCallbacksAdapter
import com.example.hearablemusicplayer.ui.components.musiclist.galleryPresetMusicListConfig
import com.example.hearablemusicplayer.ui.components.musiclist.indexJumpConfigForOrderBy
import com.example.hearablemusicplayer.ui.controller.DialogManager
import com.example.hearablemusicplayer.ui.dialogs.ConfirmDialog
import com.example.hearablemusicplayer.ui.pages.base.TabScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.UiState
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.DialogManagerViewModel
import com.example.hearablemusicplayer.ui.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaybackViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistQueueViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

@OptIn(UnstableApi::class)
@Composable
fun GalleryScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playbackViewModel: PlaybackViewModel,
    playlistQueueViewModel: PlaylistQueueViewModel,
    dialogViewModel: DialogViewModel,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    dialogManagerViewModel: DialogManagerViewModel = hiltViewModel(),
    navController: NavBackStack<NavKey>
) {
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    val musicInfoList by libraryViewModel.allMusic.collectAsState()
    val currentPlayingMusic by playlistQueueViewModel.currentPlayingMusic.collectAsState()
    val selectedGenre by libraryViewModel.orderBy.collectAsState("title")
    val selectedOrder by libraryViewModel.orderType.collectAsState("ASC")
    val userCustomPlaylistsState by playlistViewModel.userCustomPlaylistsState.collectAsState()
    val userCustomPlaylists = (userCustomPlaylistsState as? UiState.Success)?.data ?: emptyList()
    val currentPlayingIndex = musicInfoList.indexOfFirst { it.music.id == currentPlayingMusic?.music?.id }.takeIf { it >= 0 }
    val dialogManager = dialogManagerViewModel.dialogManager

    LaunchedEffect(Unit) {
        libraryViewModel.getAllMusic()
    }

    GalleryScreenContent(
        isPlaying = isPlaying,
        musicInfoList = musicInfoList,
        currentPlayingIndex = currentPlayingIndex,
        selectedGenre = selectedGenre,
        selectedOrder = selectedOrder,
        userCustomPlaylists = userCustomPlaylists,
        playWith = playlistQueueViewModel::playWith,
        addToPlaylist = playlistQueueViewModel::addToPlaylist,
        onFavorite = playlistQueueViewModel::updateMusicLikedStatus,
        onShare =  {  },
        onDetail = {
            navController.add(Routes.SongDetail(it.music.id))
        },
        onRemoveFromLibrary = { ids -> libraryViewModel.removeFromLibrary(ids) },
        onShufflePlay = {
            playlistQueueViewModel.addAllToPlaylistByShuffle(musicInfoList)
            navController.add(Routes.Player)
        },
        onOrderPlay = {
            playlistQueueViewModel.addAllToPlaylistInOrder(musicInfoList)
            navController.add(Routes.Player)
        },
        onFilterGenreChange = {
            libraryViewModel.updateOrderBy(it)
            libraryViewModel.getAllMusic()
        },
        onFilterOrderChange = {
            libraryViewModel.updateOrderType(it)
            libraryViewModel.getAllMusic()
        },
        dialogViewModel = dialogViewModel,
        playlistViewModel = playlistViewModel,
        dialogManager = dialogManager,
        navController = navController,
    )
}

@OptIn(UnstableApi::class)
@Composable
fun GalleryScreenContent(
    isPlaying: Boolean,
    musicInfoList: List<MusicInfo>,
    currentPlayingIndex: Int?,
    selectedGenre: String,
    selectedOrder: String,
    userCustomPlaylists: List<Playlist>,
    playWith: (MusicInfo) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit,
    onFavorite: (MusicInfo, Boolean) -> Unit,
    onShare: (MusicInfo) -> Unit,
    onDetail: (MusicInfo) -> Unit,
    onRemoveFromLibrary: (List<Long>) -> Unit,
    onShufflePlay: () -> Unit,
    onOrderPlay: () -> Unit,
    onFilterGenreChange: (String) -> Unit,
    onFilterOrderChange: (String) -> Unit,
    dialogViewModel: DialogViewModel,
    playlistViewModel: PlaylistViewModel,
    dialogManager: DialogManager,
    navController: NavBackStack<NavKey>
) {
    val haptic = rememberHapticFeedback()
    var showRemoveConfirmDialog by remember { mutableStateOf(false) }
    var pendingRemoveMusicInfo by remember { mutableStateOf<MusicInfo?>(null) }
    var showBatchRemoveConfirmDialog by remember { mutableStateOf(false) }
    var pendingBatchIds by remember { mutableStateOf<Set<Long>?>(null) }
    var deleteCounter by remember { mutableIntStateOf(0) }

    val callbacks = object : MusicListCallbacksAdapter() {
        override fun onItemClick(musicInfo: MusicInfo, index: Int) {
            haptic.performClick()
            playWith(musicInfo)
        }
        override fun onMenuClick(musicInfo: MusicInfo) {
            val menuConfig = DialogViewModel.MusicDetailMenuConfig(
                showAddToPlaylist = true,
                showAddToSpecificPlaylist = true,
                showShare = true,
                showViewDetail = true,
                showPlayNext = true,
                showRemoveFromCurrentPlaylist = false,
                showDelete = true
            )
            dialogViewModel.showMusicDetailDialog(musicInfo, menuConfig)
        }
        override fun onBatchAddToPlaylist(selectedIds: Set<Long>) {
            val selectedMusicList = musicInfoList.filter { it.music.id in selectedIds }
            if (selectedMusicList.isNotEmpty()) {
                // 显示播放列表选择弹窗
                dialogViewModel.showPlaylistPickerDialog(
                    playlists = userCustomPlaylists,
                    title = "选择播放列表",
                    onConfirm = { selectedPlaylist ->
                        // 批量添加歌曲到选择的播放列表
                        val itemsToAdd = selectedMusicList.map {
                            it.music.id to it.music.path
                        }
                        playlistViewModel.addItemsToPlaylist(
                            playlistId = selectedPlaylist.id,
                            items = itemsToAdd,
                            onComplete = {
                                dialogManager.showMessage("已添加 ${selectedMusicList.size} 首歌曲到播放列表")
                            }
                        )
                    }
                )
            }
        }
        override fun onBatchDelete(selectedIds: Set<Long>) {
            if (selectedIds.isEmpty()) return
            pendingBatchIds = selectedIds
            showBatchRemoveConfirmDialog = true
        }
    }
    val config = galleryPresetMusicListConfig(callbacks).copy(
        header = HeaderConfig.Full(
            selectedGenre = selectedGenre,
            selectedOrder = selectedOrder,
            onFilterGenreChange = onFilterGenreChange,
            onFilterOrderChange = onFilterOrderChange,
            onOrderPlay = onOrderPlay,
            onShufflePlay = onShufflePlay,
        ),
        item = ItemConfig(
            showIndex = true,
            showCheckbox = true,
            variant = ItemVariant.Gallery,
            galleryOptions = GalleryItemOptions(
                showPinButton = false,
                showRemoveButton = false,
                showMenuButton = true,
            ),
        ),
        list = ListConfig(
            enableLongPressToEnterEdit = true,
            bottomSpacerHeight = 88.dp
        ),
        edit = EditConfig(enabled = true),
        indexJump = indexJumpConfigForOrderBy(selectedGenre, selectedOrder),
        currentPlaying = CurrentPlayingConfig(
            index = currentPlayingIndex,
            autoScrollToCurrent = false,
        ),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            TabScreen(
                title = stringResource(R.string.title_gallery),
                hasSearchBotton = true,
                navController = navController
            ) {
                Column {
                    key(deleteCounter) {
                        MusicList(
                            musicInfoList = musicInfoList,
                            config = config,
                            modifier = Modifier.fillMaxSize(),
                            isPlaying = isPlaying,
                        )
                    }
                }
            }
        }
    }

    // 单项移除确认
    ConfirmDialog(
        visible = showRemoveConfirmDialog && pendingRemoveMusicInfo != null,
        title = stringResource(R.string.confirm_remove_from_library),
        message = "",
        onConfirm = {
            pendingRemoveMusicInfo?.let { info -> 
                onRemoveFromLibrary(listOf(info.music.id))
            }
            showRemoveConfirmDialog = false
            pendingRemoveMusicInfo = null
        },
        onDismiss = {
            showRemoveConfirmDialog = false
            pendingRemoveMusicInfo = null
        }
    )

    // 批量移除确认
    if (showBatchRemoveConfirmDialog && pendingBatchIds != null) {
        val count = pendingBatchIds!!.size
        ConfirmDialog(
            visible = true,
            title = stringResource(R.string.confirm_batch_remove_from_library, count),
            message = "",
            onConfirm = {
                onRemoveFromLibrary(pendingBatchIds!!.toList())
                deleteCounter++
                showBatchRemoveConfirmDialog = false
                pendingBatchIds = null
            },
            onDismiss = {
                showBatchRemoveConfirmDialog = false
                pendingBatchIds = null
            }
        )
    }
}

