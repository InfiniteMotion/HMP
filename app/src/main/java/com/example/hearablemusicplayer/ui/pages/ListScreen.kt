package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.ui.components.ListBanner
import com.example.hearablemusicplayer.ui.components.SearchArea
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel

@Composable
fun ListScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        musicViewModel.initPlaylists()
        val currentPlayList by musicViewModel.currentPlaylist.collectAsState(initial = null)
        val likedPlayList by musicViewModel.likedPlaylist.collectAsState(initial = null)
        val recentPlayList by musicViewModel.recentPlaylist.collectAsState(initial = null)
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                SearchArea(musicViewModel)
            }

            listOf(
                Triple(
                    currentPlayList,
                    stringResource(R.string.banner_daily_FF) to stringResource(R.string.banner_daily_FS),
                    R.color.HDRed
                ),
                Triple(
                    likedPlayList,
                    stringResource(R.string.banner_daily_SF) to stringResource(R.string.banner_daily_SS),
                    R.color.HDBlue
                ),
                Triple(
                    recentPlayList,
                    stringResource(R.string.banner_daily_TF) to stringResource(R.string.banner_daily_TS),
                    R.color.HDOrange
                )
            ).forEach { (list, names, color) ->
                list?.let {
                    ListBanner(
                        bannerNameF = names.first,
                        bannerNameS = names.second,
                        musicList = it,
                        themeColorResId = color,
                        navController = navController,
                        playControlViewModel = playControlViewModel
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}