package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.compose.runtime.rememberCoroutineScope
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.musiclist.CurrentPlayingConfig
import com.example.hearablemusicplayer.ui.components.musiclist.EditConfig
import com.example.hearablemusicplayer.ui.components.musiclist.FullItemOptions
import com.example.hearablemusicplayer.ui.components.musiclist.HeaderConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemVariant
import com.example.hearablemusicplayer.ui.components.musiclist.MusicList
import com.example.hearablemusicplayer.ui.components.musiclist.MusicListCallbacksAdapter
import com.example.hearablemusicplayer.ui.components.musiclist.defaultMusicListConfig
import kotlinx.coroutines.launch
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaybackViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistQueueViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SearchViewModel

import androidx.compose.ui.res.stringResource
import com.example.hearablemusicplayer.ui.pages.base.SubScreen

@OptIn(UnstableApi::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel(),
    playbackViewModel: PlaybackViewModel = hiltViewModel(),
    playlistQueueViewModel: PlaylistQueueViewModel = hiltViewModel(),
    dialogViewModel: DialogViewModel = hiltViewModel(),
    navController: NavBackStack<NavKey>
){
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchResults by searchViewModel.searchResults.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        searchViewModel.searchMusic(searchQuery)
    }

    val currentPlayingMusic by playlistQueueViewModel.currentPlayingMusic.collectAsState(null)
    SearchScreenContent(
        isPlaying = isPlaying,
        searchQuery = searchQuery,
        searchResults = searchResults,
        currentPlayingMusic = currentPlayingMusic,
        onSearchQueryChange = {
            searchQuery = it
            searchViewModel.searchMusic(it)
        },
        onBackClick = { navController.removeLastOrNull() },
        playWith = playlistQueueViewModel::playWith,
        addToPlaylist = playlistQueueViewModel::addToPlaylist,
        onShowMusicDetailDialog = dialogViewModel::showMusicDetailDialog
    )
}

@OptIn(UnstableApi::class)
@Composable
fun SearchScreenContent(
    isPlaying: Boolean,
    searchQuery: String,
    searchResults: List<MusicInfo>,
    currentPlayingMusic: MusicInfo?,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    playWith: suspend (MusicInfo) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit,
    onShowMusicDetailDialog: (MusicInfo) -> Unit
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val currentPlayingIndex = searchResults.indexOfFirst { it.music.id == currentPlayingMusic?.music?.id }.takeIf { it >= 0 }
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
        title = stringResource(R.string.search)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = {
                    Text(
                        stringResource(R.string.search_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.magnifyingglass),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = stringResource(R.string.search)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Transparent,
                    unfocusedIndicatorColor = Transparent,
                    disabledIndicatorColor = Transparent
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            MusicList(
                musicInfoList = searchResults,
                config = config,
                modifier = Modifier.fillMaxWidth().weight(1f),
                isPlaying = isPlaying,
            )
        }
    }
}
