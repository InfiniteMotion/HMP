package com.hmp.desktop.ui.settings.pages
import com.hmp.desktop.ui.common.navigation.NavController

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject


import com.hmp.domain.setting.model.LabelCountEntry
import com.hmp.domain.setting.model.UserUsageAnalytics
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.common.layout.WindowWidthSizeClass
import com.hmp.desktop.ui.common.layout.widthSizeClass
import com.hmp.desktop.ui.player.components.MiniPlayerSafeSpacer
import com.hmp.desktop.ui.common.pages.base.SubScreen
import com.hmp.desktop.ui.common.navigation.Routes
import com.hmp.desktop.ui.common.util.UiState
import com.hmp.desktop.ui.common.components.SegmentedControl
import com.hmp.desktop.ui.common.components.SegmentedOption
import com.hmp.desktop.ui.common.util.HapticFeedbackHelper
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.settings.viewmodel.UserUsageDataViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun UserUsageDataScreen(
    navController: NavController,
    viewModel: UserUsageDataViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()

    SubScreen(
        onBackClick = { navController.popBackStack() },
        title = stringResource(Res.string.title_user_usage_data),
    ) {
        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val windowWidthDp = with(density) { windowInfo.containerSize.width.toDp() }
        val sizeClass = widthSizeClass(windowWidthDp)

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is UiState.Loading, is UiState.Idle -> {
                    UsageDataLoading()
                }
                is UiState.Success -> {
                    val analytics = state.data
                    val hasTasteCard = analytics.topGenres.isNotEmpty() || analytics.topMoods.isNotEmpty() || analytics.topScenarios.isNotEmpty()

                    if (sizeClass == WindowWidthSizeClass.Expanded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                OverviewCard(analytics = analytics)
                                if (hasTasteCard) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    TasteCard(analytics = analytics)
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                RankingAndHistoryCard(
                                    analytics = analytics,
                                    navController = navController,
                                    haptic = haptic
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                RecentHistoryBlock(
                                    analytics = analytics,
                                    navController = navController,
                                    haptic = haptic
                                )
                            }
                        }
                    } else {
                        OverviewCard(analytics = analytics)
                        Spacer(modifier = Modifier.height(20.dp))

                        if (hasTasteCard) {
                            TasteCard(analytics = analytics)
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        RankingAndHistoryCard(
                            analytics = analytics,
                            navController = navController,
                            haptic = haptic
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        RecentHistoryBlock(
                            analytics = analytics,
                            navController = navController,
                            haptic = haptic
                        )
                    }
                }
                is UiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.load() }) {
                            Text(stringResource(Res.string.refresh))
                        }
                    }
                }

                else -> {}
            }
            MiniPlayerSafeSpacer()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun OverviewCard(analytics: UserUsageAnalytics) {
    val weekTrendPct = if (analytics.lastWeekMinutes > 0) {
        ((analytics.thisWeekMinutes - analytics.lastWeekMinutes).toFloat() / analytics.lastWeekMinutes * 100).toInt()
    } else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(Res.string.listening_insights),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

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

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RateProgressRow(
                    label = stringResource(Res.string.completion_rate),
                    rate = analytics.completionRate,
                    valueLabel = "%.0f%%".format(analytics.completionRate * 100),
                    isPositive = true
                )
                RateProgressRow(
                    label = stringResource(Res.string.skip_rate),
                    rate = analytics.skipRate,
                    valueLabel = "%.0f%%".format(analytics.skipRate * 100),
                    isPositive = false
                )
            }

            if (analytics.playSourceBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                PlaySourcePieChart(entries = analytics.playSourceBreakdown.entries.toList())
            }
        }
    }
}

@Composable
private fun TasteCard(analytics: UserUsageAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(Res.string.taste_card),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (analytics.topGenres.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.top_genres),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LabelStackedBarWithLegend(entries = analytics.topGenres)
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (analytics.topMoods.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.top_moods),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LabelStackedBarWithLegend(entries = analytics.topMoods)
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (analytics.topScenarios.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.top_scenarios),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LabelStackedBarWithLegend(entries = analytics.topScenarios)
            }
        }
    }
}

