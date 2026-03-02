package com.example.hearablemusicplayer.ui.pages

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.playlist.Playlist
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.NewPlaylistButton
import com.example.hearablemusicplayer.ui.components.UserListCard
import com.example.hearablemusicplayer.ui.pages.base.SubScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun UserPlaylistManageScreen(
    playlistViewModel: PlaylistViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    navController: NavController
) {
    val userCustomPlaylists by playlistViewModel.userCustomPlaylists.collectAsState()
    val haptic = rememberHapticFeedback()
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val sortedList = remember(userCustomPlaylists) {
        userCustomPlaylists.sortedWith(
            compareByDescending<Playlist> { it.isPinned }
                .thenByDescending { it.lastPlayedAt ?: 0L }
                .thenByDescending { it.updatedAt }
        )
    }

    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlaylistDialog = false; newPlaylistName = "" },
            title = { Text(stringResource(R.string.new_playlist_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text(stringResource(R.string.playlist_name_hint)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newPlaylistName.trim()
                        if (name.isNotEmpty()) {
                            playlistViewModel.createPlaylistAsync(name) { id ->
                                showNewPlaylistDialog = false
                                newPlaylistName = ""
                                navController.navigate(Routes.CustomPlaylist(id))
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlaylistDialog = false; newPlaylistName = "" }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    SubScreen(
        onBackClick = { navController.popBackStack() },
        title = stringResource(R.string.title_manage_user_playlists)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                NewPlaylistButton(onClick = { showNewPlaylistDialog = true })
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sortedList) { playlist ->
                    UserListCard(
                        playlist = playlist,
                        onClick = {
                            haptic.performClick()
                            navController.navigate(Routes.CustomPlaylist(playlist.id))
                        }
                    )
                }
            }
        }
    }
}
