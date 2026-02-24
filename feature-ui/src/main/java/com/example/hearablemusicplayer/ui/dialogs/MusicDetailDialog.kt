package com.example.hearablemusicplayer.ui.dialogs

import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.AlbumCover

// 音乐详情卡片弹窗 - 优化版UI布局
@OptIn(UnstableApi::class)
@Composable
fun MusicDetailDialog(
    musicInfo: MusicInfo?,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onDetail: () -> Unit,
    onRemove: () -> Unit
) {
    if (musicInfo == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(max = 130.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onPlay() }
                    ) {
                        AlbumCover(
                            uri = musicInfo.music.albumArtUri,
                            size = 120.dp,
                            corner = 20.dp,
                            shadow = 10.dp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 艺术家信息
                        InfoRow(
                            iconRes = R.drawable.person,
                            label = stringResource(R.string.artist),
                            value = musicInfo.music.artist
                        )
                        // 专辑信息
                        InfoRow(
                            iconRes = R.drawable.music_note_list,
                            label = stringResource(R.string.album),
                            value = musicInfo.music.album
                        )
                        // 时长信息（如果可用）
                        musicInfo.music.duration.let { duration ->
                            InfoRow(
                                iconRes = R.drawable.timer,
                                label = stringResource(R.string.duration),
                                value = stringResource(
                                    R.string.duration_format,
                                    duration / 1000 / 60,
                                    (duration / 1000) % 60
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                ) {
                    val menuOptions = listOf(
                        Triple(R.drawable.plus_square, R.string.add_to_playlist, onAddToPlaylist),
                        Triple(R.drawable.heart, R.string.favorite, onFavorite),
                        Triple(R.drawable.share, R.string.share, onShare),
                        Triple(R.drawable.music, R.string.title_song_detail, onDetail),
                        Triple(R.drawable.trash, R.string.remove, onRemove)
                    )

                    menuOptions.forEach { (icon, label, action) ->
                        MenuOption(iconRes = icon, labelRes = label, onClick = action)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun InfoRow(
    iconRes: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MenuOption(
    iconRes: Int,
    @StringRes labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(5.dp))
            .clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = stringResource(labelRes),
            modifier = Modifier
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleMedium
        )
    }
}