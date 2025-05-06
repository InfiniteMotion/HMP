package com.example.hearablemusicplayer.ui.dialogs

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun PermissionRequest(): Boolean {
    val context = LocalContext.current
    // 权限状态
    val hasPermission = remember { mutableStateOf(false) }

    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission.value = isGranted
    }

    // 首次检查权限
    LaunchedEffect(Unit) {
        val status = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_MEDIA_AUDIO
        )
        hasPermission.value = status == PackageManager.PERMISSION_GRANTED
    }

    // 按需请求权限
    if (!hasPermission.value) {
        AlertDialog(
            onDismissRequest = { /* 用户取消 */ },
            title = { Text("需要存储权限") },
            text = { Text("扫描音乐需要访问设备存储") },
            confirmButton = {
                Button(onClick = {
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
                }) {
                    Text("授权")
                }
            }
        )
    }

    return hasPermission.value
}

