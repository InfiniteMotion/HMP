package com.hearablemusic.player.ui.settings.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.components.Avatar
import com.hearablemusic.player.ui.common.design.dimens.LocalHMPDimens
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.common.layout.WindowWidthSizeClass
import com.hearablemusic.player.ui.common.navigation.Routes
import com.hearablemusic.player.ui.common.pages.base.TabScreen
import com.hearablemusic.player.ui.common.util.UiState
import com.hearablemusic.player.ui.common.util.rememberHapticFeedback
import com.hearablemusic.player.ui.settings.components.ListeningChart
import com.hearablemusic.player.ui.settings.viewmodel.RecommendationViewModel
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel
import com.hearablemusic.player.ui.settings.viewmodel.UserUsageDataViewModel
import com.hmp.domain.setting.model.ListeningDuration
import com.hmp.domain.setting.model.UserUsageAnalytics
import org.koin.androidx.compose.koinViewModel
import com.hearablemusic.player.ui.common.util.activityViewModel
import kotlin.math.abs

private data class SettingsEntry(
    val titleRes: Int,
    val icon: Int,
    val route: NavKey
)

private val settingsItems = listOf(
    SettingsEntry(R.string.theme_customization, R.drawable.slider_vertical_3, Routes.Custom.Custom),
    SettingsEntry(R.string.audio_effects, R.drawable.identify_song, Routes.Player.AudioEffects),
    SettingsEntry(R.string.ai_services, R.drawable.icloud, Routes.AI.AI),
    SettingsEntry(R.string.lyrics_settings, R.drawable.music_note_list, Routes.Settings.LyricsSettings),
    SettingsEntry(R.string.backup_settings, R.drawable.externaldrive, Routes.Settings.BackupSettings),
    SettingsEntry(R.string.library_settings, R.drawable.music, Routes.Settings.LibrarySettings),
)

@Composable
private fun SettingsListCard(
    navController: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticFeedback()
    val dimens = LocalHMPDimens.current
    Card(
        shape = RoundedCornerShape(dimens.corner.md),
        colors = CardDefaults.cardColors(containerColor = Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = dimens.spacing.sm)) {
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
                            navController.add(route)
                        }
                        .padding(horizontal = dimens.spacing.lg, vertical = dimens.spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(dimens.icon.md),
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
                        painter = painterResource(R.drawable.chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(dimens.icon.sm),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun UserScreen(
    settingsViewModel: SettingsViewModel = activityViewModel(),
    recommendationViewModel: RecommendationViewModel = activityViewModel(),
    navController: NavBackStack<NavKey>,
    usageDataViewModel: UserUsageDataViewModel = koinViewModel(),
) {
    val userName by settingsViewModel.userName.collectAsState("")
    val avatarUri by settingsViewModel.avatarUri.collectAsState("")
    val listeningData by recommendationViewModel.recentListeningDurations.collectAsState()
    val usageState by usageDataViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        settingsViewModel.getAvatarUri()
    }

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
    navController: NavBackStack<NavKey>
) {
    TabScreen{
        val sizeClass = LocalWindowSizeInfo.current.widthSizeClass
        val isLandscape = LocalWindowSizeInfo.current.isLandscape
        val dimens = LocalHMPDimens.current
        val horizontalPadding = when (sizeClass) {
            WindowWidthSizeClass.Expanded -> 48.dp
            WindowWidthSizeClass.Medium -> 32.dp
            WindowWidthSizeClass.Compact -> 16.dp
        }
        val sortedData = listeningData.sortedBy { it.date }.takeLast(35)
        val chartData = sortedData.map { ((it.duration / (1000 * 60)).toInt()) }
        val haptic = rememberHapticFeedback()

        val profileCard: @Composable () -> Unit = {
            Card(
                shape = RoundedCornerShape(dimens.corner.md),
                colors = CardDefaults.cardColors(containerColor = Transparent),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.clip(RoundedCornerShape(dimens.corner.md))
                    .clickable {
                        haptic.performClick()
                        navController.add(Routes.Settings.ProfileSettings)
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(dimens.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Avatar(aSize = dimens.component.sm.value.toInt(), imageUri = avatarUri)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){
                        userName?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.displayMedium,
                                fontSize = dimens.type.xl,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        if (isLandscape) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = dimens.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.spacing.xl)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacing.lg)
                ) {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing.lg)) {
                            profileCard()
                            SettingsListCard(navController = navController)
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    ) {
                        Card(
                            shape = RoundedCornerShape(dimens.corner.md),
                            colors = CardDefaults.cardColors(containerColor = Transparent),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(dimens.corner.md))
                                .clickable {
                                    haptic.performClick()
                                    navController.add(Routes.UserData.UserUsageData)
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(dimens.spacing.lg),
                                verticalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                Text(
                                    text = stringResource(R.string.listening_insights),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = dimens.type.md,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                val analytics = (usageState as? UiState.Success)?.data
                                if (analytics != null) {
                                    val weekTrendPct = if (analytics.lastWeekMinutes > 0) {
                                        ((analytics.thisWeekMinutes - analytics.lastWeekMinutes).toFloat() / analytics.lastWeekMinutes * 100).toInt()
                                    } else null

                                    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing.sm)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Column(horizontalAlignment = Alignment.Start) {
                                                Text(
                                                    text = stringResource(R.string.total_listening_minutes),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontSize = dimens.type.xs,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${analytics.totalListeningMinutes}",
                                                    style = MaterialTheme.typography.headlineLarge,
                                                    fontSize = dimens.type.xl,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            if (weekTrendPct != null) {
                                                Text(
                                                    text = "${stringResource(R.string.this_week_minutes)} ${analytics.thisWeekMinutes}${if (weekTrendPct >= 0) " ↑" else " ↓"} ${abs(weekTrendPct)}%",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontSize = dimens.type.sm,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(dimens.spacing.sm)
                                        ) {
                                            InsightPill(Modifier.weight(1f), stringResource(R.string.this_week_minutes), "${analytics.thisWeekMinutes}")
                                            InsightPill(Modifier.weight(1f), stringResource(R.string.last_week_minutes), "${analytics.lastWeekMinutes}")
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(dimens.spacing.sm)
                                        ) {
                                            InsightPill(Modifier.weight(1f), stringResource(R.string.sort_play_count), "${analytics.totalPlayCount}")
                                            InsightPill(Modifier.weight(1f), stringResource(R.string.skipped_count), "${analytics.totalSkipCount}")
                                            InsightPill(Modifier.weight(1f), stringResource(R.string.liked_status), "${analytics.likedCount}")
                                        }
                                    }
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.spacing.lg)
                ) {
                    profileCard()

                    Card(
                        shape = RoundedCornerShape(dimens.corner.md),
                        colors = CardDefaults.cardColors(containerColor = Transparent),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(dimens.corner.md))
                            .clickable {
                                haptic.performClick()
                                navController.add(Routes.UserData.UserUsageData)
                            }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = dimens.spacing.md)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.spacing.md),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.title_user_usage_data),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = dimens.type.md,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    painter = painterResource(R.drawable.square_fill_grid_2x2),
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.icon.sm),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(dimens.spacing.sm))
                            ListeningChart(data = chartData)
                        }
                    }

                    SettingsListCard(navController = navController)
                }
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

@Composable
private fun InsightPill(modifier: Modifier, label: String, value: String) {
    val dimens = LocalHMPDimens.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.corner.sm),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.spacing.md, vertical = dimens.spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontSize = dimens.type.sm,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = dimens.type.xs,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
