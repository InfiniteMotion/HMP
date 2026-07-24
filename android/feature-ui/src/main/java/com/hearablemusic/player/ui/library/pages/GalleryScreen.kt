package com.hearablemusic.player.ui.library.pages

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
import org.koin.androidx.compose.koinViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.playlist.Playlist
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.library.pages.components.musiclist.CurrentPlayingConfig
import com.hearablemusic.player.ui.library.pages.components.musiclist.EditConfig
import com.hearablemusic.player.ui.library.pages.components.musiclist.GalleryItemOptions
import com.hearablemusic.player.ui.library.pages.components.musiclist.HeaderConfig
import com.hearablemusic.player.ui.library.pages.components.musiclist.ItemConfig
import com.hearablemusic.player.ui.library.pages.components.musiclist.ItemVariant
import com.hearablemusic.player.ui.library.pages.components.musiclist.ListConfig
import com.hearablemusic.player.ui.library.pages.components.musiclist.MusicList
import com.hearablemusic.player.ui.library.pages.components.musiclist.MusicListCallbacksAdapter
import com.hearablemusic.player.ui.library.pages.components.musiclist.galleryPresetMusicListConfig
import com.hearablemusic.player.ui.library.pages.components.musiclist.indexJumpConfigForOrderBy
import com.hearablemusic.player.ui.common.dialogs.controller.DialogManager
import com.hearablemusic.player.ui.common.dialogs.base.ConfirmDialog
import com.hearablemusic.player.ui.common.pages.base.TabScreen
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.common.navigation.Routes
import com.hearablemusic.player.ui.common.util.UiState
import com.hearablemusic.player.ui.common.util.rememberHapticFeedback
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogViewModel
import com.hearablemusic.player.ui.library.viewmodel.LibraryViewModel
import com.hearablemusic.player.ui.player.viewmodel.PlaybackViewModel
import com.hearablemusic.player.ui.player.viewmodel.PlaylistQueueViewModel
import com.hearablemusic.player.ui.playlist.viewmodel.PlaylistViewModel

@OptIn(UnstableApi::class)
@Composable
fun GalleryScreen(
    libraryViewModel: LibraryViewModel = koinViewModel(),
    playbackViewModel: PlaybackViewModel,
    playlistQueueViewModel: PlaylistQueueViewModel,
    dialogViewModel: DialogViewModel,
    playlistViewModel: PlaylistViewModel = koinViewModel(),
    dialogManagerViewModel: DialogManagerViewModel = koinViewModel(),
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
            navController.add(Routes.Library.SongDetail(it.music.id))
        },
        onRemoveFromLibrary = { ids -> libraryViewModel.removeFromLibrary(ids) },
        onShufflePlay = {
            playlistQueueViewModel.addAllToPlaylistByShuffle(musicInfoList)
            navController.add(Routes.Player.Player)
        },
        onOrderPlay = {
            playlistQueueViewModel.addAllToPlaylistInOrder(musicInfoList)
            navController.add(Routes.Player.Player)
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
    val isLandscape = LocalWindowSizeInfo.current.isLandscape
    
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
                    title = stringResource(R.string.select_playlist),
                    onConfirm = { selectedPlaylist ->
                        // 批量添加歌曲到选择的播放列表
                        val itemsToAdd = selectedMusicList.map {
                            it.music.id to it.music.path
                        }
                        playlistViewModel.addItemsToPlaylist(
                            playlistId = selectedPlaylist.id,
                            items = itemsToAdd,
                            onComplete = {
                                dialogManager.showMessage(stringResource(R.string.added_n_songs_to_playlist, selectedMusicList.size))
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
            singleRowFilter = isLandscape,
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
            bottomSpacerHeight = 88.dp,
            columns = if (isLandscape) 2 else 1
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

