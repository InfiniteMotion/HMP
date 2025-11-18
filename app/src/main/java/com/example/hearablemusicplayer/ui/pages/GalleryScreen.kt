package com.example.hearablemusicplayer.ui.pages

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonOne
import com.example.hearablemusicplayer.ui.components.SearchButton
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel

@OptIn(UnstableApi::class)
@Composable
fun GalleryScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
) {
    var visible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val musicInfoList by musicViewModel.allMusic.collectAsState()

    LaunchedEffect(Unit) {
        visible = true
        musicViewModel.getAllMusic()
        playControlViewModel.toastEvent.collect { event ->
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = "音乐库",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                    Spacer(modifier = Modifier.width(144.dp))
                    SearchButton(navController)
                    Spacer(modifier = Modifier.width(16.dp))

                }
                Row(
                    modifier = Modifier.padding(bottom = 16.dp)
                ){
                    PlayControlButtonOne(musicViewModel,playControlViewModel,navController)
                }
                MusicList(musicInfoList,playControlViewModel,navController)
            }
        }
    }
}