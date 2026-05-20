package com.hmp.desktop.ui.library.pages
import com.hmp.desktop.ui.common.navigation.NavController

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import org.koin.compose.koinInject


import com.hmp.domain.music.MusicInfo
import com.hmp.domain.playlist.Playlist
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.library.pages.components.musiclist.CurrentPlayingConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.EditConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.GalleryItemOptions
import com.hmp.desktop.ui.library.pages.components.musiclist.HeaderConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.ItemConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.ItemVariant
import com.hmp.desktop.ui.library.pages.components.musiclist.ListConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.MusicList
import com.hmp.desktop.ui.library.pages.components.musiclist.MusicListCallbacksAdapter
import com.hmp.desktop.ui.library.pages.components.musiclist.galleryPresetMusicListConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.indexJumpConfigForOrderBy
import com.hmp.desktop.ui.common.dialogs.controller.DialogManager
import com.hmp.desktop.ui.common.dialogs.base.ConfirmDialog
import com.hmp.desktop.ui.common.pages.base.TabScreen
import com.hmp.desktop.ui.common.navigation.Routes
import com.hmp.desktop.ui.common.util.UiState
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.library.viewmodel.LibraryViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.playlist.viewmodel.PlaylistViewModel

@Composable
fun GalleryScreen(
    libraryViewModel: LibraryViewModel = koinInject(),
    playbackViewModel: PlaybackViewModel,
    playlistQueueViewModel: PlaylistQueueViewModel,
    dialogViewModel: DialogViewModel,
    playlistViewModel: PlaylistViewModel = koinInject(),
    dialogManagerViewModel: DialogManagerViewModel = koinInject(),
    navController: NavController
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
            navController.navigate(Routes.Library.SongDetail(it.music.id))
        },
        onRemoveFromLibrary = { ids -> libraryViewModel.removeFromLibrary(ids) },
        onShufflePlay = {
            playlistQueueViewModel.addAllToPlaylistByShuffle(musicInfoList)
            navController.navigate(Routes.Player.Player)
        },
        onOrderPlay = {
            playlistQueueViewModel.addAllToPlaylistInOrder(musicInfoList)
            navController.navigate(Routes.Player.Player)
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
    navController: NavController
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
            navController.navigate(Routes.Player.Player)
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
    val density = LocalDensity.current
    val isExpanded = with(density) { LocalWindowInfo.current.containerSize.width.toDp() } >= 840.dp
    val config = galleryPresetMusicListConfig(callbacks).copy(
        header = HeaderConfig.Full(
            selectedGenre = selectedGenre,
            selectedOrder = selectedOrder,
            onFilterGenreChange = onFilterGenreChange,
            onFilterOrderChange = onFilterOrderChange,
            onOrderPlay = onOrderPlay,
            onShufflePlay = onShufflePlay,
            singleRowFilter = isExpanded,
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
            columns = if (isExpanded) 2 else 1,
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
                title = stringResource(Res.string.title_gallery),
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
        title = stringResource(Res.string.confirm_remove_from_library),
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
            title = stringResource(Res.string.confirm_batch_remove_from_library, count),
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

