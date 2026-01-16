package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SearchViewModel

@OptIn(UnstableApi::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel(),
    navController: NavController
){
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchResults by searchViewModel.searchResults.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        searchViewModel.searchMusic(searchQuery)
    }

    SearchScreenContent(
        isPlaying = isPlaying,
        searchQuery = searchQuery,
        searchResults = searchResults,
        onSearchQueryChange = {
            searchQuery = it
            searchViewModel.searchMusic(it)
        },
        onBackClick = { navController.popBackStack() },
        onNavigate = navController::navigate,
        playWith = playControlViewModel::playWith,
        recordPlayback = playControlViewModel::recordPlayback,
        addToPlaylist = playControlViewModel::addToPlaylist
    )
}

@OptIn(UnstableApi::class)
@Composable
fun SearchScreenContent(
    isPlaying: Boolean,
    searchQuery: String,
    searchResults: List<MusicInfo>,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onNavigate: (Any) -> Unit,
    playWith: suspend (MusicInfo) -> Unit,
    recordPlayback: (Long, String?) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit
) {
    val haptic = rememberHapticFeedback()
    // 使用SubScreen模板
    SubScreen(
        onBackClick = onBackClick,
        title = "搜索"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = {
                    Text(
                        "搜索您的音乐",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.magnifyingglass),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "搜索"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Transparent, // 聚焦时下划线颜色
                    unfocusedIndicatorColor = Transparent, // 未聚焦时下划线颜色
                    disabledIndicatorColor = Transparent // 禁用时下划线颜色
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            MusicList(
                musicInfoList = searchResults,
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
