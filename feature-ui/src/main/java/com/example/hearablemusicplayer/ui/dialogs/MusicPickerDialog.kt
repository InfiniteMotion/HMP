
package com.example.hearablemusicplayer.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.musiclist.EditConfig
import com.example.hearablemusicplayer.ui.components.musiclist.FullItemOptions
import com.example.hearablemusicplayer.ui.components.musiclist.HeaderConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemConfig
import com.example.hearablemusicplayer.ui.components.musiclist.ItemVariant
import com.example.hearablemusicplayer.ui.components.musiclist.MusicList
import com.example.hearablemusicplayer.ui.components.musiclist.MusicListCallbacksAdapter
import com.example.hearablemusicplayer.ui.components.musiclist.defaultMusicListConfig

@Composable
fun MusicPickerDialog(
    allMusic: List<MusicInfo>,
    selectedIds: Set<Long>,
    title: String,
    onConfirm: (Set<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelectedIds by remember { mutableStateOf(selectedIds) }

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
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
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
                    val callbacks = object : MusicListCallbacksAdapter() {
                        override fun onItemClick(musicInfo: MusicInfo, index: Int) {
                            currentSelectedIds = if (currentSelectedIds.contains(musicInfo.music.id)) {
                                currentSelectedIds - musicInfo.music.id
                            } else {
                                currentSelectedIds + musicInfo.music.id
                            }
                        }
                        override fun onSelectionChange(ids: Set<Long>) {
                            currentSelectedIds = ids
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
                        edit = EditConfig(enabled = true),
                    )
                    MusicList(
                        musicInfoList = allMusic,
                        config = config,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        isPlaying = false
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
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onConfirm(currentSelectedIds) }) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
}
