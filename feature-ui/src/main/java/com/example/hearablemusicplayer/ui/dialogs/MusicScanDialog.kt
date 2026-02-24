package com.example.hearablemusicplayer.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel

@Composable
fun MusicScanDialog(
    libraryViewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val isLoading by libraryViewModel.isScanning.collectAsState(initial = false)
    val musicCount by libraryViewModel.musicCount.collectAsState(initial = 0)
    
    AlertDialog(
        onDismissRequest = { /* 扫描期间禁止关闭 */ },
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(R.string.scan_music_title)) },
        text = {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.scanning_desc))
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.scan_complete_desc, musicCount))
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
        confirmButton = {
            if (!isLoading) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.ok))
                }
            }
        }
    )
}
