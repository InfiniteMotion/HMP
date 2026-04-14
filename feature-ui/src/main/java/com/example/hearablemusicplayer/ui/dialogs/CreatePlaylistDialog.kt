package com.example.hearablemusicplayer.ui.dialogs

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
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.util.HazeRenderSettings
import com.example.hearablemusicplayer.ui.util.LocalHazeRenderSettings
import com.example.hearablemusicplayer.ui.util.ProvideHazeRenderSettings
import com.example.hearablemusicplayer.ui.util.hazeStyleForIntensity
import com.example.hearablemusicplayer.ui.util.hazeTintAlpha
import com.example.hearablemusicplayer.ui.viewmodel.DialogViewModel
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.new_playlist_dialog_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
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
                        }
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = dialogViewModel::updateCreatePlaylistDescription,
                        label = { Text(stringResource(R.string.playlist_description_hint)) },
                        singleLine = false,
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.pin_playlist))
                        Switch(
                            checked = uiState.pinAfterCreate,
                            onCheckedChange = dialogViewModel::setCreatePlaylistPinned
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            enabled = !uiState.isSubmitting,
                            onClick = dialogViewModel::onCreatePlaylistAddSongsClick
                        ) {
                            Text(stringResource(R.string.add_songs_to_playlist))
                        }
                        Text(
                            text = "已选择 ${uiState.selectedSongIds.size} 首",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    uiState.submitError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            enabled = !uiState.isSubmitting,
                            onClick = dialogViewModel::dismissCreatePlaylistDialog
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(
                            enabled = uiState.canSubmit,
                            onClick = dialogViewModel::submitCreatePlaylist
                        ) {
                            Text(
                                text = stringResource(R.string.new_playlist),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
