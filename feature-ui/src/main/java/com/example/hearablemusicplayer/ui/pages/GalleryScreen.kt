package com.example.hearablemusicplayer.ui.pages

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.musiclist.MusicList
import com.example.hearablemusicplayer.ui.components.musiclist.MusicListCallbacksAdapter
import com.example.hearablemusicplayer.ui.components.musiclist.CurrentPlayingConfig
import com.example.hearablemusicplayer.ui.components.musiclist.EditConfig
import com.example.hearablemusicplayer.ui.components.musiclist.GalleryItemOptions
import com.example.hearablemusicplayer.ui.components.musiclist.HeaderConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ListConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemVariant
import com.example.hearablemusicplayer.ui.components.musiclist.galleryPresetMusicListConfig
import com.example.hearablemusicplayer.ui.components.musiclist.indexJumpConfigForOrderBy
import com.example.hearablemusicplayer.ui.dialogs.MusicDetailDialog
import com.example.hearablemusicplayer.ui.pages.base.TabScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@OptIn(UnstableApi::class)
@Composable
fun GalleryScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel(),
    navController: NavBackStack<NavKey>
) {
    val context = LocalContext.current
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    val musicInfoList by libraryViewModel.allMusic.collectAsState()
    val currentPlayingMusic by playControlViewModel.currentPlayingMusic.collectAsState()
    val selectedGenre by libraryViewModel.orderBy.collectAsState("title")
    val selectedOrder by libraryViewModel.orderType.collectAsState("ASC")
    val currentPlayingIndex = musicInfoList.indexOfFirst { it.music.id == currentPlayingMusic?.music?.id }.takeIf { it >= 0 }

    LaunchedEffect(Unit) {
        libraryViewModel.getAllMusic()
        playControlViewModel.toastEvent.collect { event ->
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
    }

    GalleryScreenContent(
        isPlaying = isPlaying,
        musicInfoList = musicInfoList,
        currentPlayingIndex = currentPlayingIndex,
        selectedGenre = selectedGenre,
        selectedOrder = selectedOrder,
        onNavigate = navController::add,
        playWith = playControlViewModel::playWith,
        addToPlaylist = playControlViewModel::addToPlaylist,
        onFavorite = playControlViewModel::updateMusicLikedStatus,
        onShare =  {  },
        onDetail = {
            navController.add(Routes.SongDetail(it.music.id))
        },
        onRemoveFromLibrary = { ids -> libraryViewModel.removeFromLibrary(ids) },
        onShufflePlay = {
            playControlViewModel.addAllToPlaylistByShuffle(musicInfoList)
            navController.add(Routes.Player)
        },
        onOrderPlay = {
            playControlViewModel.addAllToPlaylistInOrder(musicInfoList)
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
    onNavigate: (NavKey) -> Unit,
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
    navController: NavBackStack<NavKey>
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedMusicInfo by remember { mutableStateOf<MusicInfo?>(null) }
    var showRemoveConfirmDialog by remember { mutableStateOf(false) }
    var pendingRemoveMusicInfo by remember { mutableStateOf<MusicInfo?>(null) }
    var showBatchRemoveConfirmDialog by remember { mutableStateOf(false) }
    var pendingBatchIds by remember { mutableStateOf<Set<Long>?>(null) }
    var deleteCounter by remember { mutableStateOf(0) }
    val hazeState = rememberHazeState()

    val callbacks = object : MusicListCallbacksAdapter() {
        override fun onItemClick(musicInfo: MusicInfo, index: Int) {
            haptic.performClick()
            playWith(musicInfo)
        }
        override fun onMenuClick(musicInfo: MusicInfo) {
            selectedMusicInfo = musicInfo
            showDetailDialog = true
        }
        override fun onBatchAddToPlaylist(selectedIds: Set<Long>) {
            musicInfoList
                .filter { it.music.id in selectedIds }
                .forEach { addToPlaylist(it) }
            if (selectedIds.isNotEmpty()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.batch_add_to_playlist_done, selectedIds.size),
                    Toast.LENGTH_SHORT
                ).show()
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
        list = ListConfig(enableLongPressToEnterEdit = true),
        edit = EditConfig(enabled = true),
        indexJump = indexJumpConfigForOrderBy(selectedGenre, selectedOrder),
        currentPlaying = CurrentPlayingConfig(
            index = currentPlayingIndex,
            autoScrollToCurrent = false,
        ),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)) {
            TabScreen(
                title = stringResource(R.string.title_gallery),
                hasSearchBotton = true,
                navController = navController
            ) {
                androidx.compose.runtime.key(deleteCounter) {
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

    // 音乐详情弹窗（ScrimDialog 在 MusicDetailDialog 内部）
    if (showDetailDialog) {
        MusicDetailDialog(
            musicInfo = selectedMusicInfo,
            onDismiss = {
                showDetailDialog = false
                selectedMusicInfo = null
            },
            onPlay = {
                selectedMusicInfo?.let { musicInfo ->
                    playWith(musicInfo)
                    showDetailDialog = false
                    selectedMusicInfo = null
                }
            },
            onAddToPlaylist = {
                selectedMusicInfo?.let { musicInfo ->
                    addToPlaylist(musicInfo)
                    showDetailDialog = false
                    selectedMusicInfo = null
                }
            },
            onFavorite = {
                selectedMusicInfo?.let { musicInfo ->
                    musicInfo.userInfo?.liked?.let { onFavorite(musicInfo, !it) }
                    showDetailDialog = false
                    selectedMusicInfo = null
                }
            },
            onShare = {
                selectedMusicInfo?.let { musicInfo ->
                    onShare(musicInfo)
                    showDetailDialog = false
                    selectedMusicInfo = null
                }
            },
            onDetail = {
                selectedMusicInfo?.let { musicInfo ->
                    onDetail(musicInfo)
                    showDetailDialog = false
                    selectedMusicInfo = null
                }
            },
            onRemove = {
                selectedMusicInfo?.let { music ->
                    pendingRemoveMusicInfo = music
                    showRemoveConfirmDialog = true
                }
                showDetailDialog = false
                selectedMusicInfo = null
            },
            hazeState = hazeState
        )
    }

    // 单项移除确认
    if (showRemoveConfirmDialog && pendingRemoveMusicInfo != null) {
        AlertDialog(
            onDismissRequest = {
                showRemoveConfirmDialog = false
                pendingRemoveMusicInfo = null
            },
            title = { Text(stringResource(R.string.confirm_remove_from_library)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingRemoveMusicInfo?.let { info ->
                            onRemoveFromLibrary(listOf(info.music.id))
                        }
                        showRemoveConfirmDialog = false
                        pendingRemoveMusicInfo = null
                    }
                ) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRemoveConfirmDialog = false
                    pendingRemoveMusicInfo = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // 批量移除确认
    if (showBatchRemoveConfirmDialog && pendingBatchIds != null) {
        val count = pendingBatchIds!!.size
        AlertDialog(
            onDismissRequest = {
                showBatchRemoveConfirmDialog = false
                pendingBatchIds = null
            },
            title = { Text(stringResource(R.string.confirm_batch_remove_from_library, count)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveFromLibrary(pendingBatchIds!!.toList())
                        deleteCounter++
                        showBatchRemoveConfirmDialog = false
                        pendingBatchIds = null
                    }
                ) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBatchRemoveConfirmDialog = false
                    pendingBatchIds = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
