package com.hmp.desktop.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.usecase.GetAllMusicUseCase
import com.hmp.desktop.player.DesktopMusicController
import com.hmp.desktop.ui.components.musiclist.CurrentPlayingConfig
import com.hmp.desktop.ui.components.musiclist.MusicList
import com.hmp.desktop.ui.components.musiclist.MusicListCallbacksAdapter
import com.hmp.desktop.ui.components.musiclist.libraryPresetMusicListConfig
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onMusicClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val getAllMusicUseCase: GetAllMusicUseCase = koinInject()
    val controller: DesktopMusicController = koinInject()

    var musicList by remember { mutableStateOf<List<MusicInfo>>(emptyList()) }
    var orderBy by remember { mutableStateOf("title") }
    var orderType by remember { mutableStateOf("ASC") }

    LaunchedEffect(orderBy, orderType) {
        musicList = getAllMusicUseCase(orderBy, orderType)
    }

    val currentPlayingMusic = controller.currentPlayingMusic.collectAsState().value
    val currentIndexInList = musicList.indexOfFirst { it.music.id == currentPlayingMusic?.music?.id }
        .takeIf { it >= 0 }

    val config = libraryPresetMusicListConfig(
        selectedGenre = orderBy,
        selectedOrder = orderType,
        onFilterGenreChange = { orderBy = it },
        onFilterOrderChange = { orderType = it },
        onOrderPlay = {
            if (musicList.isNotEmpty()) {
                controller.setPlaylist(musicList, 0)
            }
        },
        onShufflePlay = {
            if (musicList.isNotEmpty()) {
                val shuffled = musicList.shuffled()
                controller.setPlaylist(shuffled, 0)
            }
        },
        callbacks = object : MusicListCallbacksAdapter() {
            override fun onItemClick(musicInfo: MusicInfo, index: Int) {
                controller.setPlaylist(musicList, index)
            }
        },
    ).copy(
        currentPlaying = CurrentPlayingConfig(
            index = currentIndexInList,
            autoScrollToCurrent = true,
        ),
    )

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("曲库") },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
            },
        )

        MusicList(
            musicInfoList = musicList,
            config = config,
            modifier = Modifier.weight(1f),
        )
    }
}
