package com.example.hearablemusicplayer.ui.pages.playlist

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.playlist.Playlist
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.components.PlayControlButtonTwo
import com.example.hearablemusicplayer.ui.pages.base.SubScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

@OptIn(UnstableApi::class)
@Composable
fun PlaylistScreen(
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel(),
    navController: NavController,
) {
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    val uiState by playlistViewModel.playlistUiState.collectAsState()
    val allMusicForAdd by playlistViewModel.allMusicForAddPicker.collectAsState()
    var showAddSongDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showAddSongDialog) {
        if (showAddSongDialog) playlistViewModel.loadAllMusicForAddPicker()
    }

    if (showAddSongDialog && uiState.selectedPlaylistId != null) {
        val context = LocalContext.current
        val addedMessage = stringResource(R.string.song_added)
        AddSongToPlaylistDialog(
            allMusic = allMusicForAdd,
            currentInPlaylistIds = uiState.playlist.map { it.music.id }.toSet(),
            onAdd = { musicId, path ->
                playlistViewModel.addItemToPlaylist(uiState.selectedPlaylistId!!, musicId, path)
                Toast.makeText(context, addedMessage, Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAddSongDialog = false }
        )
    }

    PlaylistScreenContent(
        isPlaying = isPlaying,
        playlistName = uiState.playlistName,
        playlist = uiState.playlist,
        playlistMeta = uiState.playlistMeta,
        selectedPlaylistId = uiState.selectedPlaylistId,
        isCustomPlaylist = uiState.isCustomPlaylist,
        onBackClick = { navController.popBackStack() },
        onRecordPlaylistPlay = { playlistViewModel.recordPlaylistPlay(it) },
        onShufflePlay = {
            uiState.selectedPlaylistId?.let { playlistViewModel.recordPlaylistPlay(it) }
            playControlViewModel.addAllToPlaylistByShuffle(uiState.playlist)
            navController.navigate(Routes.Player)
        },
        onOrderPlay = {
            uiState.selectedPlaylistId?.let { playlistViewModel.recordPlaylistPlay(it) }
            playControlViewModel.addAllToPlaylistInOrder(uiState.playlist)
            navController.navigate(Routes.Player)
        },
        onNavigate = navController::navigate,
        playWith = playControlViewModel::playWith,
        addToPlaylist = playControlViewModel::addToPlaylist,
        onRenamePlaylist = { id, newName -> playlistViewModel.renamePlaylist(id, newName) },
        onDeletePlaylist = { id ->
            try {
                playlistViewModel.deletePlaylist(id)
                navController.popBackStack()
            } catch (_: IllegalArgumentException) { }
        },
        onUpdateDescription = { id, desc -> playlistViewModel.updatePlaylistDescription(id, desc) },
        onSetPinned = { id, pinned -> playlistViewModel.setPlaylistPinned(id, pinned) },
        onUpdateCover = { id, uri -> playlistViewModel.updatePlaylistCover(id, uri) },
        onRemoveFromPlaylist = { musicId, pid -> playlistViewModel.removeItemFromPlaylist(musicId, pid) },
        onReorder = { orderedIds ->
            uiState.selectedPlaylistId?.let { pid ->
                playlistViewModel.reorderPlaylistItems(pid, orderedIds)
            }
        },
        onAddSongsClick = if (uiState.selectedPlaylistId != null) {
            { showAddSongDialog = true }
        } else null
    )
}

private const val HEADER_COVER_SIZE_DP = 160
private const val HEADER_CORNER_RADIUS_DP = 16

