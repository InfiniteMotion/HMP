package com.example.hearablemusicplayer.ui.pages.playlist

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.playlist.Playlist
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.pages.base.SubScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.DialogViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun PlaylistManageScreen(
    playlistViewModel: PlaylistViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    dialogViewModel: DialogViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    navController: NavBackStack<NavKey>
) {
    val userCustomPlaylists by playlistViewModel.userCustomPlaylists.collectAsState()
    val haptic = rememberHapticFeedback()
    var isEditMode by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    var renameValue by remember { mutableStateOf("") }

    val sortedList = remember(userCustomPlaylists) {
        userCustomPlaylists.sortedWith(
            compareByDescending<Playlist> { it.isPinned }
                .thenByDescending { it.lastPlayedAt ?: 0L }
                .thenByDescending { it.updatedAt }
        )
    }

    if (playlistToDelete != null) {
        val p = playlistToDelete!!
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text(stringResource(R.string.delete_playlist)) },
            text = { Text(stringResource(R.string.delete_playlist_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        playlistViewModel.deletePlaylist(p.id)
                        playlistToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete_playlist), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (playlistToRename != null) {
        val p = playlistToRename!!
        AlertDialog(
            onDismissRequest = { playlistToRename = null; renameValue = "" },
            title = { Text(stringResource(R.string.rename_playlist)) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text(stringResource(R.string.playlist_name_hint)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = renameValue.trim()
                        if (name.isNotEmpty()) {
                            playlistViewModel.renamePlaylist(p.id, name)
                            playlistToRename = null
                            renameValue = ""
                        }
                    }
                ) {
                    Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToRename = null; renameValue = "" }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    SubScreen(
        onBackClick = { navController.removeLastOrNull() },
        title = stringResource(R.string.title_manage_user_playlists),
        trailingContent = {
            FilledIconButton(
                onClick = {
                    haptic.performClick()
                    isEditMode = !isEditMode
                },
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    painter = painterResource(if (isEditMode) R.drawable.ic_gallery_material_select_checkbox else R.drawable.gearshape),
                    contentDescription = stringResource(R.string.edit_list),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(sortedList, key = { it.id }) { playlist ->
                    UserPlaylistRow(
                        playlist = playlist,
                        isEditMode = isEditMode,
                        onClick = {
                            haptic.performClick()
                            navController.add(Routes.CustomPlaylist(playlist.id))
                        },
                        onDelete = { playlistToDelete = playlist },
                        onRename = {
                            playlistToRename = playlist
                            renameValue = playlist.name
                        },
                        onSetPinned = { pinned ->
                            haptic.performClick()
                            playlistViewModel.setPlaylistPinned(playlist.id, pinned)
                        }
                    )
                }
            }

            FilledIconButton(
                onClick = {
                    haptic.performClick()
                    dialogViewModel.showCreatePlaylistDialog { id ->
                        playlistViewModel.loadUserCustomPlaylists()
                        navController.add(Routes.CustomPlaylist(id))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 96.dp, end = 32.dp)
                    .size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = stringResource(R.string.edit_list),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun UserPlaylistRow(
    playlist: Playlist,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onSetPinned: (Boolean) -> Unit
) {
    val haptic = rememberHapticFeedback()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
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
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.music_note_list),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (playlist.songCount > 0 || playlist.totalDurationMs > 0 || playlist.playCount > 0) {
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        if (!isEditMode) {
            Icon(
                painter = painterResource(R.drawable.chevron_right),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        haptic.performClick()
                        onSetPinned(!playlist.isPinned)
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            if (playlist.isPinned) R.drawable.star_fill else R.drawable.star
                        ),
                        contentDescription = stringResource(
                            if (playlist.isPinned) R.string.unpin_playlist else R.string.pin_playlist
                        ),
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = { haptic.performClick(); onRename() }) {
                    Text(stringResource(R.string.rename_playlist), style = MaterialTheme.typography.labelMedium)
                }
                IconButton(
                    onClick = { haptic.performClick(); onDelete() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.trash),
                        contentDescription = stringResource(R.string.delete_playlist),
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
