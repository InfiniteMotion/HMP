package com.hmp.desktop.ui.library.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.hmp.desktop.generated.resources.*
import com.hmp.desktop.ui.common.dialogs.controller.DialogManager
import com.hmp.desktop.ui.common.navigation.NavController
import com.hmp.desktop.ui.common.pages.base.SubScreen
import com.hmp.desktop.ui.common.util.DesktopFilePicker
import com.hmp.desktop.ui.library.pages.components.AlbumCover
import com.hmp.desktop.ui.library.viewmodel.EditMusicTagsViewModel
import com.hmp.desktop.ui.library.viewmodel.SaveTagsResult
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@Composable
fun EditMusicTagsScreen(
    navController: NavController,
    musicId: Long,
    viewModel: EditMusicTagsViewModel = koinInject()
) {
    val dialogManager: DialogManager = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()

    val savedMessage = stringResource(Res.string.tags_saved)
    val failedMessage = stringResource(Res.string.tags_save_failed, "")
    val noChangesMessage = stringResource(Res.string.no_changes)

    LaunchedEffect(musicId) {
        viewModel.loadTags(musicId)
    }

    // 保存结果提示
    LaunchedEffect(saveResult) {
        when (val result = saveResult) {
            is SaveTagsResult.Success -> {
                dialogManager.showMessage(savedMessage)
                viewModel.clearSaveResult()
                navController.popBackStack()
            }
            is SaveTagsResult.Error -> {
                val message = when (result.message) {
                    "No changes to save" -> noChangesMessage
                    null -> failedMessage
                    else -> failedMessage + result.message
                }
                dialogManager.showMessage(message)
                viewModel.clearSaveResult()
            }
            null -> {}
        }
    }

    SubScreen(
        onBackClick = { navController.popBackStack() },
        title = stringResource(Res.string.edit_music_tags),
        trailingContent = {
            TextButton(
                enabled = !uiState.isLoading && !isSaving,
                onClick = { viewModel.save() }
            ) {
                Text(
                    if (isSaving) stringResource(Res.string.loading)
                    else stringResource(Res.string.save)
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
                        onChooseCover = {
                            DesktopFilePicker.pickImageFile()?.let { viewModel.onCoverSelected(it) }
                        },
                        onRemoveCover = { viewModel.onRemoveCover() }
                    )
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text(stringResource(Res.string.tag_title)) },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.artist,
                        onValueChange = viewModel::onArtistChange,
                        label = { Text(stringResource(Res.string.tag_artist)) },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.album,
                        onValueChange = viewModel::onAlbumChange,
                        label = { Text(stringResource(Res.string.tag_album)) },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.year,
                            onValueChange = viewModel::onYearChange,
                            label = { Text(stringResource(Res.string.tag_year)) },
                            singleLine = true,
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = uiState.track,
                            onValueChange = viewModel::onTrackChange,
                            label = { Text(stringResource(Res.string.tag_track)) },
                            singleLine = true,
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = uiState.genre,
                        onValueChange = viewModel::onGenreChange,
                        label = { Text(stringResource(Res.string.tag_genre)) },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.lyrics,
                        onValueChange = viewModel::onLyricsChange,
                        label = { Text(stringResource(Res.string.tag_lyrics)) },
                        minLines = 4,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(Res.string.edit_tags_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
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
            text = stringResource(Res.string.tag_cover),
            style = MaterialTheme.typography.titleMedium
        )
        if (newCoverBytes != null && newCoverBytes.isNotEmpty()) {
            val bitmap = remember(newCoverBytes) {
                ImageIO.read(ByteArrayInputStream(newCoverBytes))?.toComposeImageBitmap()
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(Res.string.tag_cover),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
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
                Text(stringResource(Res.string.choose_cover))
            }
            OutlinedButton(
                onClick = onRemoveCover,
                enabled = newCoverBytes != null || albumArtUri != null
            ) {
                Text(stringResource(Res.string.remove_cover))
            }
        }
    }
}
