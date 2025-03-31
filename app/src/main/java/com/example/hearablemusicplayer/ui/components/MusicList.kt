package com.example.hearablemusicplayer.ui.components

import MusicViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.model.Music
import com.example.hearablemusicplayer.repository.MusicRepository

@Composable
fun MusicList(viewModel: MusicViewModel) {
    val musicList by viewModel.musicList.collectAsState()

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(musicList) { music ->
            MusicItem(music)
        }
    }
}


@Composable
fun MusicItem(music: Music) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val defaultCoverBitmap: Bitmap = BitmapFactory.decodeResource(
                LocalContext.current.resources,
                R.drawable.example_cover_3 // 默认封面图片资源
            )
            Image(
                painter = BitmapPainter(music.albumCover?.asImageBitmap() ?: defaultCoverBitmap.asImageBitmap()),
                contentDescription = "Music Cover Art",
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )
            Text(text = "Music: ${music.title}")
            Text(text= "Artist: ${music.artist}")
            Text(text = "Album: ${music.album}")
            Text(text = "Duration: ${music.duration / 1000}s")
            Text(text= "Path: ${music.path}")
        }
    }
}