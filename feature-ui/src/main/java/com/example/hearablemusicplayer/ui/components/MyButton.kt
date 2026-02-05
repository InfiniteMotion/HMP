package com.example.hearablemusicplayer.ui.components

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.util.AnimationConfig
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback

@OptIn(UnstableApi::class)
@Composable
fun PlayControlButtonOne(
    selectedGenre: String,
    selectedOrder: String,
    onFilterGenreChange: (String) -> Unit,
    onFilterOrderChange: (String) -> Unit,
    onOrderPlay: () -> Unit,
    onShufflePlay: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    haptic.performClick()
                    expanded = !expanded
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.slider_vertical_3),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "select Button",
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(
                onClick = {
                    onOrderPlay()
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.order_play),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "order play Button",
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(
                onClick = {
                    haptic.performConfirm()
                    onShufflePlay()
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "shuffle play Button",
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(
                onClick = {
                    haptic.performLightClick()
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.lightbulb),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "self play Button",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        // 可隐藏的内容块
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(durationMillis = 300, easing = AnimationConfig.EASE_OUT)) +
                    fadeIn(animationSpec = tween(durationMillis = 300, easing = AnimationConfig.EASE_OUT)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = 300, easing = AnimationConfig.EASE_IN)) +
                    fadeOut(animationSpec = tween(durationMillis = 300, easing = AnimationConfig.EASE_IN))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Transparent,
            ) {
                val genres = listOf(
                    stringResource(R.string.sort_title) to "title",
                    stringResource(R.string.sort_artist) to "artist",
                    stringResource(R.string.sort_duration) to "duration",
                    stringResource(R.string.sort_size) to "fileSize",
                    stringResource(R.string.sort_play_count) to "playCount",
                    stringResource(R.string.sort_add_time) to "id"
                )
                val orders = listOf(
                    stringResource(R.string.order_asc) to "ASC",
                    stringResource(R.string.order_desc) to "DESC"
                )
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.sort),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        FlowRow(
                            maxItemsInEachRow = 3,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            genres.forEach { (genre,eGenre) ->
                                FilterChip(
                                    selected = selectedGenre == eGenre,
                                    onClick = {
                                        haptic.performLightClick()
                                        (if (selectedGenre == eGenre) null else eGenre)?.let {
                                            onFilterGenreChange(it)
                                        }
                                    },
                                    label = { Text(text = genre, style = MaterialTheme.typography.titleSmall) },
                                    // 选中状态样式
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.order),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            orders.forEach{ (order,eOrder) ->
                                FilterChip(
                                    selected = selectedOrder == eOrder,
                                    onClick = {
                                        haptic.performLightClick()
                                        (if (selectedOrder == eOrder) null else eOrder)?.let {
                                            onFilterOrderChange(it)
                                        }
                                    },
                                    label = { Text(text = order, style = MaterialTheme.typography.titleSmall) },
                                    // 选中状态样式
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayControlButtonTwo(
    onShufflePlay: () -> Unit,
    onOrderPlay: () -> Unit
) {
    val haptic = rememberHapticFeedback()
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                    onClick = {
                        haptic.performConfirm()
                        onShufflePlay()
                    },
            modifier = Modifier
                .size(32.dp)
            ) {
            Icon(
                painter = painterResource(R.drawable.shuffle),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "shuffle play Button",
                modifier = Modifier.size(24.dp),
            )
            }
            Spacer(modifier = Modifier.width(160.dp))
            IconButton(
                onClick = {
                    haptic.performConfirm()
                    onOrderPlay()
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.order_play),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "order play Button",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}



@Composable
fun BackButton(
    onClick: () -> Unit
){
    val haptic = rememberHapticFeedback()
    FilledIconButton(
        onClick = {
            haptic.performClick()
            onClick()
        },
        modifier = Modifier
            .size(32.dp), // Larger touch target
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.chevron_left),
            contentDescription = "Back Button",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SearchButton(
    navController: NavController
){
    val haptic = rememberHapticFeedback()
    Box(
        modifier = Modifier.size(48.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                shape = RoundedCornerShape(48),
                color = MaterialTheme.colorScheme.primary,
            ),
        contentAlignment = Alignment.Center
    ) {
        FilledIconButton(
            onClick = {
                haptic.performClick()
                navController.navigate(Routes.Search)
            },
            modifier = Modifier
                .size(48.dp), // Larger touch target
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.magnifyingglass),
                contentDescription = "Search Button",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
