package com.example.hearablemusicplayer.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MusicScanDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() }, // 加载中时禁止关闭
        title = { Text("扫描音乐") },
        text = {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Text("正在扫描设备中的音乐…")
                }
            } else {
                Text("扫描完成！已加载本地音乐。")
            }
        },
        confirmButton = {
            if (!isLoading) {
                TextButton(onClick = onDismiss) {
                    Text("确定")
                }
            }else{
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}
