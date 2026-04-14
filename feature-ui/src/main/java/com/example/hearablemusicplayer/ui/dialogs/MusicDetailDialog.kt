package com.example.hearablemusicplayer.ui.dialogs

import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.AlbumCover
import com.example.hearablemusicplayer.ui.util.HazeRenderSettings
import com.example.hearablemusicplayer.ui.util.LocalHazeRenderSettings
import com.example.hearablemusicplayer.ui.util.ProvideHazeRenderSettings
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.hazeStyleForIntensity
import com.example.hearablemusicplayer.ui.util.hazeTintAlpha
import com.example.hearablemusicplayer.ui.viewmodel.DialogViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

// 音乐详情卡片弹窗 - 优化版UI布局
@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun MusicDetailDialog(
    dialogViewModel: DialogViewModel,
    onDismiss: () -> Unit,
    navController: NavBackStack<NavKey>,
    hazeState: HazeState? = null,
    hazeRenderSettings: HazeRenderSettings? = null
) {
    val musicDetailState by dialogViewModel.musicDetailState.collectAsState()
    val musicInfo = musicDetailState?.musicInfo
    val resolvedHazeRenderSettings = hazeRenderSettings ?: LocalHazeRenderSettings.current
    
    // 设置导航控制器
    dialogViewModel.setNavController(navController)
    
    if (musicInfo == null || !musicDetailState!!.isVisible) return

    ProvideHazeRenderSettings(settings = resolvedHazeRenderSettings) {
        ScrimDialog(onDismissRequest = onDismiss) {
            val dialogShape = RoundedCornerShape(28.dp)
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .clip(dialogShape)
                    .then(
                        if (hazeState != null) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = hazeStyleForIntensity()
                            )
                        } else Modifier
                    ),
                shape = dialogShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (hazeState != null) {
                        MaterialTheme.colorScheme.surface.copy(alpha = hazeTintAlpha())
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = musicInfo.music.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        // 收藏状态图标
                        IconButton(
                            onClick = { dialogViewModel.toggleFavorite() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (musicInfo.userInfo?.liked == true) R.drawable.heart_fill else R.drawable.heart
                                ),
                                contentDescription = stringResource(
                                    if (musicInfo.userInfo?.liked == true) R.string.favorite else R.string.add_to_favorites
                                ),
                                tint = if (musicInfo.userInfo?.liked == true) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
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
                                .clickable { 
                                    dialogViewModel.playMusic { 
                                        dialogViewModel.dismissMusicDetailDialog()
                                        onDismiss()
                                    }
                                }
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
                                value = musicInfo.music.artist,
                                onClick = {
                                    navController.add(Routes.Artist(musicInfo.music.artist))
                                    dialogViewModel.dismissMusicDetailDialog()
                                    onDismiss()
                                }
                            )
                            // 专辑信息
                            InfoRow(
                                iconRes = R.drawable.music_note_list,
                                label = stringResource(R.string.album),
                                value = musicInfo.music.album,
                                onClick = {
                                    navController.add(Routes.Album(musicInfo.music.album))
                                    dialogViewModel.dismissMusicDetailDialog()
                                    onDismiss()
                                }
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
                        val menuOptions = dialogViewModel.getMenuOptions {
                            dialogViewModel.dismissMusicDetailDialog()
                            onDismiss()
                        }

                        menuOptions.forEach { (icon, label, action) ->
                            MenuOption(iconRes = icon, labelRes = label, onClick = action)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    iconRes: Int,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
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
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
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
                .size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}