package com.hmp.desktop.ui.settings.pages
import com.hmp.desktop.ui.common.navigation.NavController

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.hmp.desktop.ui.common.layout.widthSizeClass
import com.hmp.desktop.ui.common.layout.WindowWidthSizeClass


import com.hmp.domain.setting.model.ListeningDuration
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.common.components.Avatar
import com.hmp.desktop.ui.settings.components.ListeningChart
import com.hmp.desktop.ui.common.pages.base.TabScreen
import com.hmp.desktop.ui.common.navigation.NavKey
import com.hmp.desktop.ui.common.navigation.Routes
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.settings.viewmodel.RecommendationViewModel
import com.hmp.desktop.ui.settings.viewmodel.SettingsViewModel
import com.hmp.desktop.ui.settings.viewmodel.UserUsageDataViewModel
import com.hmp.desktop.ui.common.util.UiState
import com.hmp.domain.setting.model.UserUsageAnalytics
import org.koin.compose.koinInject
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.abs

private data class SettingsEntry(
    val titleRes: StringResource,
    val icon: DrawableResource,
    val route: NavKey
)

private val settingsItems = listOf(
    SettingsEntry(Res.string.theme_customization, Res.drawable.slider_vertical_3, Routes.Custom.Custom),
    SettingsEntry(Res.string.audio_effects, Res.drawable.identify_song, Routes.Player.AudioEffects),
    SettingsEntry(Res.string.ai_services, Res.drawable.icloud, Routes.AI.AI),
    SettingsEntry(Res.string.backup_settings, Res.drawable.externaldrive, Routes.Settings.BackupSettings),
    SettingsEntry(Res.string.library_settings, Res.drawable.music, Routes.Settings.LibrarySettings),
)

@Composable
private fun SettingsListCard(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticFeedback()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            settingsItems.forEachIndexed { index, (titleRes, icon, route) ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performClick()
                            navController.navigate(route)
                        }
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(Res.drawable.chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun UserScreen(
    settingsViewModel: SettingsViewModel,
    recommendationViewModel: RecommendationViewModel,
    navController: NavController,
    usageDataViewModel: UserUsageDataViewModel = koinInject()
) {

    val userName by settingsViewModel.userName.collectAsState(settingsViewModel.userName.value)
    val avatarUri by settingsViewModel.avatarUri.collectAsState(settingsViewModel.avatarUri.value)
    val listeningData by recommendationViewModel.recentListeningDurations.collectAsState(recommendationViewModel.recentListeningDurations.value)
    val usageState by usageDataViewModel.uiState.collectAsState(usageDataViewModel.uiState.value)

    UserScreenContent(
        userName = userName,
        avatarUri = avatarUri,
        listeningData = listeningData,
        usageState = usageState,
        navController = navController
    )
}

@Composable
fun UserScreenContent(
    userName: String?,
    avatarUri: String,
    listeningData: List<ListeningDuration>,
    usageState: UiState<UserUsageAnalytics>,
    navController: NavController
) {
    TabScreen {
        val sortedData = listeningData.sortedBy { it.date }.takeLast(35)
        val chartData = sortedData.map { ((it.duration / (1000 * 60)).toInt()) }
        val haptic = rememberHapticFeedback()

        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val windowWidthDp = with(density) { windowInfo.containerSize.width.toDp() }
        val sizeClass = widthSizeClass(windowWidthDp)

        val profileCard: @Composable () -> Unit = {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Transparent),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .clickable {
                        haptic.performClick()
                        navController.navigate(Routes.Settings.ProfileSettings)
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(aSize = 100, imageUri = avatarUri)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
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
        }

        val usageCard: @Composable () -> Unit = {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Transparent),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        haptic.performClick()
                        navController.navigate(Routes.UserData.UserUsageData)
                    }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
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
        }

        if (sizeClass == WindowWidthSizeClass.Expanded) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        profileCard()
                        SettingsListCard(navController = navController)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Transparent),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    haptic.performClick()
                                    navController.navigate(Routes.UserData.UserUsageData)
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.listening_insights),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                val analytics = (usageState as? UiState.Success)?.data
                                if (analytics != null) {
                                    val weekTrendPct = if (analytics.lastWeekMinutes > 0) {
                                        ((analytics.thisWeekMinutes - analytics.lastWeekMinutes).toFloat() / analytics.lastWeekMinutes * 100).toInt()
                                    } else null

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(
                                                text = stringResource(Res.string.total_listening_minutes),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${analytics.totalListeningMinutes}",
                                                style = MaterialTheme.typography.headlineLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (weekTrendPct != null) {
                                            Text(
                                                text = buildString {
                                                    append(stringResource(Res.string.this_week_minutes))
                                                    append(" ")
                                                    append(analytics.thisWeekMinutes)
                                                    if (weekTrendPct >= 0) append(" ↑") else append(" ↓")
                                                    append(" ${abs(weekTrendPct)}%")
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        InsightPill(
                                            modifier = Modifier.weight(1f),
                                            label = stringResource(Res.string.this_week_minutes),
                                            value = "${analytics.thisWeekMinutes}"
                                        )
                                        InsightPill(
                                            modifier = Modifier.weight(1f),
                                            label = stringResource(Res.string.last_week_minutes),
                                            value = "${analytics.lastWeekMinutes}"
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        InsightPill(
                                            modifier = Modifier.weight(1f),
                                            label = stringResource(Res.string.sort_play_count),
                                            value = "${analytics.totalPlayCount}"
                                        )
                                        InsightPill(
                                            modifier = Modifier.weight(1f),
                                            label = stringResource(Res.string.skipped_count),
                                            value = "${analytics.totalSkipCount}"
                                        )
                                        InsightPill(
                                            modifier = Modifier.weight(1f),
                                            label = stringResource(Res.string.liked_status),
                                            value = "${analytics.likedCount}"
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                ListeningChart(data = chartData)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    profileCard()
                    usageCard()
                    SettingsListCard(navController = navController)
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun InsightPill(modifier: Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
