package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.GalleryShiftButton
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonOne
import com.example.hearablemusicplayer.ui.components.SearchArea
import com.example.hearablemusicplayer.viewmodel.MusicViewModel

@Composable
fun GalleryScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Row(
                modifier = Modifier.padding(vertical = 16.dp,horizontal = 16.dp)
            ){
                GalleryShiftButton()
                Spacer(modifier = Modifier.width(16.dp))
                SearchArea(viewModel)
            }
            Row(
                modifier = Modifier.padding(vertical = 16.dp)
            ){
                PlayControlButtonOne()
            }
            MusicList(viewModel, navController)
        }
    }
}