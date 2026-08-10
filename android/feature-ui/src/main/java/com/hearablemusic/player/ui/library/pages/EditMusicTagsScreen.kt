package com.hearablemusic.player.ui.library.pages

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.dialogs.controller.DialogManager
import com.hearablemusic.player.ui.common.pages.base.SubScreen
import com.hearablemusic.player.ui.library.pages.components.AlbumCover
import com.hearablemusic.player.ui.library.viewmodel.EditMusicTagsViewModel
import com.hearablemusic.player.ui.library.viewmodel.SaveTagsResult
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun EditMusicTagsScreen(
    navController: NavBackStack<NavKey>,
    musicId: Long,
    viewModel: EditMusicTagsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val dialogManager: DialogManager = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    var showPermissionDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(musicId) {
        viewModel.loadTags(musicId)
    }

    // 保存结果提示
    LaunchedEffect(saveResult) {
        when (val result = saveResult) {
            is SaveTagsResult.Success -> {
                dialogManager.showMessage(context.getString(R.string.tags_saved))
                viewModel.clearSaveResult()
                navController.removeLastOrNull()
            }
            is SaveTagsResult.Error -> {
                dialogManager.showMessage(
                    context.getString(R.string.tags_save_failed, result.message.orEmpty())
                )
                viewModel.clearSaveResult()
            }
            null -> {}
        }
    }

    val coverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onCoverSelected(it) }
    }

    SubScreen(
        onBackClick = { navController.removeLastOrNull() },
        title = stringResource(R.string.edit_music_tags),
        trailingContent = {
            TextButton(
                enabled = !uiState.isLoading && !isSaving,
                onClick = {
                    if (hasStorageManagerPermission()) {
                        viewModel.save()
                    } else {
                        showPermissionDialog = true
                    }
                }
            ) {
                Text(
                    if (isSaving) stringResource(R.string.loading)
                    else stringResource(R.string.save)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    CoverEditor(
                        albumArtUri = uiState.albumArtUri,
                        newCoverBytes = uiState.newAlbumArtBytes,
                        onChooseCover = { coverLauncher.launch("image/*") },
                        onRemoveCover = { viewModel.onRemoveCover() }
                    )
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text(stringResource(R.string.tag_title)) },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.artist,
                        onValueChange = viewModel::onArtistChange,
                        label = { Text(stringResource(R.string.tag_artist)) },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.album,
                        onValueChange = viewModel::onAlbumChange,
                        label = { Text(stringResource(R.string.tag_album)) },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.year,
                            onValueChange = viewModel::onYearChange,
                            label = { Text(stringResource(R.string.tag_year)) },
                            singleLine = true,
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = uiState.track,
                            onValueChange = viewModel::onTrackChange,
                            label = { Text(stringResource(R.string.tag_track)) },
                            singleLine = true,
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = uiState.genre,
                        onValueChange = viewModel::onGenreChange,
                        label = { Text(stringResource(R.string.tag_genre)) },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.lyrics,
                        onValueChange = viewModel::onLyricsChange,
                        label = { Text(stringResource(R.string.tag_lyrics)) },
                        minLines = 4,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.edit_tags_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.storage_permission_title)) },
            text = { Text(stringResource(R.string.storage_permission_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    openStorageManagerSettings(context)
                }) {
                    Text(stringResource(R.string.go_to_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun CoverEditor(
    albumArtUri: String?,
    newCoverBytes: ByteArray?,
    onChooseCover: () -> Unit,
    onRemoveCover: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.tag_cover),
            style = MaterialTheme.typography.titleMedium
        )
        if (newCoverBytes != null && newCoverBytes.isNotEmpty()) {
            val bitmap = remember(newCoverBytes) {
                BitmapFactory.decodeByteArray(newCoverBytes, 0, newCoverBytes.size)
                    .asImageBitmap()
            }
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(R.string.tag_cover),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            AlbumCover(
                uri = albumArtUri,
                size = 120.dp,
                corner = 12.dp,
                shadow = 4.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            OutlinedButton(onClick = onChooseCover) {
                Text(stringResource(R.string.choose_cover))
            }
            OutlinedButton(
                onClick = onRemoveCover,
                enabled = newCoverBytes != null || albumArtUri != null
            ) {
                Text(stringResource(R.string.remove_cover))
            }
        }
    }
}

private fun hasStorageManagerPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()
}

private fun openStorageManagerSettings(context: android.content.Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    context.startActivity(intent)
}
