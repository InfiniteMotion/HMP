package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.ui.components.Avatar
import com.example.hearablemusicplayer.ui.components.ListBanner
import com.example.hearablemusicplayer.ui.components.SearchArea
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel

@Composable
fun HomeScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Avatar(56,musicViewModel)
                Spacer(modifier = Modifier.width(16.dp))
                SearchArea(musicViewModel)
            }
            Row(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Make It Hearable！",
                    textAlign = TextAlign.Left,
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }
    }
}