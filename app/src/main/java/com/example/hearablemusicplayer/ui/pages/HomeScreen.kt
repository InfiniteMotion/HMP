package com.example.hearablemusicplayer.ui.pages

import MusicViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.hearablemusicplayer.ui.components.MusicList

@Composable
fun HomeScreen(viewModel: MusicViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MusicList(viewModel)
    }
}