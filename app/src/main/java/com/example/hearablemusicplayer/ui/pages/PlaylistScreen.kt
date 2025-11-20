package com.example.hearablemusicplayer.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.BackButton
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonTwo
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel

@Composable
fun PlaylistScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
    navController: NavController,
) {
    var visible by remember { mutableStateOf(false) }
    val playlistName by musicViewModel.selectedPlaylistName.collectAsState()
    val playlist by musicViewModel.selectedPlaylist.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.align(Alignment.CenterStart)
                    ){
                        BackButton(navController)
                    }
                    Text(
                        playlistName,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
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
    }
}