package com.hmp.desktop.ui.settings.pages
import com.hmp.desktop.ui.common.navigation.NavController

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp


import com.hmp.domain.setting.model.ListeningDuration
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.common.components.Avatar
import com.hmp.desktop.ui.settings.components.ListeningChart
import com.hmp.desktop.ui.settings.components.SquareCard
import com.hmp.desktop.ui.common.pages.base.TabScreen
import com.hmp.desktop.ui.common.navigation.Routes
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.settings.viewmodel.RecommendationViewModel
import com.hmp.desktop.ui.settings.viewmodel.SettingsViewModel

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
                            navController.navigate(Routes.Settings.ProfileSettings)
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

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Transparent
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.padding(4.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            haptic.performClick()
                            navController.navigate(Routes.UserData.UserUsageData)
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.title_user_usage_data),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                painter = painterResource(Res.drawable.square_fill_grid_2x2),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ListeningChart(data = chartData)
                    }
                }

                // 第一行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp) // 列间距
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            stringResource(Res.string.theme_customization),
                            Res.drawable.slider_vertical_3,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.Custom.Custom)
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            stringResource(Res.string.audio_effects),
                            Res.drawable.identify_song,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.Player.AudioEffects)
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
                            stringResource(Res.string.ai_services),
                            Res.drawable.icloud,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.AI.AI)
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            stringResource(Res.string.title_settings),
                            Res.drawable.gearshape,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.Settings.Setting)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