@Composable
private fun RankingAndHistoryCard(
    analytics: UserUsageAnalytics,
    navController: NavController,
    haptic: HapticFeedbackHelper
) {
    var selectedTab by rememberSaveable { mutableStateOf("top_played") }
    val tabs = listOf(
        SegmentedOption("top_played", stringResource(Res.string.top_played)),
        SegmentedOption("top_artists", stringResource(Res.string.top_artists))
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(Res.string.ranking_and_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            SegmentedControl(
                modifier = Modifier.fillMaxWidth(),
                options = tabs,
                selectedOption = selectedTab,
                onOptionSelected = { selectedTab = it; haptic.performClick() }
            )
            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                "top_played" -> {
                    if (analytics.topPlayedSongs.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.usage_data_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        analytics.topPlayedSongs.forEachIndexed { index, entry ->
                            UsageListItem(
                                rank = index + 1,
                                title = entry.title,
                                subtitle = entry.artist,
                                trailing = "${entry.playCount}",
                                onClick = {
                                    haptic.performClick()
                                    navController.navigate(Routes.Library.SongDetail(entry.musicId))
                                }
                            )
                        }
                    }
                }
                "top_artists" -> {
                    if (analytics.topArtists.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.usage_data_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        analytics.topArtists.forEachIndexed { index, entry ->
                            UsageListItem(
                                rank = index + 1,
                                title = entry.artistName,
                                subtitle = null,
                                trailing = "${entry.playCount}",
                                onClick = {
                                    haptic.performClick()
                                    navController.navigate(Routes.Library.Artist(entry.artistName))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentHistoryBlock(
    analytics: UserUsageAnalytics,
    navController: NavController,
    haptic: HapticFeedbackHelper
) {
    SectionHeader(title = stringResource(Res.string.recent_history))
    Spacer(modifier = Modifier.height(8.dp))
    if (analytics.recentPlaybackWithTitle.isEmpty()) {
        Text(
            text = stringResource(Res.string.usage_data_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        analytics.recentPlaybackWithTitle.forEach { entry ->
            RecentPlaybackItem(
                title = entry.title,
                artist = entry.artist,
                playedAt = entry.playedAt,
                playDuration = entry.playDuration,
                isCompleted = entry.isCompleted,
                onClick = {
                    haptic.performClick()
                    navController.navigate(Routes.Library.SongDetail(entry.musicId))
                }
            )
        }
    }
}

@Composable
private fun RateProgressRow(
    label: String,
    rate: Float,
    valueLabel: String,
    isPositive: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { rate.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isPositive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** 播放来源：饼状图 + 图例 */
@Composable
private fun PlaySourcePieChart(entries: List<Map.Entry<String, Int>>) {
    val total = entries.sumOf { it.value }.coerceAtLeast(1)
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .padding(8.dp)
        ) {
            val side = size.minDimension
            val left = (size.width - side) / 2
            val top = (size.height - side) / 2
            var startAngle = -90f // 从 12 点方向开始，顺时针
            entries.forEachIndexed { index, (_, count) ->
                val sweepAngle = (count.toFloat() / total * 360f).coerceIn(0f, 360f)
                if (sweepAngle > 0f) {
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(left, top),
                        size = Size(side, side)
                    )
                    startAngle += sweepAngle
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            entries.forEachIndexed { index, (source, count) ->
                val pct = count.toFloat() / total
                val color = colors[index % colors.size]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                    Text(
                        text = source.ifEmpty { "—" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(pct * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** 听歌口味每组：横向堆积条 + 纵向图例（与 OverviewCard 下半区原样式一致） */
@Composable
private fun LabelStackedBarWithLegend(
    entries: List<LabelCountEntry>
) {
    val total = entries.sumOf { it.count }.coerceAtLeast(1)
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        if (total > 0) {
            entries.forEachIndexed { index, entry ->
                val pct = entry.count.toFloat() / total
                val color = colors[index % colors.size]
                Box(
                    modifier = Modifier
                        .weight(pct.coerceIn(0.001f, 1f))
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        entries.forEachIndexed { index, entry ->
            val pct = entry.count.toFloat() / total
            val color = colors[index % colors.size]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
                Text(
                    text = entry.labelDisplayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(pct * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UsageListItem(
    rank: Int = 0,
    title: String,
    subtitle: String?,
    trailing: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = if (rank in 1..3) 0.35f else 0.25f
            )
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (rank > 0) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (rank <= 3) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (rank <= 3) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(date)
}

@Composable
private fun RecentPlaybackItem(
    title: String,
    artist: String,
    playedAt: Long,
    playDuration: Long,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                    )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = formatDuration(playDuration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTimestamp(playedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(if (isCompleted) Res.string.completed else Res.string.incomplete),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun UsageDataLoading() {
    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.loading),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
