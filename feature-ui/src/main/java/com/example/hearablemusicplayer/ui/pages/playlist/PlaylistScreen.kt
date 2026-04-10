package com.example.hearablemusicplayer.ui.pages.playlist

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.playlist.Playlist
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.dialogs.ScrimDialog
import com.example.hearablemusicplayer.ui.components.AlbumCover
import com.example.hearablemusicplayer.ui.components.musiclist.EditConfig
import com.example.hearablemusicplayer.ui.components.musiclist.FixedMusicList
import com.example.hearablemusicplayer.ui.components.musiclist.FullItemOptions
import com.example.hearablemusicplayer.ui.components.musiclist.HeaderConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemVariant
import com.example.hearablemusicplayer.ui.components.musiclist.MusicList
import com.example.hearablemusicplayer.ui.components.musiclist.MusicListCallbacksAdapter
import com.example.hearablemusicplayer.ui.components.musiclist.defaultMusicListConfig
import com.example.hearablemusicplayer.ui.components.musiclist.playlistPresetMusicListConfig
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
    navController: NavBackStack<NavKey>,
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
        onBackClick = { navController.removeLastOrNull() },
        onRecordPlaylistPlay = { playlistViewModel.recordPlaylistPlay(it) },
        onShufflePlay = {
            uiState.selectedPlaylistId?.let { playlistViewModel.recordPlaylistPlay(it) }
            playControlViewModel.addAllToPlaylistByShuffle(uiState.playlist)
            navController.add(Routes.Player)
        },
        onOrderPlay = {
            uiState.selectedPlaylistId?.let { playlistViewModel.recordPlaylistPlay(it) }
            playControlViewModel.addAllToPlaylistInOrder(uiState.playlist)
            navController.add(Routes.Player)
        },
        onNavigate = navController::add,
        playWith = playControlViewModel::playWith,
        addToPlaylist = playControlViewModel::addToPlaylist,
        onRenamePlaylist = { id, newName -> playlistViewModel.renamePlaylist(id, newName) },
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

private const val HEADER_COVER_SIZE_DP = 280
private const val HEADER_CORNER_RADIUS_DP = 25

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
    onNavigate: (NavKey) -> Unit,
    playWith: suspend (MusicInfo) -> Unit,
    addToPlaylist: (MusicInfo) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
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
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var descriptionValue by remember(playlistMeta?.description) {
        mutableStateOf(
            playlistMeta?.description ?: ""
        )
    }
    var isListEditMode by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
        ) {
            if (playlistMeta != null) {
                PlaylistHeader(
                    meta = playlistMeta
                )
            }
            if (isCustomPlaylist && selectedPlaylistId != null) {
                AnimatedVisibility(
                    visible = isListEditMode,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            if (playlistMeta != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
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
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TextButton(
                                    onClick = { renameValue = playlistName; showRenameDialog = true }
                                ) {
                                    Text(stringResource(R.string.rename_playlist))
                                }
                            }
                        }
                    }
                }
            }
            val coroutineScope = rememberCoroutineScope()
            val callbacks = object : MusicListCallbacksAdapter() {
                override fun onEnterEditMode() {
                    isListEditMode = true
                }
                override fun onExitEditMode() {
                    isListEditMode = false
                }
                override fun onItemClick(musicInfo: MusicInfo, index: Int) {
                    haptic.performClick()
                    coroutineScope.launch { playWith(musicInfo) }
                }
                override fun onMenuClick(musicInfo: MusicInfo) {
                    onNavigate(Routes.SongDetail(musicInfo.music.id))
                }
                override fun onRemoveFromPlaylist(musicInfo: MusicInfo) {
                    selectedPlaylistId?.let { pid -> onRemoveFromPlaylist(musicInfo.music.id, pid) }
                }
                override fun onMoveUp(index: Int) {
                    if (index <= 0 || index >= playlist.size) return
                    val ids = playlist.map { it.music.id }.toMutableList()
                    ids[index] = ids[index - 1].also { ids[index - 1] = ids[index] }
                    onReorder(ids)
                }
                override fun onMoveDown(index: Int) {
                    if (index < 0 || index >= playlist.size - 1) return
                    val ids = playlist.map { it.music.id }.toMutableList()
                    ids[index] = ids[index + 1].also { ids[index + 1] = ids[index] }
                    onReorder(ids)
                }
                override fun onPinToTop(musicInfo: MusicInfo) {
                    val ids = listOf(musicInfo.music.id) + playlist.map { it.music.id }.filter { it != musicInfo.music.id }
                    onReorder(ids)
                }
            }
            val addSongsClick = onAddSongsClick
            val baseConfig = playlistPresetMusicListConfig(
                onOrderPlay = onOrderPlay,
                onShufflePlay = onShufflePlay,
                callbacks = callbacks,
                headerTrailing = if (addSongsClick != null) {
                    {
                        IconButton(
                            onClick = {
                                haptic.performClick()
                                addSongsClick()
                            },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.plus),
                                contentDescription = stringResource(R.string.add_songs_to_playlist),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                } else null,
            )
            val config = baseConfig.copy(
                item = baseConfig.item.copy(
                    fullOptions = baseConfig.item.fullOptions?.copy(showRemoveButton = false)
                        ?: FullItemOptions(showPinButton = true, showRemoveButton = false, showMenuButton = true),
                ),
            )
            FixedMusicList(
                musicInfoList = playlist,
                config = config,
                modifier = Modifier.fillMaxWidth(),
                isPlaying = isPlaying,
            )
        }
    }
}

