package com.hearablemusic.player.ui.settings.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hmp.domain.enum.AiPresetEndpoints
import com.hmp.domain.setting.model.AiAccessMode
import com.hmp.domain.setting.model.AiEndpointConfig
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.components.SegmentedControl
import com.hearablemusic.player.ui.common.components.SegmentedOption
import com.hearablemusic.player.ui.common.components.base.TitleWidget
import com.hearablemusic.player.ui.common.dialogs.controller.DialogManager
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hearablemusic.player.ui.common.pages.base.SubScreen
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.library.viewmodel.LibraryViewModel
import com.hearablemusic.player.ui.common.util.activityViewModel
import com.hearablemusic.player.ui.settings.viewmodel.AiSettingsViewModel
import com.hearablemusic.player.ui.settings.viewmodel.RecommendationViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AIScreen(
    aiSettingsViewModel: AiSettingsViewModel = koinViewModel(),
    recommendationViewModel: RecommendationViewModel = activityViewModel(),
    libraryViewModel: LibraryViewModel = activityViewModel(),
    navController: NavBackStack<NavKey>
) {
    val dialogManager = activityViewModel<DialogManagerViewModel>().dialogManager
    LaunchedEffect(Unit) {
        aiSettingsViewModel.loadCustomAiConfig()
    }

    val musicWithExtraCount by libraryViewModel.musicWithExtraCount.collectAsState(initial = 0)
    val pendingCount by recommendationViewModel.pendingMusicCount.collectAsState(initial = 0)
    val aiAccessMode by aiSettingsViewModel.aiAccessMode.collectAsState()
    val freeTrialRemaining by aiSettingsViewModel.aiFreeTrialRemainingCount.collectAsState()
    val customConfig by aiSettingsViewModel.customAiConfig.collectAsState()
    val availableModels by aiSettingsViewModel.availableModels.collectAsState()
    val isTestingApi by aiSettingsViewModel.isTestingApi.collectAsState()
    val apiTestResult by aiSettingsViewModel.apiTestResult.collectAsState()
    val progress by recommendationViewModel.processingProgress.collectAsState()
    val autoBatchProcess by aiSettingsViewModel.autoBatchProcess.collectAsState()
    val refreshMode by aiSettingsViewModel.dailyRefreshMode.collectAsState()
    val refreshHours by aiSettingsViewModel.dailyRefreshHours.collectAsState()
    val startupCount by aiSettingsViewModel.dailyRefreshStartupCount.collectAsState()

    AIScreenContent(
        aiAccessMode = aiAccessMode,
        freeTrialRemaining = freeTrialRemaining,
        customConfig = customConfig,
        availableModels = availableModels,
        isTestingApi = isTestingApi,
        apiTestResult = apiTestResult,
        musicWithExtraCount = musicWithExtraCount,
        pendingCount = pendingCount,
        progress = progress,
        autoBatchProcess = autoBatchProcess,
        refreshMode = refreshMode,
        refreshHours = refreshHours,
        startupCount = startupCount,
        onModeChange = aiSettingsViewModel::switchAiAccessMode,
        onSaveCustomConfig = aiSettingsViewModel::saveCustomAiConfig,
        onFetchModels = aiSettingsViewModel::fetchAvailableModels,
        onTestConnection = aiSettingsViewModel::testAiConnection,
        onClearTestResult = aiSettingsViewModel::clearApiTestResult,
        onAutoBatchProcessChange = aiSettingsViewModel::saveAutoBatchProcess,
        startAutoProcessExtraInfo = recommendationViewModel::startAutoProcessWithCurrentProvider,
        pauseProcess = recommendationViewModel::pauseProcessing,
        resumeProcess = recommendationViewModel::resumeProcessing,
        cancelProcess = recommendationViewModel::cancelProcessing,
        onSaveDailyRefreshMode = aiSettingsViewModel::saveDailyRefreshMode,
        onSaveDailyRefreshHours = aiSettingsViewModel::saveDailyRefreshHours,
        onSaveDailyRefreshStartupCount = aiSettingsViewModel::saveDailyRefreshStartupCount,
        dialogManager = dialogManager,
        onBackClick = { navController.removeLastOrNull() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIScreenContent(
    aiAccessMode: AiAccessMode,
    freeTrialRemaining: Int,
    customConfig: AiEndpointConfig,
    availableModels: List<String>,
    isTestingApi: Boolean,
    apiTestResult: AiSettingsViewModel.ApiTestResult?,
    musicWithExtraCount: Int,
    pendingCount: Int,
    progress: RecommendationViewModel.BatchProcessingProgress,
    autoBatchProcess: Boolean,
    refreshMode: String,
    refreshHours: Int,
    startupCount: Int,
    onModeChange: (AiAccessMode) -> Unit,
    onSaveCustomConfig: (String, String, String) -> Unit,
    onFetchModels: (String, String) -> Unit,
    onTestConnection: (String, String) -> Unit,
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
    val context = LocalContext.current

    // 显示测试结果 Toast
    LaunchedEffect(apiTestResult) {
        apiTestResult?.let { result ->
            val message = when (result) {
                is AiSettingsViewModel.ApiTestResult.Success -> result.message
                is AiSettingsViewModel.ApiTestResult.Error -> result.message
            }
            dialogManager.showMessage(message)
            onClearTestResult()
        }
    }

    SubScreen(
        onBackClick = onBackClick,
        title = stringResource(R.string.title_ai)
    ) {
        val isLandscape = LocalWindowSizeInfo.current.isLandscape
        val tabs = listOf(AiAccessMode.FREE, AiAccessMode.CUSTOM, AiAccessMode.PAID)
        val selectedModeId = aiAccessMode.name

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 顶部模式切换（统一使用 SegmentedControl 风格）
            SegmentedControl(
                modifier = Modifier.fillMaxWidth(),
                options = tabs.map { mode ->
                    SegmentedOption(
                        id = mode.name,
                        label = stringResource(
                            when (mode) {
                                AiAccessMode.FREE -> R.string.ai_tab_free
                                AiAccessMode.CUSTOM -> R.string.ai_tab_custom
                                AiAccessMode.PAID -> R.string.ai_tab_paid
                            }
                        )
                    )
                },
                selectedOption = selectedModeId,
                onOptionSelected = { id ->
                    onModeChange(AiAccessMode.valueOf(id))
                }
            )

            // Tab 内容
            when (aiAccessMode) {
                AiAccessMode.FREE -> FreeTrialContent(
                    freeTrialRemaining = freeTrialRemaining,
                    pendingCount = pendingCount
                )
                AiAccessMode.CUSTOM -> CustomConfigContent(
                    customConfig = customConfig,
                    availableModels = availableModels,
                    isTestingApi = isTestingApi,
                    onSaveConfig = onSaveCustomConfig,
                    onFetchModels = onFetchModels,
                    onTestConnection = onTestConnection,
                    dialogManager = dialogManager
                )
                AiAccessMode.PAID -> PaidModeContent()
            }

            // 批量补全 + 每日刷新（所有 Tab 共享）
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        LoadMusicExtraInfo(
                            pendingCount = pendingCount, musicWithExtraCount = musicWithExtraCount,
                            progress = progress, isConfigured = true,
                            autoBatchProcess = autoBatchProcess, onAutoBatchProcessChange = onAutoBatchProcessChange,
                            startAutoProcessExtraInfo = startAutoProcessExtraInfo,
                            pauseProcess = pauseProcess, resumeProcess = resumeProcess,
                            cancelProcess = cancelProcess, dialogManager = dialogManager
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        DailyRefreshSettings(
                            refreshMode = refreshMode, refreshHours = refreshHours, startupCount = startupCount,
                            onSaveRefreshMode = onSaveDailyRefreshMode, onSaveRefreshHours = onSaveDailyRefreshHours,
                            onSaveStartupCount = onSaveDailyRefreshStartupCount, dialogManager = dialogManager
                        )
                    }
                }
            } else {
                LoadMusicExtraInfo(
                    pendingCount = pendingCount, musicWithExtraCount = musicWithExtraCount,
                    progress = progress, isConfigured = true,
                    autoBatchProcess = autoBatchProcess, onAutoBatchProcessChange = onAutoBatchProcessChange,
                    startAutoProcessExtraInfo = startAutoProcessExtraInfo,
                    pauseProcess = pauseProcess, resumeProcess = resumeProcess,
                    cancelProcess = cancelProcess, dialogManager = dialogManager
                )
                DailyRefreshSettings(
                    refreshMode = refreshMode, refreshHours = refreshHours, startupCount = startupCount,
                    onSaveRefreshMode = onSaveDailyRefreshMode, onSaveRefreshHours = onSaveDailyRefreshHours,
                    onSaveStartupCount = onSaveDailyRefreshStartupCount, dialogManager = dialogManager
                )
            }
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

// ==================== 免费体验 Tab ====================

@Composable
private fun FreeTrialContent(
    freeTrialRemaining: Int,
    pendingCount: Int
) {
    TitleWidget(title = stringResource(R.string.ai_tab_free)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 剩余次数显示
            Text(
                text = stringResource(R.string.ai_free_remaining, freeTrialRemaining),
                style = MaterialTheme.typography.headlineMedium,
                color = if (freeTrialRemaining > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            // 进度条
            val usedPercent = ((100 - freeTrialRemaining).coerceAtLeast(0) / 100f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { usedPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (freeTrialRemaining > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Text(
                text = stringResource(R.string.ai_free_used, 100 - freeTrialRemaining, 100),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (freeTrialRemaining <= 0) {
                Text(
                    text = stringResource(R.string.ai_free_exhausted),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(R.string.ai_free_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ==================== 自定义配置 Tab ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomConfigContent(
    customConfig: AiEndpointConfig,
    availableModels: List<String>,
    isTestingApi: Boolean,
    onSaveConfig: (String, String, String) -> Unit,
    onFetchModels: (String, String) -> Unit,
    onTestConnection: (String, String) -> Unit,
    dialogManager: DialogManager
) {
    val context = LocalContext.current
    var endpointValue by rememberSaveable { mutableStateOf(customConfig.endpoint) }
    var apiKeyValue by rememberSaveable { mutableStateOf("") }
    var modelValue by rememberSaveable { mutableStateOf(customConfig.selectedModel) }
    var showPassword by remember { mutableStateOf(false) }
    var presetExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }

    // 当配置加载后更新模型
    LaunchedEffect(customConfig) {
        if (customConfig.selectedModel.isNotBlank()) {
            modelValue = customConfig.selectedModel
        }
    }

    TitleWidget(title = stringResource(R.string.ai_tab_custom)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 预设快捷按钮
            Text(
                text = stringResource(R.string.ai_preset_quick_fill),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiPresetEndpoints.ALL.forEach { preset ->
                    Button(
                        onClick = {
                            endpointValue = preset.endpoint
                            modelValue = preset.defaultModel
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(preset.displayName, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Endpoint 输入
            OutlinedTextField(
                value = endpointValue,
                onValueChange = { endpointValue = it },
                label = { Text(stringResource(R.string.ai_endpoint)) },
                placeholder = { Text("https://api.example.com/v1") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // API Key 输入
            TextField(
                value = apiKeyValue,
                onValueChange = { apiKeyValue = it },
                label = { Text(stringResource(R.string.api_key), color = MaterialTheme.colorScheme.onBackground) },
                placeholder = { Text(stringResource(R.string.enter_api_key_placeholder, ""), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Transparent,
                    unfocusedIndicatorColor = Transparent,
                    disabledIndicatorColor = Transparent
                ),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // 获取模型按钮
            Button(
                onClick = {
                    if (endpointValue.isNotBlank() && apiKeyValue.isNotBlank()) {
                        onFetchModels(endpointValue, apiKeyValue)
                    } else {
                        dialogManager.showMessage(context.getString(R.string.please_enter_api_key))
                    }
                },
                enabled = endpointValue.isNotBlank() && apiKeyValue.isNotBlank() && !isTestingApi,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTestingApi) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.ai_fetch_models), color = MaterialTheme.colorScheme.onPrimary)
            }

            // 模型下拉选择
            if (availableModels.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = modelExpanded,
                    onExpandedChange = { modelExpanded = !modelExpanded }
                ) {
                    TextField(
                        value = modelValue.ifBlank { availableModels.firstOrNull() ?: "" },
                        onValueChange = { modelValue = it },
                        readOnly = true,
                        label = { Text(stringResource(R.string.model_name)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Transparent,
                            unfocusedIndicatorColor = Transparent,
                            disabledIndicatorColor = Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = modelExpanded,
                        onDismissRequest = { modelExpanded = false }
                    ) {
                        availableModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    modelValue = model
                                    modelExpanded = false
                                }
                            )
                        }
                    }
                }
            } else if (modelValue.isNotBlank()) {
                OutlinedTextField(
                    value = modelValue,
                    onValueChange = { modelValue = it },
                    label = { Text(stringResource(R.string.model_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 测试 + 保存按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (endpointValue.isNotBlank() && apiKeyValue.isNotBlank()) {
                            onTestConnection(endpointValue, apiKeyValue)
                        }
                    },
                    enabled = endpointValue.isNotBlank() && apiKeyValue.isNotBlank() && !isTestingApi,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTestingApi) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.test), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Button(
                    onClick = {
                        if (endpointValue.isNotBlank() && apiKeyValue.isNotBlank()) {
                            onSaveConfig(endpointValue, apiKeyValue, modelValue)
                            dialogManager.showMessage(context.getString(R.string.config_saved))
                        } else {
                            dialogManager.showMessage(context.getString(R.string.please_enter_api_key))
                        }
                    },
                    enabled = endpointValue.isNotBlank() && apiKeyValue.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save), color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            // 配置状态
            if (customConfig.isConfigured) {
                Text(
                    text = stringResource(R.string.configured),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ==================== 付费模式 Tab ====================

@Composable
private fun PaidModeContent() {
    TitleWidget(title = stringResource(R.string.ai_tab_paid)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.ai_paid_coming_soon),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.ai_paid_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== 每日推荐刷新策略（复用原有实现）====================

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
                            type = MenuAnchorType.PrimaryEditable,
                            enabled = true
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

// ==================== 批量补全（复用原有实现）====================

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
                        if (pendingCount <= 0) {
                            dialogManager.showMessage(context.getString(R.string.no_pending_music))
                            return@Button
                        }
                        startAutoProcessExtraInfo()
                    },
                    enabled = true
                ) {
                    Text(text = stringResource(R.string.start_batch_completion), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
