package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.database.AppPreferences
import com.example.hearablemusicplayer.database.Music
import com.example.hearablemusicplayer.viewmodel.MusicViewModel

@Composable
fun ListBanner(
    bannerNameF: String,
    bannerNameS: String,
    musicList: List<Music>,
    tColor:Int,
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier
){
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier.width(20.dp)
            )
            Text(
                text = bannerNameF,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(
                modifier = Modifier.width(10.dp)
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colorResource(tColor))
            )
            Spacer(
                modifier = Modifier.width(10.dp)
            )
            Text(
                text = bannerNameS,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier=Modifier
                .padding(top = 10.dp, bottom = 16.dp)
                .fillMaxWidth(),

        ) {
            Banner(
                music = musicList[1],
                viewModel = viewModel,
                navController = navController,
                modifier = modifier
            )
            Banner(
                music = musicList[2],
                viewModel = viewModel,
                navController = navController,
                modifier = modifier
            )
            Banner(
                music = musicList[3],
                viewModel = viewModel,
                navController = navController,
                modifier = modifier
            )
        }
    }
}

@Composable
fun Banner(
    music:Music,
    viewModel: MusicViewModel,
    navController: NavController,
    modifier: Modifier
){
    val imageModifier = Modifier
        .size(100.dp)
        .shadow(elevation = 5.dp,shape = RoundedCornerShape(10.dp))
        .clip(RoundedCornerShape(10.dp))

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .clickable{
            AppPreferences.saveCurrentPlayingMusicId(music.id.toString())
            viewModel.changePlayingOn()
            navController.navigate("player")
        }
    ) {
        AsyncImage(
            model = music.albumArtUri,
            contentDescription = "Album art",
            contentScale = ContentScale.Crop, // 裁剪图片以适应目标区域
            modifier = imageModifier
        )
        Spacer(
            modifier=Modifier.height(5.dp)
        )
        if (music.title != null) {
            Text(
                text = music.title,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.widthIn(max = 120.dp), // 设置最大宽度
                overflow = TextOverflow.Ellipsis, // 超出部分显示省略号
                maxLines = 1 // 限制为单行
            )
        }
    }
}