@Composable
private fun PlaylistStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlaylistHeader(
    meta: Playlist
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cover Image
            Box(
                modifier = Modifier
                    .size(HEADER_COVER_SIZE_DP.dp)
                    .clip(RoundedCornerShape(HEADER_CORNER_RADIUS_DP.dp))
            ) {
                AlbumCover(
                    uri = meta.coverUri,
                    size = HEADER_COVER_SIZE_DP.dp,
                    corner = 25.dp,
                    shadow = 15.dp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            val lastPlayedAt = meta.lastPlayedAt
            if (meta.songCount > 0 || meta.playCount > 0 || meta.totalDurationMs > 0 || lastPlayedAt != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (meta.songCount > 0) {
                        PlaylistStatItem(
                            label = stringResource(R.string.counts),
                            value = meta.songCount.toString()
                        )
                    }
                    if (meta.totalDurationMs > 0) {
                        val minutes = (meta.totalDurationMs / 1000 / 60).toInt()
                        PlaylistStatItem(
                            label = stringResource(R.string.duration),
                            value = stringResource(R.string.minutes_format, minutes)
                        )
                    }
                    if (meta.playCount > 0) {
                        PlaylistStatItem(
                            label = stringResource(R.string.play),
                            value = meta.playCount.toString()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
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
                        .padding(horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddSongToPlaylistDialog(
    allMusic: List<MusicInfo>,
    currentInPlaylistIds: Set<Long>,
    onAdd: (musicId: Long, path: String) -> Unit,
    onDismiss: () -> Unit
) {
    val toShow = allMusic.filter { it.music.id !in currentInPlaylistIds }
    ScrimDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_songs_to_playlist),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (toShow.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No songs available to add",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val callbacks = object : MusicListCallbacksAdapter() {
                        override fun onItemClick(musicInfo: MusicInfo, index: Int) {
                            onAdd(musicInfo.music.id, musicInfo.music.path)
                        }
                    }
                    val config = defaultMusicListConfig(callbacks).copy(
                        header = HeaderConfig.None,
                        item = ItemConfig(
                            variant = ItemVariant.Full,
                            fullOptions = FullItemOptions(
                                showPinButton = false,
                                showRemoveButton = false,
                                showMenuButton = false,
                            ),
                        ),
                        edit = EditConfig(enabled = false),
                    )
                    MusicList(
                        musicInfoList = toShow,
                        config = config,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        isPlaying = false,
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}