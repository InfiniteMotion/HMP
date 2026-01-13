package com.example.hearablemusicplayer.ui.pages

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.dialogs.MusicScanDialog
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel

@Composable
fun IntroScreen(
    settingsViewModel: SettingsViewModel,
    libraryViewModel: LibraryViewModel,
    onFinished: ()-> Unit
) {
    val currentStep = remember { mutableIntStateOf(0) }
    val isPermissionGiven = remember { mutableStateOf(false) }
    val showScanDialog = remember { mutableStateOf(false) }
    val isScanCompleted = remember { mutableStateOf(false) }
    
    // 权限授予后自动进入下一步
    LaunchedEffect(isPermissionGiven.value) {
        if (isPermissionGiven.value && currentStep.intValue == 0) {
            currentStep.intValue = 1
        }
    }

    // 修改为多权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 所有权限都被授予时才设置为 true
        isPermissionGiven.value = permissions.all { it.value }
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "跟着向导快速完成配置",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
                
        // 步骤指示器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (index == currentStep.intValue)
                        MaterialTheme.colorScheme.primary
                    else if (index < currentStep.intValue)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                if (index < 2) {
                    Text(
                        text = "→",
                        color = if (index < currentStep.intValue)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
                
        // 步骤内容容器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = currentStep.intValue,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    0 -> PermissionStep(
                        isPermissionGiven = isPermissionGiven.value,
                        onRequestPermission = {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.READ_MEDIA_AUDIO,
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                )
                            )
                        }
                    )
                    1 -> ScanMusicStep(
                        isScanCompleted = isScanCompleted.value,
                        onStartScan = {
                            libraryViewModel?.refreshMusicList()
                            showScanDialog.value = true
                        },
                        onScanComplete = {
                            showScanDialog.value = false
                            isScanCompleted.value = true
                            currentStep.intValue = 2
                        },
                        showScanDialog = showScanDialog.value,
                        libraryViewModel = libraryViewModel
                    )
                    2 -> StartExperienceStep(
                        onFinished = onFinished
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionStep(
    isPermissionGiven: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "1. 授予权限",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "向您请求授予音频媒体文件访问权限和通知权限,我们需要这些权限来实现音乐扫描和通知栏控制。",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(max = 300.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!isPermissionGiven) {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.width(150.dp)
            ) {
                Text("授予")
            }
        } else {
            Button(
                onClick = { },
                colors = ButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                modifier = Modifier.width(150.dp)
            ) {
                Text("已授权!")
            }
        }
    }
}

@Composable
fun ScanMusicStep(
    isScanCompleted: Boolean,
    showScanDialog: Boolean,
    onStartScan: () -> Unit,
    onScanComplete: () -> Unit,
    libraryViewModel: LibraryViewModel?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "2. 扫描音乐",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "首次启动需要从设备中扫描音乐,后续您可以在设置中自行手动更新和扫描。",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(max = 300.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isScanCompleted) {
            Button(
                onClick = { },
                colors = ButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                modifier = Modifier.width(150.dp)
            ) {
                Text("扫描完毕!")
            }
        } else {
            Button(
                onClick = onStartScan,
                modifier = Modifier.width(150.dp)
            ) {
                Text("扫描")
            }
        }
        
        if (showScanDialog && libraryViewModel != null) {
            MusicScanDialog(
                libraryViewModel = libraryViewModel,
                onDismiss = onScanComplete
            )
        }
    }
}

@Composable
fun StartExperienceStep(
    onFinished: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "3. 开始体验",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "设置完毕后,点击按钮即可开启您的音乐体验。",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(max = 300.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onFinished,
            modifier = Modifier.width(150.dp)
        ) {
            Text("开始体验")
        }
    }
}
