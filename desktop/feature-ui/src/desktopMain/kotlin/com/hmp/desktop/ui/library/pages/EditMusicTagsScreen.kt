package com.hmp.desktop.ui.library.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmp.desktop.generated.resources.*
import com.hmp.desktop.ui.common.dialogs.controller.DialogManager
import com.hmp.desktop.ui.common.navigation.NavController
import com.hmp.desktop.ui.common.pages.base.SubScreen
import com.hmp.desktop.ui.common.components.base.TitleWidget
import com.hmp.desktop.ui.common.util.DesktopFilePicker
import com.hmp.desktop.ui.library.pages.components.AlbumCover
import com.hmp.desktop.ui.library.viewmodel.EditMusicTagsViewModel
import com.hmp.desktop.ui.library.viewmodel.SaveTagsResult
import com.hmp.desktop.ui.player.components.MiniPlayerSafeSpacer
import org.jetbrains.compose.resources.painterResource
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
                enabled = !uiState.isLoading && !isSaving && uiState.hasChanges,
                onClick = { viewModel.save() }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(Res.string.save))
                }
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text(stringResource(Res.string.retry))
                    }
                }
                else -> {
                    if (uiState.hasChanges) {
                        UnsavedChangesHint()
                    }
                    SongHeaderCard(
                        albumArtUri = uiState.albumArtUri,
                        newCoverBytes = uiState.newAlbumArtBytes,
                        title = uiState.title,
                        artist = uiState.artist,
                        album = uiState.album,
                        onChooseCover = {
                            DesktopFilePicker.pickImageFile()?.let { viewModel.onCoverSelected(it) }
                        },
                        onRemoveCover = { viewModel.onRemoveCover() }
                    )

                    TitleWidget(title = stringResource(Res.string.section_basic_info)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        }
                    }

                    TitleWidget(title = stringResource(Res.string.section_detail_info)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        }
                    }

                    LyricsSection(
                        lyrics = uiState.lyrics,
                        onLyricsChange = viewModel::onLyricsChange,
                        enabled = !isSaving
                    )
                    Text(
                        text = stringResource(Res.string.edit_tags_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.save() },
                        enabled = !isSaving && uiState.hasChanges,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(Res.string.save))
                        }
                    }
                    MiniPlayerSafeSpacer(height = 56.dp)
                }
            }
        }
    }
}

@Composable
private fun SongHeaderCard(
    albumArtUri: String?,
    newCoverBytes: ByteArray?,
    title: String,
    artist: String,
    album: String,
    onChooseCover: () -> Unit,
    onRemoveCover: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                                .size(96.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                } else {
                    AlbumCover(
                        uri = albumArtUri,
                        size = 96.dp,
                        corner = 12.dp,
                        shadow = 4.dp
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = album,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
}

@Composable
private fun LyricsSection(
    lyrics: String,
    onLyricsChange: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    TitleWidget(title = stringResource(Res.string.tag_lyrics)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (lyrics.isNotBlank()) lyrics else stringResource(Res.string.no_lyrics),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (lyrics.isNotBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(
                        if (expanded) Res.drawable.chevron_up else Res.drawable.chevron_down
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                OutlinedTextField(
                    value = lyrics,
                    onValueChange = onLyricsChange,
                    label = { Text(stringResource(Res.string.tag_lyrics)) },
                    minLines = 6,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun UnsavedChangesHint() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = stringResource(Res.string.unsaved_changes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
