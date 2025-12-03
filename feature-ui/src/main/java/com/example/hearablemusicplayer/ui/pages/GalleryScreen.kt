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
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonOne
import com.example.hearablemusicplayer.ui.template.pages.TabScreen
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel

@OptIn(UnstableApi::class)
@Composable
fun GalleryScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val musicInfoList by musicViewModel.allMusic.collectAsState()

    LaunchedEffect(Unit) {
        musicViewModel.getAllMusic()
        playControlViewModel.toastEvent.collect { event ->
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
    }

    TabScreen(
        title = "音乐库",
        hasSearchBotton = true,
        navController = navController
    ) {
        Row(
            modifier = Modifier.padding(bottom = 16.dp)
        ){
            PlayControlButtonOne(musicViewModel,playControlViewModel,navController)
        }

        MusicList(
            musicInfoList = musicInfoList,
            navigate = navController::navigate,
            playWith = playControlViewModel::playWith,
            recordPlayback = playControlViewModel::recordPlayback,
            addToPlaylist = playControlViewModel::addToPlaylist,
        )
    }
}
