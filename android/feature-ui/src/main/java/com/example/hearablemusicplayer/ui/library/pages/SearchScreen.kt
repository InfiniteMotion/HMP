package com.example.hearablemusicplayer.ui.library.pages

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.hmp.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.common.components.base.UiStateContent
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.CurrentPlayingConfig
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.EditConfig
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.FullItemOptions
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.HeaderConfig
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.ItemConfig
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.ItemVariant
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.MusicList
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.MusicListCallbacksAdapter
import com.example.hearablemusicplayer.ui.library.pages.components.musiclist.defaultMusicListConfig
import kotlinx.coroutines.launch
import com.example.hearablemusicplayer.ui.common.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.common.util.UiState
import com.example.hearablemusicplayer.ui.common.dialogs.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.player.viewmodel.PlaybackViewModel
import com.example.hearablemusicplayer.ui.player.viewmodel.PlaylistQueueViewModel
import com.example.hearablemusicplayer.ui.library.viewmodel.SearchViewModel

import com.example.hearablemusicplayer.ui.common.pages.base.SubScreen

@OptIn(UnstableApi::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = koinViewModel(),
    playbackViewModel: PlaybackViewModel,
    playlistQueueViewModel: PlaylistQueueViewModel,
    dialogViewModel: DialogViewModel,
    navController: NavBackStack<NavKey>
){
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchState by searchViewModel.searchState.collectAsState()

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            searchViewModel.searchMusic(searchQuery)
        }
    }

    val currentPlayingMusic by playlistQueueViewModel.currentPlayingMusic.collectAsState(null)
    SearchScreenContent(
        isPlaying = isPlaying,
        searchQuery = searchQuery,
        searchState = searchState,
        currentPlayingMusic = currentPlayingMusic,
        onSearchQueryChange = {
            searchQuery = it
            if (it.isNotEmpty()) {
                searchViewModel.searchMusic(it)
            }
        },
        onBackClick = { navController.removeLastOrNull() },
        playWith = playlistQueueViewModel::playWith,
        addToPlaylist = playlistQueueViewModel::addToPlaylist,
        onShowMusicDetailDialog = { musicInfo ->
            val menuConfig = DialogViewModel.MusicDetailMenuConfig(
                showAddToPlaylist = true,
                showAddToSpecificPlaylist = true,
                showShare = true,
                showViewDetail = true,
                showPlayNext = true,
                showRemoveFromCurrentPlaylist = false,
                showDelete = false
            )
            dialogViewModel.showMusicDetailDialog(musicInfo, menuConfig)
        },
        onRetry = { searchViewModel.searchMusic(searchQuery) }
    )
}

@OptIn(UnstableApi::class)
@Composable
fun SearchScreenContent(
    isPlaying: Boolean,
    searchQuery: String,
    searchState: UiState<List<MusicInfo>>,
    currentPlayingMusic: MusicInfo?,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    playWith: suspend (MusicInfo) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit,
    onShowMusicDetailDialog: (MusicInfo) -> Unit,
    onRetry: () -> Unit
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
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
            if (searchQuery.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.magnifyingglass),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = stringResource(R.string.search),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = stringResource(R.string.search_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                UiStateContent(
                    state = searchState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onEmpty = {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_results_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                ) { searchResults ->
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
                            showAddToPlaylistInMenu = false,
                        ),
                    ),
                    edit = EditConfig(enabled = false),
                    currentPlaying = CurrentPlayingConfig(
                        index = currentPlayingIndex,
                        autoScrollToCurrent = false
                    ),
                )
                    MusicList(
                        musicInfoList = searchResults,
                        config = config,
                        modifier = Modifier.fillMaxWidth(),
                        isPlaying = isPlaying,
                    )
            }
            }
        }
    }
}
