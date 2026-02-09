package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback

/**
 * 悬浮迷你播放器组件
 * 展示当前播放音乐信息和快捷控制按钮
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MiniPlayerBar(
    modifier: Modifier = Modifier,
    musicInfo: MusicInfo?,
    isPlaying: Boolean,
    progress: Float = 0f, // 0f-1f的进度值
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    val haptic = rememberHapticFeedback()
    val musicTitle = musicInfo?.music?.title ?: "Music Title"
    val artistName = musicInfo?.music?.artist ?: "Artist Name"
    val albumArtUri = musicInfo?.music?.albumArtUri ?: ""
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                haptic.performLightClick()
                onOpenPlayer()
            },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(Transparent)
    ) {
        Column {
            Box{
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        )
                )
                if (progress > 0f){
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .fillMaxWidth(progress)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(0.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp,vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 专辑封面
                AlbumCover(
                    uri = albumArtUri,
                    size = 48.dp,
                    corner = 8.dp,
                    shadow = 5.dp,
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 音乐信息
                Column (modifier = Modifier.weight(1f)) {
                    Text(
                        text = musicTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 控制按钮组
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 上一首按钮
                    IconButton(
                        onClick = {
                            haptic.performLightClick()
                            onPrev()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.backward_end_fill),
                            contentDescription = "Previous",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // 播放/暂停按钮
                    IconButton(
                        onClick = {
                            haptic.performLightClick()
                            onPlayPause()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = if (isPlaying) {
                                painterResource(R.drawable.pause)
                            } else {
                                painterResource(R.drawable.play_fill)
                            },
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // 下一首按钮
                    IconButton(
                        onClick = {
                            haptic.performLightClick()
                            onNext()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.forward_end_fill),
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}