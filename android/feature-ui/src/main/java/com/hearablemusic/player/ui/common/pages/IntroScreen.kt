package com.hearablemusic.player.ui.common.pages

import android.Manifest
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.dialogs.MusicScanDialog
import com.hearablemusic.player.ui.common.design.animation.AnimationTokens
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_BLUR_RADIUS
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_INTENSITY
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_MATERIAL_PRESET
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_MODE
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_NOISE_FACTOR
import com.hearablemusic.player.ui.common.util.DEFAULT_HAZE_TINT_ALPHA
import com.hearablemusic.player.ui.common.util.HazeRenderSettings
import com.hearablemusic.player.ui.common.util.ProvideHazeRenderSettings
import com.hearablemusic.player.ui.library.viewmodel.LibraryViewModel
import com.hearablemusic.player.ui.settings.viewmodel.RecommendationViewModel
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel
import com.hmp.domain.setting.model.AiAccessMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun IntroScreen(
    settingsViewModel: SettingsViewModel,
    libraryViewModel: LibraryViewModel,
    recommendationViewModel: RecommendationViewModel,
    onFinished: ()-> Unit
) {
    val currentStep = remember { mutableIntStateOf(0) }
    val isPermissionGiven = remember { mutableStateOf(false) }
    val showScanDialog = remember { mutableStateOf(false) }
    val isScanCompleted = remember { mutableStateOf(false) }
    val aiAccessMode by settingsViewModel.aiAccessMode.collectAsState(AiAccessMode.FREE)
    val hazeMode by settingsViewModel.hazeMode.collectAsState(DEFAULT_HAZE_MODE)
    val hazeMaterialPreset by settingsViewModel.hazeMaterialPreset.collectAsState(DEFAULT_HAZE_MATERIAL_PRESET)
    val hazeBlurRadius by settingsViewModel.hazeBlurRadius.collectAsState(DEFAULT_HAZE_BLUR_RADIUS)
    val hazeNoiseFactor by settingsViewModel.hazeNoiseFactor.collectAsState(DEFAULT_HAZE_NOISE_FACTOR)
    val hazeTintAlpha by settingsViewModel.hazeTintAlpha.collectAsState(DEFAULT_HAZE_TINT_ALPHA)
    val hazeIntensity by settingsViewModel.hazeIntensity.collectAsState(DEFAULT_HAZE_INTENSITY)
    
    val hazeState = rememberHazeState()
    
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

    ProvideHazeRenderSettings(
        settings = HazeRenderSettings(
            mode = hazeMode,
            preset = hazeMaterialPreset,
            intensity = hazeIntensity,
            blurRadius = hazeBlurRadius,
            noiseFactor = hazeNoiseFactor,
            tintAlpha = hazeTintAlpha
        )
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .hazeSource(state = hazeState)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── 统一容器：所有内容并入单一卡片 ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(top = 40.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── 卡片顶部：Logo + 品牌区 ──
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = "Logo",
                        modifier = Modifier.size(88.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.welcome_to),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "Hearable",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " Music Player",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── 胶囊式步骤指示器 ──
                    StepIndicator(
                        currentStep = currentStep.intValue,
                        totalSteps = 3
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── 细分隔线 ──
                    HorizontalDivider(
                        modifier = Modifier.widthIn(max = 240.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // ── 步骤内容区（固定高度，保证三个步骤大小一致） ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentStep.intValue,
                            transitionSpec = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = AnimationTokens.EASE_OUT
                                    )
                                ) + fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = AnimationTokens.EASE_OUT
                                    )
                                ) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = AnimationTokens.EASE_IN
                                    )
                                ) + fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = AnimationTokens.EASE_IN
                                    )
                                )
                            },
                            label = "step_transition"
                        ) { step ->
                            when (step) {
                                0 -> PermissionStep(
                                    isPermissionGiven = isPermissionGiven.value,
                                    onRequestPermission = {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.READ_MEDIA_AUDIO,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            )
                                        )
                                    }
                                )
                                1 -> ScanMusicStep(
                                    isScanCompleted = isScanCompleted.value,
                                    onStartScan = {
                                        libraryViewModel.refreshMusicList()
                                        showScanDialog.value = true
                                    },
                                    onScanComplete = {
                                        showScanDialog.value = false
                                        isScanCompleted.value = true
                                        currentStep.intValue = 2
                                        // 首次扫描完成后，若为免费体验模式则自动触发 AI 批量补全
                                        // isLoadMusic 已在 LibraryViewModel 扫描成功后持久化
                                        if (aiAccessMode == AiAccessMode.FREE) {
                                            recommendationViewModel.startAutoProcessWithCurrentProvider()
                                        }
                                    },
                                    showScanDialog = showScanDialog.value,
                                    libraryViewModel = libraryViewModel,
                                    hazeState = hazeState
                                )
                                2 -> AiExperienceStep(
                                    onFinished = onFinished
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 胶囊式步骤指示器
 * - 已完成：实心圆点 + 主色
 * - 当前：拉伸为胶囊 + 主色
 * - 未到达：小圆点 + onSurface 低透明
 */
@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isCurrent = index == currentStep
            val isDone = index < currentStep
            val color = when {
                isCurrent -> MaterialTheme.colorScheme.primary
                isDone -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            }
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .then(
                        if (isCurrent) Modifier.width(28.dp) else Modifier.size(6.dp)
                    )
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun PermissionStep(
    isPermissionGiven: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.intro_step_1),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.intro_step_1_desc),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        if (!isPermissionGiven) {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.width(200.dp)
            ) {
                Text(stringResource(R.string.grant_permission))
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
                modifier = Modifier.width(200.dp)
            ) {
                Text(stringResource(R.string.permission_granted))
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
    libraryViewModel: LibraryViewModel?,
    hazeState: HazeState
) {
    Column(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.intro_step_2),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.intro_step_2_desc),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        if (isScanCompleted) {
            Button(
                onClick = { },
                colors = ButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                modifier = Modifier.width(200.dp)
            ) {
                Text(stringResource(R.string.scan_finished))
            }
        } else {
            Button(
                onClick = onStartScan,
                modifier = Modifier.width(200.dp)
            ) {
                Text(stringResource(R.string.scan))
            }
        }
    }

    if (showScanDialog && libraryViewModel != null) {
        MusicScanDialog(
            libraryViewModel = libraryViewModel,
            onDismiss = onScanComplete,
            hazeState = hazeState
        )
    }
}

@Composable
fun AiExperienceStep(
    onFinished: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.intro_step_3),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.intro_step_3_desc),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onFinished,
            modifier = Modifier.width(200.dp)
        ) {
            Text(stringResource(R.string.start_experience))
        }
    }
}
