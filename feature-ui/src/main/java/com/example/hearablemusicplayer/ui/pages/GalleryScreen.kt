package com.example.hearablemusicplayer.ui.pages

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.GalleryList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonOne
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
    navController: NavController
) {
    val context = LocalContext.current
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    val musicInfoList by libraryViewModel.allMusic.collectAsState()
    val selectedGenre by libraryViewModel.orderBy.collectAsState("title")
    val selectedOrder by libraryViewModel.orderType.collectAsState("ASC")

    LaunchedEffect(Unit) {
        libraryViewModel.getAllMusic()
        playControlViewModel.toastEvent.collect { event ->
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
    }

    GalleryScreenContent(
        isPlaying = isPlaying,
        musicInfoList = musicInfoList,
        selectedGenre = selectedGenre,
        selectedOrder = selectedOrder,
        onNavigate = navController::navigate,
        playWith = playControlViewModel::playWith,
        recordPlayback = playControlViewModel::recordPlayback,
        addToPlaylist = playControlViewModel::addToPlaylist,
        onFavorite = playControlViewModel::updateMusicLikedStatus,
        onShare =  {  },
        onDetail = {
            navController.navigate(Routes.SongDetail(it.music.id))
        },
        onRemove = {  },
        onShufflePlay = {
            playControlViewModel.addAllToPlaylistByShuffle(musicInfoList)
            navController.navigate(Routes.Player)
        },
        onOrderPlay = {
            playControlViewModel.addAllToPlaylistInOrder(musicInfoList)
            navController.navigate(Routes.Player)
        },
        onFilterGenreChange = {
            libraryViewModel.updateOrderBy(it)
            libraryViewModel.getAllMusic()
        },
        onFilterOrderChange = {
            libraryViewModel.updateOrderType(it)
            libraryViewModel.getAllMusic()
        },
        navController = navController
    )
}

@OptIn(UnstableApi::class)
@Composable
fun GalleryScreenContent(
    isPlaying: Boolean,
    musicInfoList: List<MusicInfo>,
    selectedGenre: String,
    selectedOrder: String,
    onNavigate: (Any) -> Unit,
    playWith: (MusicInfo) -> Unit,
    recordPlayback: (Long, String?) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit,
    onFavorite: (MusicInfo, Boolean) -> Unit,
    onShare: (MusicInfo) -> Unit,
    onDetail: (MusicInfo) -> Unit,
    onRemove: (MusicInfo) -> Unit,
    onShufflePlay: () -> Unit,
    onOrderPlay: () -> Unit,
    onFilterGenreChange: (String) -> Unit,
    onFilterOrderChange: (String) -> Unit,
    navController: NavController
) {
    val haptic = rememberHapticFeedback()
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedMusicInfo by remember { mutableStateOf<MusicInfo?>(null) }
    val hazeState = rememberHazeState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)) {
            TabScreen(
                title = stringResource(R.string.title_gallery),
                hasSearchBotton = true,
                navController = navController
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp)
                ){
                    PlayControlButtonOne(
                        selectedGenre = selectedGenre,
                        selectedOrder = selectedOrder,
                        onFilterGenreChange = onFilterGenreChange,
                        onFilterOrderChange = onFilterOrderChange,
                        onOrderPlay = onOrderPlay,
                        onShufflePlay = onShufflePlay
                    )
                }
        
                // 使用 GalleryList 组件
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ){
                    GalleryList(
                        musicInfoList = musicInfoList,
                        onItemClick = { musicInfo ->
                            haptic.performClick()
                            playWith(musicInfo)
                        },
                        onMenuClick = { musicInfo ->
                            selectedMusicInfo = musicInfo
                            showDetailDialog = true
                        },
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )
            }
        }
    }

    // 音乐详情弹窗
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
                selectedMusicInfo?.let { musicInfo ->
                    onRemove(musicInfo)
                    showDetailDialog = false
                    selectedMusicInfo = null
                }
            },
            hazeState = hazeState
        )}
    }
}
