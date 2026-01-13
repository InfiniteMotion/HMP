package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.ListBanner
import com.example.hearablemusicplayer.ui.components.ListGroupName
import com.example.hearablemusicplayer.ui.template.pages.TabScreen
import com.example.hearablemusicplayer.ui.util.iconResId
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

@Composable
fun ListScreen(
    playlistViewModel: PlaylistViewModel,
    navController: NavController
) {
    TabScreen(
        title = "播放列表",
        hasSearchBotton = true,
        navController = navController
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ListGroupName(
                bannerNameF = stringResource(R.string.banner_daily_A),
                bannerNameS = stringResource(R.string.banner_daily_AA),
                themeColorResId = R.color.HDRed
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ListBanner(
                    listName = "默认列表",
                    listCoverUri = R.drawable.defaultlist,
                    playlistViewModel = playlistViewModel,
                    navController = navController
                )
                ListBanner(
                    listName = "红心列表",
                    listCoverUri = R.drawable.heartlist,
                    playlistViewModel = playlistViewModel,
                    navController = navController
                )
                ListBanner(
                    listName = "最近播放",
                    listCoverUri = R.drawable.historylist,
                    playlistViewModel = playlistViewModel,
                    navController = navController
                )
            }
            ListGroupName(
                bannerNameF = stringResource(R.string.banner_daily_B),
                bannerNameS = stringResource(R.string.banner_daily_BB),
                themeColorResId = R.color.HDBlue
            )
            val genreListState = rememberLazyListState()
            val genreFlingBehavior = rememberSnapFlingBehavior(lazyListState = genreListState)
            val genreList by playlistViewModel.genrePlaylistName.collectAsState()
            LazyRow(
                state = genreListState,
                flingBehavior = genreFlingBehavior,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(genreList.size) { index ->
                    ListBanner(
                        listName = genreList[index],
                        listCoverUri = genreList[index].iconResId,
                        playlistViewModel = playlistViewModel,
                        navController = navController
                    )
                }
            }
            ListGroupName(
                bannerNameF = stringResource(R.string.banner_daily_C),
                bannerNameS = stringResource(R.string.banner_daily_CC),
                themeColorResId = R.color.HDOrange
            )
            val moodListState = rememberLazyListState()
            val moodFlingBehavior = rememberSnapFlingBehavior(lazyListState = genreListState)
            val moodList by playlistViewModel.moodPlaylistName.collectAsState()
            LazyRow(
                state = moodListState,
                flingBehavior = moodFlingBehavior,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(moodList.size) { index ->
                    ListBanner(
                        listName = moodList[index],
                        listCoverUri = moodList[index].iconResId,
                        playlistViewModel = playlistViewModel,
                        navController = navController
                    )
                }
            }
            ListGroupName(
                bannerNameF = stringResource(R.string.banner_daily_D),
                bannerNameS = stringResource(R.string.banner_daily_DD),
                themeColorResId = R.color.HDGreen
            )
            val scenarioListState = rememberLazyListState()
            val scenarioFlingBehavior = rememberSnapFlingBehavior(lazyListState = scenarioListState)
            val scenarioList by playlistViewModel.scenarioPlaylistName.collectAsState()
            LazyRow(
                state = scenarioListState,
                flingBehavior = scenarioFlingBehavior,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scenarioList.size) { index ->
                    ListBanner(
                        listName = scenarioList[index],
                        listCoverUri = scenarioList[index].iconResId,
                        playlistViewModel = playlistViewModel,
                        navController = navController
                    )
                }
            }
            ListGroupName(
                bannerNameF = stringResource(R.string.banner_daily_E),
                bannerNameS = stringResource(R.string.banner_daily_EE),
                themeColorResId = R.color.HDPurple
            )
            val languageListState = rememberLazyListState()
            val languageFlingBehavior = rememberSnapFlingBehavior(lazyListState = languageListState)
            val languageList by playlistViewModel.languagePlaylistName.collectAsState()
            LazyRow(
                state = languageListState,
                flingBehavior = languageFlingBehavior,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(languageList.size) { index ->
                    ListBanner(
                        listName = languageList[index],
                        listCoverUri = languageList[index].iconResId,
                        playlistViewModel = playlistViewModel,
                        navController = navController
                    )
                }
            }
            ListGroupName(
                bannerNameF = stringResource(R.string.banner_daily_F),
                bannerNameS = stringResource(R.string.banner_daily_FF),
                themeColorResId = R.color.HDPurple
            )
            val eraListState = rememberLazyListState()
            val eraFlingBehavior = rememberSnapFlingBehavior(lazyListState = eraListState)
            val eraList by playlistViewModel.eraPlaylistName.collectAsState()
            LazyRow(
                state = eraListState,
                flingBehavior = eraFlingBehavior,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(eraList.size) { index ->
                    ListBanner(
                        listName = eraList[index],
                        listCoverUri = eraList[index].iconResId,
                        playlistViewModel = playlistViewModel,
                        navController = navController
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
