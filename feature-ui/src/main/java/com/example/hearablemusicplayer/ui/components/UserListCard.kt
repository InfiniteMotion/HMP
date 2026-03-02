package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.playlist.Playlist
import com.example.hearablemusicplayer.ui.R

private const val CARD_WIDTH_DP = 110
private const val CARD_HEIGHT_DP = 180
private const val CORNER_RADIUS_DP = 24

/**
 * 用户自定义歌单卡片：竖版（高>宽）超大圆角矩形，展示封面与列表信息。
 */
@Composable
fun UserListCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(CARD_WIDTH_DP.dp)
            .height(CARD_HEIGHT_DP.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CORNER_RADIUS_DP.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 封面区域（上半部分）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = CORNER_RADIUS_DP.dp, topEnd = CORNER_RADIUS_DP.dp))
            ) {
                if (playlist.coverUri != null && playlist.coverUri!!.isNotBlank()) {
                    AsyncImage(
                        model = playlist.coverUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.music_note_list),
                            contentDescription = null,
                            modifier = Modifier
                                .width(40.dp)
                                .height(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // 列表信息（下半部分）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (playlist.songCount > 0 || playlist.playCount > 0 || playlist.totalDurationMs > 0) {
                    val parts = mutableListOf<String>()
                    if (playlist.songCount > 0) {
                        parts.add(stringResource(R.string.songs_count, playlist.songCount))
                    }
                    if (playlist.totalDurationMs > 0) {
                        val minutes = (playlist.totalDurationMs / 1000 / 60).toInt()
                        parts.add(stringResource(R.string.minutes_format, minutes))
                    }
                    if (playlist.playCount > 0 && parts.size < 2) {
                        parts.add(stringResource(R.string.play_count_display, playlist.playCount))
                    }
                    if (parts.isNotEmpty()) {
                        Text(
                            text = parts.take(2).joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
