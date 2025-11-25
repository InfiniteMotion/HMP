package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback

@Composable
fun PlayControlButtonOne(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGenre by musicViewModel.orderBy.collectAsState("title")
    val selectedOrder by musicViewModel.orderType.collectAsState("ASC")
    val playlist by musicViewModel.allMusic.collectAsState()
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
                    playControlViewModel.addAllToPlaylistInOrder(playlist)
                    navController.navigate("player")
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
                    playControlViewModel.addAllToPlaylistByShuffle(playlist)
                    navController.navigate("player")
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
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val genres = listOf(
                    "歌曲名" to "title",
                    "歌手名" to "artist",
                    "时长" to "duration",
                    "大小" to "fileSize",
                    "播放次数" to "playCount",
                    "添加时间" to "id"
                )
                val orders = listOf(
                    "升序" to "ASC",
                    "降序" to "DESC"
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
                            text = "排序",
                            style = MaterialTheme.typography.titleMedium,
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
                                            musicViewModel.updateOrderBy(
                                                it
                                            )
                                            musicViewModel.getAllMusic()
                                        }
                                    },
                                    label = { Text(text = genre, style = MaterialTheme.typography.titleSmall) },
                                    // 选中状态样式
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
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
                            text = "方式",
                            style = MaterialTheme.typography.titleMedium,
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
                                            musicViewModel.updateOrderType(
                                                it
                                            )
                                            musicViewModel.getAllMusic()
                                        }
                                    },
                                    label = { Text(text = order, style = MaterialTheme.typography.titleSmall) },
                                    // 选中状态样式
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
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

@Composable
fun PlayControlButtonTwo(
    playlist: List<MusicInfo>,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
) {
    val haptic = rememberHapticFeedback()
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                    onClick = {
                        haptic.performConfirm()
                        playControlViewModel.addAllToPlaylistByShuffle(playlist)
                        navController.navigate("player")
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
                    playControlViewModel.addAllToPlaylistInOrder(playlist)
                    navController.navigate("player")
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
    navController: NavController
){
    val haptic = rememberHapticFeedback()
    IconButton(
        onClick = {
            haptic.performClick()
            navController.popBackStack()
        },
    ) {
        Icon(
            painter = painterResource(R.drawable.back_to),
            contentDescription = "Back Button",
            modifier = Modifier.size(48.dp),
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
        IconButton(
            onClick = {
                haptic.performClick()
                navController.navigate("search")
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.magnifyingglass),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "Search Button",
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
