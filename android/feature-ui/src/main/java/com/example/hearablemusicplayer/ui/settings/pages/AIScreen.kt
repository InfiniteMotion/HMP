package com.example.hearablemusicplayer.ui.settings.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hmp.domain.setting.model.AiProviderConfig
import com.hmp.domain.enum.AiProviderType
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.common.components.base.TitleWidget
import com.example.hearablemusicplayer.ui.common.dialogs.controller.DialogManager
import com.example.hearablemusicplayer.ui.common.pages.base.SubScreen
import com.example.hearablemusicplayer.ui.library.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.SettingsViewModel

@Composable
fun AIScreen(
    settingsViewModel: SettingsViewModel,
    recommendationViewModel: RecommendationViewModel,
    libraryViewModel: LibraryViewModel,
    dialogManager: DialogManager,
    navController: NavBackStack<NavKey>
) {
    // 加载当前服务商配置
    LaunchedEffect(Unit) {
        settingsViewModel.loadCurrentProviderConfig()
    }

    val musicWithExtraCount by libraryViewModel.musicWithExtraCount.collectAsState(initial = 0)
    val pendingCount by recommendationViewModel.pendingMusicCount.collectAsState(initial = 0)
    val currentProvider by settingsViewModel.currentAiProvider.collectAsState()
    val currentConfig by settingsViewModel.currentProviderConfig.collectAsState()
    val isTestingApi by settingsViewModel.isTestingApi.collectAsState()
    val apiTestResult by settingsViewModel.apiTestResult.collectAsState()
    val progress by recommendationViewModel.processingProgress.collectAsState()
    val autoBatchProcess by settingsViewModel.autoBatchProcess.collectAsState()
    
    // Daily Refresh Settings State
    val refreshMode by settingsViewModel.dailyRefreshMode.collectAsState()
    val refreshHours by settingsViewModel.dailyRefreshHours.collectAsState()
    val startupCount by settingsViewModel.dailyRefreshStartupCount.collectAsState()

    AIScreenContent(
        musicWithExtraCount = musicWithExtraCount,
        pendingCount = pendingCount,
        currentProvider = currentProvider,
        currentConfig = currentConfig,
        isTestingApi = isTestingApi,
        apiTestResult = apiTestResult,
        progress = progress,
        autoBatchProcess = autoBatchProcess,
        refreshMode = refreshMode,
        refreshHours = refreshHours,
        startupCount = startupCount,
        onProviderChange = settingsViewModel::switchAiProvider,
        onTestConnection = settingsViewModel::testAiProviderConnection,
        onSaveConfig = settingsViewModel::saveAiProviderConfig,
        onClearTestResult = settingsViewModel::clearApiTestResult,
        onAutoBatchProcessChange = settingsViewModel::saveAutoBatchProcess,
        startAutoProcessExtraInfo = recommendationViewModel::startAutoProcessWithCurrentProvider,
        pauseProcess = recommendationViewModel::pauseProcessing,
        resumeProcess = recommendationViewModel::resumeProcessing,
        cancelProcess = recommendationViewModel::cancelProcessing,
        onSaveDailyRefreshMode = settingsViewModel::saveDailyRefreshMode,
        onSaveDailyRefreshHours = settingsViewModel::saveDailyRefreshHours,
        onSaveDailyRefreshStartupCount = settingsViewModel::saveDailyRefreshStartupCount,
        dialogManager = dialogManager,
        onBackClick = { navController.removeLastOrNull() }
    )
}

