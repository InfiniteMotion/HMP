package com.example.hearablemusicplayer.ui.common.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.common.dialogs.base.ScrimDialog
import com.example.hearablemusicplayer.ui.common.util.HazeRenderSettings
import com.example.hearablemusicplayer.ui.common.util.LocalHazeRenderSettings
import com.example.hearablemusicplayer.ui.common.util.ProvideHazeRenderSettings
import com.example.hearablemusicplayer.ui.common.util.hazeStyleForIntensity
import com.example.hearablemusicplayer.ui.common.util.hazeTintAlpha
import com.example.hearablemusicplayer.ui.common.dialogs.viewmodel.DialogViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun CreatePlaylistDialog(
    dialogViewModel: DialogViewModel,
    hazeState: HazeState? = null,
    hazeRenderSettings: HazeRenderSettings? = null
) {
    val state by dialogViewModel.createPlaylistState.collectAsState()
    val uiState = state ?: return
    val resolvedHazeRenderSettings = hazeRenderSettings ?: LocalHazeRenderSettings.current

    ProvideHazeRenderSettings(settings = resolvedHazeRenderSettings) {
        ScrimDialog(
            onDismissRequest = {
                if (!uiState.isSubmitting) {
                    dialogViewModel.dismissCreatePlaylistDialog()
                }
            }
        ) {
            val dialogShape = RoundedCornerShape(24.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(dialogShape)
                    .then(
                        if (hazeState != null) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = hazeStyleForIntensity()
                            )
                        } else {
                            Modifier
                        }
                    ),
                shape = dialogShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (hazeState != null) {
                        MaterialTheme.colorScheme.surface.copy(alpha = hazeTintAlpha())
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (uiState.isEditing) stringResource(R.string.edit_playlist) else stringResource(
                            R.string.new_playlist_dialog_title
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = dialogViewModel::updateCreatePlaylistName,
                        label = { Text(stringResource(R.string.playlist_name_hint)) },
                        singleLine = true,
                        isError = uiState.nameError != null,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            uiState.nameError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        )
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = dialogViewModel::updateCreatePlaylistDescription,
                        label = { Text(stringResource(R.string.playlist_description_hint)) },
                        singleLine = false,
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.pin_playlist),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = uiState.pinAfterCreate,
                            onCheckedChange = dialogViewModel::setCreatePlaylistPinned,
                            enabled = !uiState.isSubmitting
                        )
                    }
                    if (!uiState.isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "已选择 ${uiState.selectedSongIds.size} 首",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                enabled = !uiState.isSubmitting,
                                onClick = dialogViewModel::onCreatePlaylistAddSongsClick,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.add_songs_to_playlist),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    uiState.submitError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            enabled = !uiState.isSubmitting,
                            onClick = dialogViewModel::dismissCreatePlaylistDialog,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        TextButton(
                            enabled = uiState.canSubmit && !uiState.isSubmitting,
                            onClick = dialogViewModel::submitCreatePlaylist
                        ) {
                            Text(
                                text = if (uiState.isEditing) stringResource(R.string.ok) else stringResource(
                                    R.string.new_playlist
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
