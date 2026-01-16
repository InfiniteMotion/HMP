package com.example.hearablemusicplayer.ui.pages

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonOne
import com.example.hearablemusicplayer.ui.template.pages.TabScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel

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
    playWith: suspend (MusicInfo) -> Unit,
    recordPlayback: (Long, String?) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit,
    onShufflePlay: () -> Unit,
    onOrderPlay: () -> Unit,
    onFilterGenreChange: (String) -> Unit,
    onFilterOrderChange: (String) -> Unit,
    navController: NavController
) {
    val haptic = rememberHapticFeedback()
    TabScreen(
        title = "音乐库",
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
        Row(
            modifier = Modifier.padding(horizontal = 16.dp)
        ){
            MusicList(
                musicInfoList = musicInfoList,
                onItemClick = {
                    haptic.performClick()
                    playWith(it)
                    onNavigate(Routes.Player) },
                onAddToPlaylist = addToPlaylist,
                onMenuClick = {onNavigate(Routes.SongDetail(it.music.id))},
                showAddButton = true,
                showMenuButton = true,
                isPlaying = isPlaying,
            )
        }
    }
}
