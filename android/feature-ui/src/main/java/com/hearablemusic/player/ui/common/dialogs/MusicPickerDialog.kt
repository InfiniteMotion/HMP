package com.hearablemusic.player.ui.common.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmp.domain.music.MusicInfo
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.dialogs.base.ScrimDialog
import com.hearablemusic.player.ui.common.util.HazeRenderSettings
import com.hearablemusic.player.ui.common.util.ProvideHazeRenderSettings
import com.hearablemusic.player.ui.common.util.hazeStyleForIntensity
import com.hearablemusic.player.ui.common.util.hazeTintAlpha
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun MusicPickerDialog(
    allMusic: List<MusicInfo>,
    selectedIds: Set<Long>,
    title: String,
    onConfirm: (Set<Long>) -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
    hazeRenderSettings: HazeRenderSettings = HazeRenderSettings()
) {
    var currentSelectedIds by remember(selectedIds) { mutableStateOf(selectedIds) }

    ProvideHazeRenderSettings(settings = hazeRenderSettings) {
        ScrimDialog(onDismissRequest = onDismiss) {
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
                        .padding(20.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "已选择 ${currentSelectedIds.size} 首",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (allMusic.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_songs_available),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(allMusic, key = { it.music.id }) { musicInfo ->
                                val songId = musicInfo.music.id
                                val checked = currentSelectedIds.contains(songId)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            currentSelectedIds = if (checked) {
                                                currentSelectedIds - songId
                                            } else {
                                                currentSelectedIds + songId
                                            }
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { isChecked ->
                                            currentSelectedIds = if (isChecked) {
                                                currentSelectedIds + songId
                                            } else {
                                                currentSelectedIds - songId
                                            }
                                        }
                                    )
                                    Column(
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .weight(1f)
                                    ) {
                                        Text(
                                            text = musicInfo.music.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = musicInfo.music.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
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
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { onConfirm(currentSelectedIds) }) {
                            Text(stringResource(R.string.confirm))
                        }
                    }
                }
            }
        }
}
}
