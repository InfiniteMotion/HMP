package com.example.hearablemusicplayer.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.viewmodel.MusicViewModel

@Composable
fun MusicScanDialog(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val isLoading by viewModel.isScanning.collectAsState(initial = false)
    var isFinished = false
    val musicCount by viewModel.musicCount.collectAsState(initial = 0)
    AlertDialog(
        onDismissRequest = { if (!isFinished) onDismiss() }, // 加载中时禁止关闭
        title = { Text("扫描音乐") },
        text = {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("正在扫描设备中的音乐，请耐心等待……")
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text("扫描完成！已加载本地音乐${musicCount}首。")
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            if (!isLoading) {
                TextButton(onClick = { isFinished=true; onDismiss() }) {
                    Text("确定")
                }
            }
        }
    )
}
