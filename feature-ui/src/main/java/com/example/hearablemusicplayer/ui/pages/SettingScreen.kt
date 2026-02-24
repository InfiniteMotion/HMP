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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.Avatar
import com.example.hearablemusicplayer.ui.components.TitleWidget
import com.example.hearablemusicplayer.ui.pages.base.SubScreen
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
    val avatarUri by settingsViewModel.avatarUri.collectAsState("")
    val userName by settingsViewModel.userName.collectAsState("")
    val musicCount by libraryViewModel.musicCount.collectAsState(initial = 0)
    
    // Daily Refresh Settings State
    val refreshMode by settingsViewModel.dailyRefreshMode.collectAsState()
    val refreshHours by settingsViewModel.dailyRefreshHours.collectAsState()
    val startupCount by settingsViewModel.dailyRefreshStartupCount.collectAsState()

    SettingScreenContent(
        avatarUri = avatarUri,
        userName = userName,
        musicCount = musicCount,
        refreshMode = refreshMode,
        refreshHours = refreshHours,
        startupCount = startupCount,
        onBackClick = { navController.popBackStack() },
        onSaveAvatarUri = settingsViewModel::saveAvatarUri,
        onSaveUserName = settingsViewModel::saveUserName,
        onRefreshMusicList = libraryViewModel::refreshMusicList,
        onSaveDailyRefreshMode = settingsViewModel::saveDailyRefreshMode,
        onSaveDailyRefreshHours = settingsViewModel::saveDailyRefreshHours,
        onSaveDailyRefreshStartupCount = settingsViewModel::saveDailyRefreshStartupCount
    )
}

@Composable
fun SettingScreenContent(
    avatarUri: String,
    userName: String,
    musicCount: Int,
    refreshMode: String,
    refreshHours: Int,
    startupCount: Int,
    onBackClick: () -> Unit,
    onSaveAvatarUri: (String) -> Unit,
    onSaveUserName: (String) -> Unit,
    onRefreshMusicList: () -> Unit,
    onSaveDailyRefreshMode: (String) -> Unit,
    onSaveDailyRefreshHours: (Int) -> Unit,
    onSaveDailyRefreshStartupCount: (Int) -> Unit
) {
    // 使用SubScreen模板
    SubScreen(
        onBackClick = onBackClick,
        title = stringResource(R.string.title_settings)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            UpdateAvatar(
                avatarUri = avatarUri,
                updateAvatar = onSaveAvatarUri
            )
            UpdateUserName(
                userName = userName,
                updateUserName = onSaveUserName
            )
            DailyRefreshSettings(
                refreshMode = refreshMode,
                refreshHours = refreshHours,
                startupCount = startupCount,
                onSaveRefreshMode = onSaveDailyRefreshMode,
                onSaveRefreshHours = onSaveDailyRefreshHours,
                onSaveStartupCount = onSaveDailyRefreshStartupCount
            )
            ReloadMusic(
                musicCount = musicCount,
                refreshMusicList = onRefreshMusicList
            )
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun UpdateAvatar(
    avatarUri: String,
    updateAvatar: (String) -> Unit
){
    TitleWidget(
        title = stringResource(R.string.avatar),
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
                        Text(text = stringResource(R.string.change_avatar), color = MaterialTheme.colorScheme.onPrimary)
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
                            Toast.makeText(context, context.getString(R.string.avatar_changed), Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = stringResource(R.string.change), color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(
                        onClick = {
                            uriImg.value = ""
                            Toast.makeText(context, context.getString(R.string.avatar_change_cancelled), Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onPrimary)
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
        title = stringResource(R.string.user_name),
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
                text = userName?:stringResource(R.string.user_name),
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
                    label = { Text(stringResource(R.string.enter_new_user_name)) },
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
                Text(text = stringResource(R.string.change_user_name), color = MaterialTheme.colorScheme.onPrimary)
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
        title = stringResource(R.string.music_scan),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var isLoading by remember { mutableStateOf(false) }
            Text(
                text = stringResource(R.string.current_music_count, musicCount),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.scan_method_desc),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.scan_method_warning),
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
                    Text(stringResource(R.string.incremental_load), color = MaterialTheme.colorScheme.onPrimary)
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
                    Text(stringResource(R.string.reload), color = MaterialTheme.colorScheme.onPrimary)
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
    refreshMode: String,
    refreshHours: Int,
    startupCount: Int,
    onSaveRefreshMode: (String) -> Unit,
    onSaveRefreshHours: (Int) -> Unit,
    onSaveStartupCount: (Int) -> Unit
) {
    val context = LocalContext.current
    
    TitleWidget(
        title = stringResource(R.string.daily_recommendation_strategy),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.select_refresh_strategy),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // 刷新模式选择
            var expanded by remember { mutableStateOf(false) }
            val refreshModes = listOf(
                "time" to stringResource(R.string.refresh_by_time),
                "startup" to stringResource(R.string.refresh_by_startup),
                "smart" to stringResource(R.string.refresh_smart)
            )
            val currentModeLabel = refreshModes.find { it.first == refreshMode }?.second ?: stringResource(R.string.refresh_by_time)
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = currentModeLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.refresh_mode_label)) },
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
                                onSaveRefreshMode(mode)
                                expanded = false
                                Toast.makeText(context, context.getString(R.string.switched_to, label), Toast.LENGTH_SHORT).show()
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
                                    onSaveRefreshHours(hours)
                                }
                            }
                        },
                        label = { Text(stringResource(R.string.refresh_interval_hours)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Text(
                        text = stringResource(R.string.current_setting_time, refreshHours),
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
                                    onSaveStartupCount(count)
                                }
                            }
                        },
                        label = { Text(stringResource(R.string.startup_count_label)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Text(
                        text = stringResource(R.string.current_setting_startup, startupCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                "smart" -> {
                    Text(
                        text = stringResource(R.string.smart_refresh_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