@Composable
fun AIScreenContent(
    musicWithExtraCount: Int,
    pendingCount: Int,
    currentProvider: AiProviderType,
    currentConfig: AiProviderConfig?,
    isTestingApi: Boolean,
    apiTestResult: SettingsViewModel.ApiTestResult?,
    progress: RecommendationViewModel.BatchProcessingProgress,
    autoBatchProcess: Boolean,
    refreshMode: String,
    refreshHours: Int,
    startupCount: Int,
    onProviderChange: (AiProviderType) -> Unit,
    onTestConnection: (AiProviderType, String, String) -> Unit,
    onSaveConfig: (AiProviderType, String, String) -> Unit,
    onClearTestResult: () -> Unit,
    onAutoBatchProcessChange: (Boolean) -> Unit,
    startAutoProcessExtraInfo: () -> Unit,
    pauseProcess: () -> Unit,
    resumeProcess: () -> Unit,
    cancelProcess: () -> Unit,
    onSaveDailyRefreshMode: (String) -> Unit,
    onSaveDailyRefreshHours: (Int) -> Unit,
    onSaveDailyRefreshStartupCount: (Int) -> Unit,
    dialogManager: DialogManager,
    onBackClick: () -> Unit
) {
    SubScreen(
        onBackClick = onBackClick,
        title = stringResource(R.string.title_ai)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 服务商配置组件
            AiProviderConfig(
                currentProvider = currentProvider,
                currentConfig = currentConfig,
                isTestingApi = isTestingApi,
                apiTestResult = apiTestResult,
                onProviderChange = onProviderChange,
                onTestConnection = onTestConnection,
                onSaveConfig = onSaveConfig,
                onClearTestResult = onClearTestResult,
                dialogManager = dialogManager
            )

            LoadMusicExtraInfo(
                pendingCount = pendingCount,
                musicWithExtraCount = musicWithExtraCount,
                progress = progress,
                isConfigured = currentConfig?.isConfigured == true,
                autoBatchProcess = autoBatchProcess,
                onAutoBatchProcessChange = onAutoBatchProcessChange,
                startAutoProcessExtraInfo = startAutoProcessExtraInfo,
                pauseProcess = pauseProcess,
                resumeProcess = resumeProcess,
                cancelProcess = cancelProcess,
                dialogManager = dialogManager
            )

            // 每日推荐刷新策略
            DailyRefreshSettings(
                refreshMode = refreshMode,
                refreshHours = refreshHours,
                startupCount = startupCount,
                onSaveRefreshMode = onSaveDailyRefreshMode,
                onSaveRefreshHours = onSaveDailyRefreshHours,
                onSaveStartupCount = onSaveDailyRefreshStartupCount,
                dialogManager = dialogManager
            )

            Spacer(modifier = Modifier.height(64.dp))
        }
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
    onSaveStartupCount: (Int) -> Unit,
    dialogManager: DialogManager
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
                                dialogManager.showMessage(context.getString(R.string.switched_to, label))
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
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun AiProviderConfig(
    currentProvider: AiProviderType,
    currentConfig: AiProviderConfig?,
    isTestingApi: Boolean,
    apiTestResult: SettingsViewModel.ApiTestResult?,
    onProviderChange: (AiProviderType) -> Unit,
    onTestConnection: (AiProviderType, String, String) -> Unit,
    onSaveConfig: (AiProviderType, String, String) -> Unit,
    onClearTestResult: () -> Unit,
    dialogManager: DialogManager
) {
    val context = LocalContext.current
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    var apiKeyValue by rememberSaveable { mutableStateOf("") }
    var modelValue by rememberSaveable { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    // 当服务商改变时更新选中状态
    LaunchedEffect(currentProvider) {
        selectedProvider = currentProvider
    }
    
    // 当配置加载后更新输入框
    LaunchedEffect(currentConfig) {
        currentConfig?.let {
            if (it.type == selectedProvider) {
                // 不回显 API Key，但显示模型名称
                modelValue = it.model
            }
        }
    }
    
    // 显示测试结果 Toast
    LaunchedEffect(apiTestResult) {
        apiTestResult?.let { result ->
            val message = when (result) {
                is SettingsViewModel.ApiTestResult.Success -> result.message
                is SettingsViewModel.ApiTestResult.Error -> result.message
            }
            dialogManager.showMessage(message)
            onClearTestResult()
        }
    }
    
    TitleWidget(title = stringResource(R.string.ai_provider_config)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // 服务商选择下拉框
            Text(
                text = stringResource(R.string.current_ai_provider),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { expanded = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedProvider.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        painter = painterResource(R.drawable.chevron_down),
                        contentDescription = stringResource(R.string.select_provider),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    AiProviderType.entries.forEach { provider ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(provider.displayName)
                                    if (provider == selectedProvider) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            painter = painterResource(R.drawable.ic_gallery_material_select_checkbox),
                                            contentDescription = stringResource(R.string.selected),
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedProvider = provider
                                onProviderChange(provider)
                                expanded = false
                                // 重置输入框
                                apiKeyValue = ""
                                modelValue = provider.defaultModel
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 配置状态显示
            val isConfigured = currentConfig?.isConfigured == true && currentConfig.type == selectedProvider
            Text(
                text = if (isConfigured) stringResource(R.string.configured) else stringResource(R.string.not_configured),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // API Key 输入框
            TextField(
                value = apiKeyValue,
                onValueChange = { apiKeyValue = it },
                label = { 
                    Text(
                        stringResource(R.string.api_key), 
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                placeholder = {
                    Text(
                        stringResource(R.string.enter_api_key_placeholder, selectedProvider.displayName),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Transparent,
                    unfocusedIndicatorColor = Transparent,
                    disabledIndicatorColor = Transparent
                ),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 模型名称输入框
            TextField(
                value = modelValue,
                onValueChange = { modelValue = it },
                label = { 
                    Text(
                        stringResource(R.string.model_name), 
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                placeholder = {
                    Text(
                        stringResource(R.string.default_model_placeholder, selectedProvider.defaultModel),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Transparent,
                    unfocusedIndicatorColor = Transparent,
                    disabledIndicatorColor = Transparent
                ),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // 测试按钮
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        if (apiKeyValue.isNotBlank()) {
                            onTestConnection(selectedProvider, apiKeyValue, modelValue)
                        } else {
                            dialogManager.showMessage(context.getString(R.string.please_enter_api_key))
                        }
                    },
                    enabled = apiKeyValue.isNotBlank() && !isTestingApi
                ) {
                    if (isTestingApi) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.test), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                // 保存按钮
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        if (apiKeyValue.isNotBlank()) {
                            onSaveConfig(selectedProvider, apiKeyValue, modelValue)
                            dialogManager.showMessage(context.getString(R.string.config_saved))
                        } else {
                            dialogManager.showMessage(context.getString(R.string.please_enter_api_key))
                        }
                    },
                    enabled = apiKeyValue.isNotBlank()
                ) {
                    Text(stringResource(R.string.save), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 提示信息
            Text(
                text = stringResource(R.string.provider_change_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun LoadMusicExtraInfo(
    pendingCount: Int,
    musicWithExtraCount: Int,
    progress: RecommendationViewModel.BatchProcessingProgress,
    isConfigured: Boolean = true,
    autoBatchProcess: Boolean = false,
    onAutoBatchProcessChange: (Boolean) -> Unit = {},
    startAutoProcessExtraInfo: () -> Unit,
    pauseProcess: () -> Unit,
    resumeProcess: () -> Unit,
    cancelProcess: () -> Unit,
    dialogManager: DialogManager
) {
    val context = LocalContext.current
    
    TitleWidget(
        title = stringResource(R.string.music_info_completion),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 自动后台处理开关
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.auto_background_completion),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.auto_background_completion_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = autoBatchProcess,
                    onCheckedChange = onAutoBatchProcessChange
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // 待处理数量提示
            if (!progress.isProcessing) {
                Text(
                    text = stringResource(R.string.pending_music_count, pendingCount),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.completed_music_count, musicWithExtraCount),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            // 进度显示
            if (progress.isProcessing) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.processing_music, progress.currentMusicTitle),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { progress.progressPercent },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "${progress.processedCount} / ${progress.totalCount}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    
                    if (progress.isPaused) {
                        Text(
                            text = stringResource(R.string.paused),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 控制按钮
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (progress.isPaused) {
                            Button(
                                onClick = resumeProcess,
                                modifier = Modifier.width(100.dp)
                            ) {
                                Text(stringResource(R.string.resume), color = MaterialTheme.colorScheme.onPrimary)
                            }
                        } else {
                            Button(
                                onClick = pauseProcess,
                                modifier = Modifier.width(100.dp)
                            ) {
                                Text(stringResource(R.string.pause), color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        
                        Button(
                            onClick = cancelProcess,
                            modifier = Modifier.width(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!isConfigured) {
                    Text(
                        text = stringResource(R.string.please_config_provider),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                Button(
                    modifier = Modifier.width(300.dp),
                    onClick = {
                        if (!isConfigured) {
                            dialogManager.showMessage(context.getString(R.string.please_config_provider))
                            return@Button
                        }
                        if (pendingCount <= 0) {
                            dialogManager.showMessage(context.getString(R.string.no_pending_music))
                            return@Button
                        }
                        startAutoProcessExtraInfo()
                    },
                    enabled = true // 始终启用，在 onClick 中处理错误情况
                ) {
                    Text(text = stringResource(R.string.start_batch_completion), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
