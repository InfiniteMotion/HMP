package com.example.hearablemusicplayer.ui.pages

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonTwo
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel

@OptIn(UnstableApi::class)
@Composable
fun PlaylistScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
    navController: NavController,
) {
    val playlistName by musicViewModel.selectedPlaylistName.collectAsState()
    val playlist by musicViewModel.selectedPlaylist.collectAsState(initial = emptyList())
    SubScreen(
        navController = navController,
        title = playlistName
    ) {
        PlayControlButtonTwo(
            playlist,
            playControlViewModel,
            navController
        )
        Spacer(modifier = Modifier.height(32.dp))
        MusicList(
            playlist,
            playControlViewModel,
            navController
        )
    }
}
