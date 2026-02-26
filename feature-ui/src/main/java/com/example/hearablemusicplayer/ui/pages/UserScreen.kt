package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.setting.model.ListeningDuration
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.Avatar
import com.example.hearablemusicplayer.ui.components.ListeningChart
import com.example.hearablemusicplayer.ui.components.SquareCard
import com.example.hearablemusicplayer.ui.components.TitleWidget
import com.example.hearablemusicplayer.ui.pages.base.TabScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel

@Composable
fun UserScreen(
    settingsViewModel: SettingsViewModel,
    recommendationViewModel: RecommendationViewModel,
    navController: NavController
) {

    val userName by settingsViewModel.userName.collectAsState("")
    val avatarUri by settingsViewModel.avatarUri.collectAsState("")
    val listeningData by recommendationViewModel.recentListeningDurations.collectAsState()

    LaunchedEffect(Unit) {
        settingsViewModel.getAvatarUri()
    }
    
    UserScreenContent(
        userName = userName,
        avatarUri = avatarUri,
        listeningData = listeningData,
        navController = navController
    )
}

@Composable
fun UserScreenContent(
    userName: String?,
    avatarUri: String,
    listeningData: List<ListeningDuration>,
    navController: NavController
) {
    TabScreen{
        val sortedData = listeningData.sortedBy { it.date }.takeLast(35) // 取最近35天
        val chartData = sortedData.map { ((it.duration / (1000 * 60)).toInt()) }
        val haptic = rememberHapticFeedback()

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp), // 增加水平边距
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp) // 行间距
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Transparent
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .clickable {
                            haptic.performClick()
                            navController.navigate(Routes.ProfileSettings)
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Avatar(
                            aSize = 100,
                            imageUri = avatarUri
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ){
                            userName?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                TitleWidget(
                    title = stringResource(R.string.listening_duration_chart_title),
                ) {
                    ListeningChart(data = chartData)
                }

                // 第一行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp) // 列间距
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            stringResource(R.string.theme_customization),
                            R.drawable.slider_vertical_3,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.Custom)
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            stringResource(R.string.audio_effects),
                            R.drawable.identify_song,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.AudioEffects)
                            }
                        )
                    }
                }
                
                // 第二行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp) // 列间距
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            stringResource(R.string.ai_services),
                            R.drawable.icloud,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.AI)
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            stringResource(R.string.title_settings),
                            R.drawable.gearshape,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.Setting)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
