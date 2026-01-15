package com.example.hearablemusicplayer.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import kotlinx.coroutines.coroutineScope
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
    transparentBackgroundWhenPlaying: Boolean
) {
    val haptic = rememberHapticFeedback()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = musicInfoList,
            key = { musicInfo -> musicInfo.music.id }
        ) { musicInfo ->
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
                transparentBackgroundWhenPlaying = transparentBackgroundWhenPlaying,
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
    transparentBackgroundWhenPlaying: Boolean,
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                haptic.performClick()
                onItemClick()
            }
            .background(
                if (isPlaying && transparentBackgroundWhenPlaying)
                    androidx.compose.ui.graphics.Color.Transparent
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = musicInfo.music.albumArtUri,
            contentDescription = "Album art",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${musicInfo.music.artist} • ${musicInfo.music.album}",
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        painter = painterResource(R.drawable.plus_square),
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
                        painter = painterResource(R.drawable.dot_grid_1x2),
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Menu Button",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
