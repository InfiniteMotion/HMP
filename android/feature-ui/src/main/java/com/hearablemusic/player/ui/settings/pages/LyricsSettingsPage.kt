package com.example.hearablemusicplayer.ui.settings.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.hearablemusicplayer.ui.player.floating.FloatingLyricsService
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.components.base.TitleWidget
import com.hearablemusic.player.ui.common.design.dimens.LocalHMPDimens
import com.hearablemusic.player.ui.common.pages.base.SubScreen
import com.hearablemusic.player.ui.player.components.MiniPlayerSafeSpacer
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.lyrics.LyricsComponent
import com.hmp.domain.lyrics.LyricsComponentConfig
import com.hmp.domain.setting.usecase.LyricsSettingsUseCase
import kotlinx.coroutines.launch

@Composable
fun LyricsSettingsPage(
    lyricsSettingsUseCase: LyricsSettingsUseCase,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dimens = LocalHMPDimens.current

    var selectedTab by remember { mutableStateOf(LyricsComponent.PLAYER) }
    var playerConfig by remember { mutableStateOf(LyricsComponentConfig.DEFAULT) }
    var fullscreenConfig by remember { mutableStateOf(LyricsComponentConfig.DEFAULT) }
    var floatingConfig by remember { mutableStateOf(LyricsComponentConfig.DEFAULT) }

    LaunchedEffect(Unit) {
        val configs = lyricsSettingsUseCase.getAllComponentConfigs()
        playerConfig = configs[LyricsComponent.PLAYER] ?: LyricsComponentConfig.DEFAULT
        fullscreenConfig = configs[LyricsComponent.FULLSCREEN] ?: LyricsComponentConfig.DEFAULT
        floatingConfig = configs[LyricsComponent.FLOATING] ?: LyricsComponentConfig.DEFAULT
    }

    val floatingEnabled by lyricsSettingsUseCase.floatingLyricsEnabled.collectAsState(false)

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(context)) {
            scope.launch { lyricsSettingsUseCase.saveFloatingLyricsEnabled(true) }
            context.startService(Intent(context, FloatingLyricsService::class.java))
        } else {
            scope.launch { lyricsSettingsUseCase.saveFloatingLyricsEnabled(false) }
        }
    }

    fun startFloatingLyrics() {
        Log.d("FloatingLyrics", "startFloatingLyrics called, canDrawOverlays=${Settings.canDrawOverlays(context)}")
        if (Settings.canDrawOverlays(context)) {
            scope.launch { lyricsSettingsUseCase.saveFloatingLyricsEnabled(true) }
            val intent = Intent(context, FloatingLyricsService::class.java)
            Log.d("FloatingLyrics", "Starting service: $intent")
            context.startService(intent)
        } else {
            Log.d("FloatingLyrics", "Launching permission request")
            overlayPermissionLauncher.launch(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}"))
            )
        }
    }

    fun stopFloatingLyrics() {
        Log.d("FloatingLyrics", "stopFloatingLyrics called")
        scope.launch { lyricsSettingsUseCase.saveFloatingLyricsEnabled(false) }
        context.stopService(Intent(context, FloatingLyricsService::class.java))
    }

    fun currentConfig() = when (selectedTab) {
        LyricsComponent.PLAYER -> playerConfig
        LyricsComponent.FULLSCREEN -> fullscreenConfig
        LyricsComponent.FLOATING -> floatingConfig
    }

    fun updateCurrent(config: LyricsComponentConfig) {
        when (selectedTab) {
            LyricsComponent.PLAYER -> playerConfig = config
            LyricsComponent.FULLSCREEN -> fullscreenConfig = config
            LyricsComponent.FLOATING -> floatingConfig = config
        }
        scope.launch { lyricsSettingsUseCase.saveComponentConfig(selectedTab, config) }
    }

    val tabLabels = mapOf(
        LyricsComponent.PLAYER to "播放页",
        LyricsComponent.FULLSCREEN to "全屏",
        LyricsComponent.FLOATING to "悬浮歌词"
    )

    SubScreen(
        onBackClick = onBack,
        title = stringResource(R.string.lyrics_settings)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(dimens.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing.lg)
        ) {
            // 组件切换
            TitleWidget(title = "歌词组件") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    LyricsComponent.entries.forEachIndexed { idx, comp ->
                        SegmentedButton(
                            selected = selectedTab == comp,
                            onClick = { selectedTab = comp },
                            shape = SegmentedButtonDefaults.itemShape(idx, LyricsComponent.entries.size)
                        ) { Text(tabLabels[comp] ?: comp.key) }
                    }
                }
            }

            // 悬浮歌词开关 — 仅 floating tab 时显示
            if (selectedTab == LyricsComponent.FLOATING) {
                TitleWidget(title = "悬浮歌词") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "开启悬浮歌词",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = floatingEnabled,
                                onCheckedChange = { if (it) startFloatingLyrics() else stopFloatingLyrics() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            val config = currentConfig()

            // 显示模式
            TitleWidget(title = "显示模式") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("DUAL" to "双语", "LANG1" to "原文", "LANG2" to "翻译").forEachIndexed { idx, (key, label) ->
                        SegmentedButton(
                            selected = config.displayMode.name == key,
                            onClick = { updateCurrent(config.copy(displayMode = DisplayMode.valueOf(key))) },
                            shape = SegmentedButtonDefaults.itemShape(idx, 3)
                        ) { Text(label, fontSize = 13.sp) }
                    }
                }
            }

            // 对齐方式
            TitleWidget(title = "对齐方式") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("LEFT" to "左对齐", "CENTER" to "居中", "RIGHT" to "右对齐").forEachIndexed { idx, (key, label) ->
                        SegmentedButton(
                            selected = config.alignment.name == key,
                            onClick = { updateCurrent(config.copy(alignment = LyricsAlignment.valueOf(key))) },
                            shape = SegmentedButtonDefaults.itemShape(idx, 3)
                        ) { Text(label, fontSize = 13.sp) }
                    }
                }
            }

            // 文本设置
            TitleWidget(title = "文本设置") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacing.sm)
                ) {
                    NumericRow("原文字号", config.originalTextSize, 10..28) {
                        updateCurrent(config.copy(originalTextSize = it))
                    }
                    NumericRow("翻译字号", config.translatedTextSize, 10..28) {
                        updateCurrent(config.copy(translatedTextSize = it))
                    }
                    NumericRow("当前行字号", config.currentTimeTextSize, 10..28) {
                        updateCurrent(config.copy(currentTimeTextSize = it))
                    }
                    NumericRow("行间距", config.lineSpacing, 0..20) {
                        updateCurrent(config.copy(lineSpacing = it))
                    }
                }
            }

            // 共享设置
            TitleWidget(title = "共享设置") {
                val linkOptions = listOf(null to "独立配置") +
                    LyricsComponent.entries.filter { it != selectedTab }.map { it.key to "跟随「${tabLabels[it]}」" }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    linkOptions.forEach { (key, label) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { updateCurrent(config.copy(linkedTo = key)) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (config.linkedTo == key)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = config.linkedTo == key,
                                    onClick = { updateCurrent(config.copy(linkedTo = key)) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 重置
            TextButton(
                onClick = {
                    scope.launch {
                        lyricsSettingsUseCase.resetComponentToDefault(selectedTab)
                        when (selectedTab) {
                            LyricsComponent.PLAYER -> playerConfig = LyricsComponentConfig.DEFAULT
                            LyricsComponent.FULLSCREEN -> fullscreenConfig = LyricsComponentConfig.DEFAULT
                            LyricsComponent.FLOATING -> floatingConfig = LyricsComponentConfig.DEFAULT
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "重置 ${tabLabels[selectedTab]} 为默认",
                    color = MaterialTheme.colorScheme.error
                )
            }

            MiniPlayerSafeSpacer(height = 56.dp)
        }
    }
}

@Composable
private fun NumericRow(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { if (value > range.first) onChange(value - 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("−", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    "$value",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                TextButton(
                    onClick = { if (value < range.last) onChange(value + 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("+", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
