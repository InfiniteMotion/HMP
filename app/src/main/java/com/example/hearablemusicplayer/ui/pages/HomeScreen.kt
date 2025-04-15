package com.example.hearablemusicplayer.ui.pages

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.ui.components.Avatar
import com.example.hearablemusicplayer.ui.components.ListBanner
import com.example.hearablemusicplayer.ui.components.SearchArea
import com.example.hearablemusicplayer.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val musicList by viewModel.musicList.collectAsState()
        val musicListS by viewModel.musicListS.collectAsState()
        val musicListT by viewModel.musicListT.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.getRandomMusic()
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Avatar(56)
                Spacer(modifier = Modifier.width(16.dp))
                SearchArea(viewModel)
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

            listOf(
                Triple(
                    musicList,
                    stringResource(R.string.banner_daily_FF) to stringResource(R.string.banner_daily_FS),
                    R.color.HDRed
                ),
                Triple(
                    musicListS,
                    stringResource(R.string.banner_daily_SF) to stringResource(R.string.banner_daily_SS),
                    R.color.HDBlue
                ),
                Triple(
                    musicListT,
                    stringResource(R.string.banner_daily_TF) to stringResource(R.string.banner_daily_TS),
                    R.color.HDOrange
                )
            ).forEach { (list, names, color) ->
                list?.let {
                    ListBanner(
                        bannerNameF = names.first,
                        bannerNameS = names.second,
                        musicList = it,
                        tColor = color,
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}