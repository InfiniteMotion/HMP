package com.example.hearablemusicplayer.ui.pages

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.dialogs.MusicScanDialog
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun IntroScreen(
    viewModel: MusicViewModel,
    onFinished: ()-> Unit
) {
    val isPermissionGiven = remember { mutableStateOf(false) }
    val isLoadMusic = remember { mutableStateOf(false) }
    val isStartLoadMusic = remember { mutableStateOf(false) }

    // 修改为多权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 所有权限都被授予时才设置为 true
        isPermissionGiven.value = permissions.all { it.value }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally),

            )
        Text(
            text = "欢迎来到",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Text(
                text = "Hearable",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " Music Player",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "跟着向导快速完成配置",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "1. 授予权限",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "    向您请求授予音频媒体文件访问权限和通知权限，我们需要这些权限来实现音乐扫描和通知栏控制。",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(max = 300.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        // 修改授权按钮的点击事件
        if(!isPermissionGiven.value){
            Button(
                onClick = { 
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.READ_MEDIA_AUDIO,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                    )
                },
                modifier = Modifier.width(150.dp)
            ) {
                Text("授予")
            }
        }else {
            Button(
                onClick = { },
                colors = ButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                modifier = Modifier.width(150.dp)
            )
            {
                Text("已授权！")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "2. 扫描音乐",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "    首次启动需要从设备中扫描音乐，后续您可以在设置中自行手动更新和扫描。",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(max = 300.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        if(!isLoadMusic.value){
            Button(
                onClick = {
                    viewModel.refreshMusicList()
                    isStartLoadMusic.value = true
                    isLoadMusic.value = true
                          },
                modifier = Modifier.width(150.dp))
            {
                Text("扫描")
            }
        }else {
            Button(
                onClick = { },
                colors = ButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                modifier = Modifier.width(150.dp)
            )
            {
                Text("扫描完毕！")
            }
        }
        if(isStartLoadMusic.value){
            MusicScanDialog(
                viewModel = viewModel,
                onDismiss = { isStartLoadMusic.value = false }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "3. 开始体验",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "设置完毕后，点击按钮即可开启您的音乐体验。",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(max = 300.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onFinished,modifier = Modifier.width(150.dp)) {
            Text("开始体验")
        }
    }
}
