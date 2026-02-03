package com.example.hearablemusicplayer.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun MusicList(
    musicInfoList: List<MusicInfo>,
    onItemClick: suspend (MusicInfo) -> Unit,
    onAddToPlaylist: (MusicInfo) -> Unit,
    onMenuClick: (MusicInfo) -> Unit,
    showAddButton: Boolean,
    showMenuButton: Boolean,
    isPlaying: Boolean,
) {
    val haptic = rememberHapticFeedback()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = musicInfoList,
            key = { index, musicInfo -> "${musicInfo.music.id}_$index" }
        ) { index, musicInfo ->
            MusicItem(
                musicInfo = musicInfo,
                onItemClick = {
                    haptic.performClick()
                    coroutineScope.launch {
                        onItemClick(musicInfo)
                    }
                },
                onAddToPlaylist = { onAddToPlaylist(musicInfo) },
                onMenuClick = { onMenuClick(musicInfo) },
                showAddButton = showAddButton,
                showMenuButton = showMenuButton,
                isPlaying = isPlaying,
                modifier = Modifier
            )
        }
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun FixedMusicList(
    musicInfoList: List<MusicInfo>,
    onItemClick: suspend (MusicInfo) -> Unit,
    onAddToPlaylist: (MusicInfo) -> Unit,
    onMenuClick: (MusicInfo) -> Unit,
    showAddButton: Boolean,
    showMenuButton: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        musicInfoList.forEach { musicInfo ->
            MusicItem(
                musicInfo = musicInfo,
                onItemClick = {
                    haptic.performClick()
                    coroutineScope.launch {
                        onItemClick(musicInfo)
                    }
                },
                onAddToPlaylist = { onAddToPlaylist(musicInfo) },
                onMenuClick = { onMenuClick(musicInfo) },
                showAddButton = showAddButton,
                showMenuButton = showMenuButton,
                isPlaying = isPlaying,
                modifier = Modifier
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun MusicItem(
    musicInfo: MusicInfo,
    onItemClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onMenuClick: () -> Unit,
    showAddButton: Boolean,
    showMenuButton: Boolean,
    isPlaying: Boolean,
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 4.dp)
            .clickable {
                haptic.performClick()
                onItemClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp, bottom = 8.dp, start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AlbumCover(
                uri = musicInfo.music.albumArtUri,
                size = 56.dp,
                corner = 10.dp,
                shadow = 3.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = musicInfo.music.title,
                    style = MaterialTheme.typography.headlineSmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = musicInfo.music.artist,
                    style = MaterialTheme.typography.labelSmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = musicInfo.music.album,
                    style = MaterialTheme.typography.labelSmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Row {
                if (showAddButton) {
                    IconButton(
                        onClick = {
                            haptic.performConfirm()
                            scope.launch {
                                onAddToPlaylist()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Add Button",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                if (showMenuButton) {
                    IconButton(
                        onClick = {
                            haptic.performLightClick()
                            onMenuClick()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more),
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Menu Button",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