@OptIn(UnstableApi::class)
@Composable
fun PlaylistScreenContent(
    isPlaying: Boolean,
    playlistName: String,
    playlist: List<MusicInfo>,
    playlistMeta: Playlist?,
    selectedPlaylistId: Long?,
    isCustomPlaylist: Boolean,
    onBackClick: () -> Unit,
    onRecordPlaylistPlay: (Long) -> Unit,
    onShufflePlay: () -> Unit,
    onOrderPlay: () -> Unit,
    onNavigate: (Any) -> Unit,
    playWith: suspend (MusicInfo) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onUpdateDescription: (Long, String?) -> Unit,
    onSetPinned: (Long, Boolean) -> Unit,
    onUpdateCover: (Long, String?) -> Unit,
    onRemoveFromPlaylist: (Long, Long) -> Unit,
    onReorder: (List<Long>) -> Unit,
    onAddSongsClick: (() -> Unit)? = null
) {
    val haptic = rememberHapticFeedback()
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf(playlistName) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var descriptionValue by remember(playlistMeta?.description) {
        mutableStateOf(
            playlistMeta?.description ?: ""
        )
    }

    if (showRenameDialog && selectedPlaylistId != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
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
                            onRenamePlaylist(selectedPlaylistId, name)
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDeleteConfirm && selectedPlaylistId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_playlist)) },
            text = { Text(stringResource(R.string.delete_playlist_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePlaylist(selectedPlaylistId)
                        showDeleteConfirm = false
                    }
                ) {
                    Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDescriptionDialog && selectedPlaylistId != null) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text(stringResource(R.string.edit_playlist_description)) },
            text = {
                OutlinedTextField(
                    value = descriptionValue,
                    onValueChange = { descriptionValue = it },
                    label = { Text(stringResource(R.string.playlist_description_hint)) },
                    minLines = 2,
                    maxLines = 4
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateDescription(
                            selectedPlaylistId,
                            descriptionValue.trim().ifEmpty { null })
                        showDescriptionDialog = false
                    }
                ) {
                    Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDescriptionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    SubScreen(
        onBackClick = onBackClick,
        title = playlistName
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            if (playlistMeta != null) {
                PlaylistHeader(meta = playlistMeta)
            }
            if (isCustomPlaylist && selectedPlaylistId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (playlistMeta != null) {
                        TextButton(
                            onClick = { onSetPinned(selectedPlaylistId, !playlistMeta.isPinned) }
                        ) {
                            Text(
                                stringResource(
                                    if (playlistMeta.isPinned) R.string.unpin_playlist else R.string.pin_playlist
                                )
                            )
                        }
                        TextButton(
                            onClick = {
                                descriptionValue = playlistMeta.description ?: ""
                                showDescriptionDialog = true
                            }
                        ) {
                            Text(stringResource(R.string.edit_playlist_description))
                        }
                        TextButton(
                            onClick = {
                                playlist.firstOrNull()?.music?.albumArtUri?.let { uri ->
                                    if (uri.isNotBlank()) onUpdateCover(selectedPlaylistId, uri)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.set_cover_from_first_song))
                        }
                    }
                    TextButton(
                        onClick = { renameValue = playlistName; showRenameDialog = true }
                    ) {
                        Text(stringResource(R.string.rename_playlist))
                    }
                    TextButton(
                        onClick = { showDeleteConfirm = true }
                    ) {
                        Text(
                            stringResource(R.string.delete_playlist),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (onAddSongsClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onAddSongsClick) {
                        Text(stringResource(R.string.add_songs_to_playlist))
                    }
                }
            }
            PlayControlButtonTwo(
                onShufflePlay = onShufflePlay,
                onOrderPlay = onOrderPlay
            )
            MusicList(
                musicInfoList = playlist,
                onItemClick = { music ->
                    haptic.performClick()
                    onNavigate(Routes.SongDetail(music.music.id))
                },
                onAddToPlaylist = { _ -> },
                onMenuClick = { music -> onNavigate(Routes.SongDetail(music.music.id)) },
                showAddButton = false,
                showMenuButton = true,
                isPlaying = isPlaying,
                playlistId = selectedPlaylistId,
                onRemoveFromPlaylist = { music ->
                    selectedPlaylistId?.let { pid ->
                        onRemoveFromPlaylist(music.music.id, pid)
                    }
                },
                showReorderButtons = isCustomPlaylist && selectedPlaylistId != null,
                onMoveUp = { index ->
                    val ids = playlist.map { it.music.id }.toMutableList()
                    if (index > 0) {
                        ids[index] = ids[index - 1].also { ids[index - 1] = ids[index] }
                        onReorder(ids)
                    }
                },
                onMoveDown = { index ->
                    val ids = playlist.map { it.music.id }.toMutableList()
                    if (index < ids.size - 1) {
                        ids[index] = ids[index + 1].also { ids[index + 1] = ids[index] }
                        onReorder(ids)
                    }
                }
            )
        }
    }
}

@Composable
private fun PlaylistHeader(meta: Playlist) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(HEADER_COVER_SIZE_DP.dp)
                .clip(RoundedCornerShape(HEADER_CORNER_RADIUS_DP.dp))
        ) {
            val coverUri = meta.coverUri
            if (!coverUri.isNullOrBlank()) {
                AsyncImage(
                    model = coverUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(HEADER_COVER_SIZE_DP.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(HEADER_COVER_SIZE_DP.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.music_note_list),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        val description = meta.description
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
        val lastPlayedAt = meta.lastPlayedAt
        if (meta.songCount > 0 || meta.playCount > 0 || meta.totalDurationMs > 0 || lastPlayedAt != null) {
            val parts = mutableListOf<String>()
            if (meta.songCount > 0) {
                parts.add(stringResource(R.string.songs_count, meta.songCount))
            }
            if (meta.totalDurationMs > 0) {
                val minutes = (meta.totalDurationMs / 1000 / 60).toInt()
                parts.add(stringResource(R.string.minutes_format, minutes))
            }
            if (meta.playCount > 0) {
                parts.add(stringResource(R.string.play_count_display, meta.playCount))
            }
            if (parts.isNotEmpty()) {
                Text(
                    text = parts.joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (lastPlayedAt != null && lastPlayedAt > 0L) {
                Text(
                    text = stringResource(R.string.last_played),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AddSongToPlaylistDialog(
    allMusic: List<MusicInfo>,
    currentInPlaylistIds: Set<Long>,
    onAdd: (musicId: Long, path: String) -> Unit,
    onDismiss: () -> Unit
) {
    val toShow = allMusic.filter { it.music.id !in currentInPlaylistIds }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.add_songs_to_playlist),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(
                    items = toShow,
                    key = { it.music.id }
                ) { musicInfo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAdd(musicInfo.music.id, musicInfo.music.path)
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = musicInfo.music.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = musicInfo.music.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}