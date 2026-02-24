package com.example.hearablemusicplayer.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.rememberAsyncImagePainter
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import kotlinx.coroutines.launch

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
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                haptic.performClick()
                onItemClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Transparent),
    ) {
        Row(
            modifier = Modifier.Companion
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

// Gallery 项组件 - 用于 Gallery 页面，只显示更多按钮
@OptIn(UnstableApi::class)
@Composable
fun GalleryItem(
    musicInfo: MusicInfo,
    onItemClick: () -> Unit,
    onMenuClick: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
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
            // 只显示更多按钮
            IconButton(
                onClick = {
                    haptic.performLightClick()
                    onMenuClick()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.more),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(R.string.more),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// 播放列表项
@Composable
fun PlaylistItem(
    musicInfo: MusicInfo,
    isCurrentPlaying: Boolean,
    index: Int,
    onItemClick: () -> Unit,
    onPinClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // 圆角裁剪
            .clickable(onClick = onItemClick)
            .background(
                if (isCurrentPlaying)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    Transparent,
                shape = RoundedCornerShape(12.dp) // 圆角背景
            )
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 序号
        Text(
            text = "$index",
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrentPlaying)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(32.dp)
        )

        // 专辑封面（使用简化版本，不使用 AlbumCover 以避免布局问题）
        Image(
            painter = rememberAsyncImagePainter(
                model = musicInfo.music.albumArtUri,
                placeholder = painterResource(R.drawable.none),
                error = painterResource(R.drawable.none)
            ),
            contentDescription = stringResource(R.string.album_art),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 标题和艺术家
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrentPlaying)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = musicInfo.music.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 操作按钮组
        Row {
            // 置顶按钮
            IconButton(
                onClick = onPinClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.chevron_up_circle),
                    contentDescription = stringResource(R.string.pin_to_top),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // 移除按钮
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.trash),
                    contentDescription = stringResource(R.string.remove),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}