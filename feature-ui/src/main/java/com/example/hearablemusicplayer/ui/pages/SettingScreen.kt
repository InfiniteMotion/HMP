package com.example.hearablemusicplayer.ui.pages

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    
    // Backup State
    val localBackups by settingsViewModel.localBackups.collectAsState()

    SettingScreenContent(
        avatarUri = avatarUri,
        userName = userName,
        musicCount = musicCount,
        refreshMode = refreshMode,
        refreshHours = refreshHours,
        startupCount = startupCount,
        localBackups = localBackups,
        onBackClick = { navController.popBackStack() },
        onSaveAvatarUri = settingsViewModel::saveAvatarUri,
        onSaveUserName = settingsViewModel::saveUserName,
        onIncrementalScan = libraryViewModel::refreshMusicList,
        onFullRescan = libraryViewModel::fullRescan,
        onSaveDailyRefreshMode = settingsViewModel::saveDailyRefreshMode,
        onSaveDailyRefreshHours = settingsViewModel::saveDailyRefreshHours,
        onSaveDailyRefreshStartupCount = settingsViewModel::saveDailyRefreshStartupCount,
        onExportBackup = settingsViewModel::exportBackup,
        onRestoreBackup = settingsViewModel::restoreBackup,
        onDeleteBackup = settingsViewModel::deleteLocalBackup,
        onRefreshBackups = settingsViewModel::loadLocalBackups
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
    localBackups: List<File>,
    onBackClick: () -> Unit,
    onSaveAvatarUri: (String) -> Unit,
    onSaveUserName: (String) -> Unit,
    onIncrementalScan: () -> Unit,
    onFullRescan: () -> Unit,
    onSaveDailyRefreshMode: (String) -> Unit,
    onSaveDailyRefreshHours: (Int) -> Unit,
    onSaveDailyRefreshStartupCount: (Int) -> Unit,
    onExportBackup: ((File) -> Unit, (String) -> Unit) -> Unit,
    onRestoreBackup: (File, () -> Unit, (String) -> Unit) -> Unit,
    onDeleteBackup: (File) -> Unit,
    onRefreshBackups: () -> Unit
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
            UserDataBackupSection(
                localBackups = localBackups,
                onExportBackup = onExportBackup,
                onRestoreBackup = onRestoreBackup,
                onDeleteBackup = onDeleteBackup,
                onRefreshBackups = onRefreshBackups
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
                onIncrementalScan = onIncrementalScan,
                onFullRescan = onFullRescan
            )
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
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
    onIncrementalScan: () -> Unit,
    onFullRescan: () -> Unit,
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
                        onIncrementalScan()
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
                        onFullRescan()
                    },
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.reload), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun UserDataBackupSection(
    localBackups: List<File>,
    onExportBackup: ((File) -> Unit, (String) -> Unit) -> Unit,
    onRestoreBackup: (File, () -> Unit, (String) -> Unit) -> Unit,
    onDeleteBackup: (File) -> Unit,
    onRefreshBackups: () -> Unit
) {
    val context = LocalContext.current
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackupFile by remember { mutableStateOf<File?>(null) }
    
    // File Picker for External Restore
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = File(context.cacheDir, "restore_temp.json")
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                selectedBackupFile = tempFile
                showRestoreDialog = true
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    TitleWidget(title = stringResource(R.string.backup)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.backup_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = Center
            )

            // Local Backups List
            if (localBackups.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.local_backup),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = onRefreshBackups) {
                            Icon(
                                painter = painterResource(R.drawable.externaldrive),
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    localBackups.forEach { file ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = Ellipsis
                                    )
                                    Text(
                                        text = SimpleDateFormat(
                                            "yyyy/MM/dd HH:mm",
                                            Locale.getDefault()
                                        ).format(Date(file.lastModified())),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = {
                                    selectedBackupFile = file
                                    showRestoreDialog = true
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_gallery_material_select_checkbox),
                                        contentDescription = "Restore",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onDeleteBackup(file) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.trash),
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        restoreLauncher.launch(arrayOf("application/json"))
                    }
                ) {
                    Text(stringResource(R.string.restore_backup))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onExportBackup({ file ->
                            // Share Intent
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_backup)))
                        }, { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        })
                    }
                ) {
                    Text(stringResource(R.string.export_backup))
                }
            }
        }
    }
    
    if (showRestoreDialog && selectedBackupFile != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Confirm Restore") },
            text = { Text("Restoring will overwrite current data. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestoreBackup(selectedBackupFile!!, {
                            Toast.makeText(context, "Restore Successful", Toast.LENGTH_SHORT).show()
                        }, { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        })
                        showRestoreDialog = false
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * 每日推荐刷新策略设置
 */
@SuppressLint("LocalContextGetResourceValueCall")
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
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryEditable, // 核心参数
                        enabled = true // 可选参数，控制菜单是否可用
                    ),
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
