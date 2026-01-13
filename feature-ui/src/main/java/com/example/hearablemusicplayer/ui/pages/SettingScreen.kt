package com.example.hearablemusicplayer.ui.pages

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.ui.components.Avatar
import com.example.hearablemusicplayer.ui.template.components.TitleWidget
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun SettingScreen(
    settingsViewModel: SettingsViewModel,
    libraryViewModel: LibraryViewModel,
    navController: NavController
) {
    // 使用SubScreen模板
    SubScreen(
        navController = navController,
        title = "设置"
    ) {
        val avatarUri by settingsViewModel.avatarUri.collectAsState("")
        val userName by settingsViewModel.userName.collectAsState("")
        val musicCount by libraryViewModel.musicCount.collectAsState(initial = 0)
        UpdateAvatar(
            avatarUri = avatarUri,
            updateAvatar = settingsViewModel::saveAvatarUri
        )
        UpdateUserName(
            userName = userName,
            updateUserName = settingsViewModel::saveUserName
        )
        DailyRefreshSettings(
            settingsViewModel = settingsViewModel
        )
        ReloadMusic(
            musicCount = musicCount,
            refreshMusicList = libraryViewModel::refreshMusicList
        )
    }
}

@Composable
fun UpdateAvatar(
    avatarUri: String,
    updateAvatar: (String) -> Unit
){
    TitleWidget(
        title = "头像",
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            val uriImg = remember { mutableStateOf("") }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
                onResult = { uri: Uri? ->
                    uri?.let {
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val file = File(context.filesDir, "user_avatar.jpg")

                            val outputStream = FileOutputStream(file)
                            inputStream?.copyTo(outputStream)

                            // 保存头像路径到 SharedPreferences
                            uriImg.value = file.absolutePath
                            inputStream?.close()
                            outputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Avatar(128, avatarUri)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (uriImg.value == "") {
                    Button(
                        modifier = Modifier.width(300.dp),
                        onClick = {
                            launcher.launch("image/*")  // 打开图片选择器
                        }
                    ) {
                        Text(text = "更改头像", color = MaterialTheme.colorScheme.onPrimary)
                    }
                } else {
                    AsyncImage(
                        model = uriImg.value,
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    Button(
                        onClick = {
                            updateAvatar(uriImg.value)
                            Toast.makeText(context, "头像已更改!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "更改", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(
                        onClick = {
                            uriImg.value = ""
                            Toast.makeText(context, "放弃更改头像！", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "取消", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateUserName(
    userName: String?,
    updateUserName: (String) -> Unit,
){
    TitleWidget(
        title = "用户名",
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var name by rememberSaveable { mutableStateOf("") }
            Text(
                text = userName?:"用户名",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
            ){
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = { Text("请输入新的用户名") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Transparent, // 聚焦时下划线颜色
                        unfocusedIndicatorColor = Transparent, // 未聚焦时下划线颜色
                        disabledIndicatorColor = Transparent // 禁用时下划线颜色
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.width(300.dp)
                        .padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.width(200.dp),
                onClick = {
                    updateUserName(name)
                }
            ) {
                Text(text = "更改用户名", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun ReloadMusic(
    musicCount: Int,
    refreshMusicList: () -> Unit,
) {
    TitleWidget(
        title = "音乐扫描",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var isLoading by remember { mutableStateOf(false) }
            Text(
                text = "当前音乐数量：${musicCount} 首",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "可选择方式从设备中读取音乐信息",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "增量加载只会增加音乐信息，重载会删除所有相关信息（建议仅在应用首次启动时使用）",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 32.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        isLoading = true
                        refreshMusicList()
                    },
                    enabled = !isLoading
                ) {
                    Text("增量加载", color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(32.dp))
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        isLoading = true
                        refreshMusicList()
                    },
                    enabled = !isLoading
                ) {
                    Text("重新加载", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

/**
 * 每日推荐刷新策略设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRefreshSettings(
    settingsViewModel: SettingsViewModel
) {
    val refreshMode by settingsViewModel.dailyRefreshMode.collectAsState()
    val refreshHours by settingsViewModel.dailyRefreshHours.collectAsState()
    val startupCount by settingsViewModel.dailyRefreshStartupCount.collectAsState()
    val context = LocalContext.current
    
    TitleWidget(
        title = "每日推荐刷新策略",
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "选择每日推荐的刷新方式",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // 刷新模式选择
            var expanded by remember { mutableStateOf(false) }
            val refreshModes = listOf(
                "time" to "按时间刷新",
                "startup" to "按启动次数刷新",
                "smart" to "智能刷新（预留）"
            )
            val currentModeLabel = refreshModes.find { it.first == refreshMode }?.second ?: "按时间刷新"
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = currentModeLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("刷新模式") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Transparent,
                        unfocusedIndicatorColor = Transparent,
                        disabledIndicatorColor = Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    refreshModes.forEach { (mode, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                settingsViewModel.saveDailyRefreshMode(mode)
                                expanded = false
                                Toast.makeText(context, "已切换到: $label", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
            
            // 根据选择的模式显示不同的配置项
            when (refreshMode) {
                "time" -> {
                    var hoursText by remember(refreshHours) { mutableStateOf(refreshHours.toString()) }
                    
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { 
                            hoursText = it
                            it.toIntOrNull()?.let { hours ->
                                if (hours > 0) {
                                    settingsViewModel.saveDailyRefreshHours(hours)
                                }
                            }
                        },
                        label = { Text("刷新间隔（小时）") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Text(
                        text = "当前设置：每 $refreshHours 小时刷新一次",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                "startup" -> {
                    var countText by remember(startupCount) { mutableStateOf(startupCount.toString()) }
                    
                    OutlinedTextField(
                        value = countText,
                        onValueChange = { 
                            countText = it
                            it.toIntOrNull()?.let { count ->
                                if (count > 0) {
                                    settingsViewModel.saveDailyRefreshStartupCount(count)
                                }
                            }
                        },
                        label = { Text("启动次数") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Text(
                        text = "当前设置：每启动 $startupCount 次后刷新",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                "smart" -> {
                    Text(
                        text = "智能刷新模式将根据您的听歌习惯、时间段等因素自动判断刷新时机。目前默认使用每 24 小时刷新一次的策略，后续将根据 AI 分析进行智能优化。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
