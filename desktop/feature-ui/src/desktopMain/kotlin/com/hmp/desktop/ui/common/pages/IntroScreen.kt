package com.hmp.desktop.ui.common.pages

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.common.dialogs.MusicScanDialog
import com.hmp.desktop.ui.common.design.animation.AnimationTokens
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_BLUR_RADIUS
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_INTENSITY
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_MATERIAL_PRESET
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_MODE
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_NOISE_FACTOR
import com.hmp.desktop.ui.common.util.DEFAULT_HAZE_TINT_ALPHA
import com.hmp.desktop.ui.common.util.HazeRenderSettings
import com.hmp.desktop.ui.common.util.ProvideHazeRenderSettings
import com.hmp.desktop.ui.library.viewmodel.LibraryViewModel
import com.hmp.desktop.ui.settings.viewmodel.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun IntroScreen(
    settingsViewModel: SettingsViewModel,
    libraryViewModel: LibraryViewModel,
    onFinished: ()-> Unit
) {
    val currentStep = remember { mutableIntStateOf(1) } // Desktop: skip permission step, start at scan
    val isPermissionGiven = remember { mutableStateOf(true) } // Desktop: no runtime permissions needed
    val showScanDialog = remember { mutableStateOf(false) }
    val isScanCompleted = remember { mutableStateOf(false) }
    val hazeMode by settingsViewModel.hazeMode.collectAsState(DEFAULT_HAZE_MODE)
    val hazeMaterialPreset by settingsViewModel.hazeMaterialPreset.collectAsState(DEFAULT_HAZE_MATERIAL_PRESET)
    val hazeBlurRadius by settingsViewModel.hazeBlurRadius.collectAsState(DEFAULT_HAZE_BLUR_RADIUS)
    val hazeNoiseFactor by settingsViewModel.hazeNoiseFactor.collectAsState(DEFAULT_HAZE_NOISE_FACTOR)
    val hazeTintAlpha by settingsViewModel.hazeTintAlpha.collectAsState(DEFAULT_HAZE_TINT_ALPHA)
    val hazeIntensity by settingsViewModel.hazeIntensity.collectAsState(DEFAULT_HAZE_INTENSITY)

    val hazeState = rememberHazeState()

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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Image(
            painter = painterResource(Res.drawable.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally),

            )
        Text(
            text = stringResource(Res.string.welcome_to),
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
            text = stringResource(Res.string.follow_guide_to_config),
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
                            // Desktop: no runtime permissions needed
                            isPermissionGiven.value = true
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
                        },
                        showScanDialog = showScanDialog.value,
                        libraryViewModel = libraryViewModel,
                        hazeState = hazeState
                    )
                    2 -> StartExperienceStep(
                        onFinished = onFinished
                    )
                }
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
            text = stringResource(Res.string.intro_step_1),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.intro_step_1_desc),
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
                Text(stringResource(Res.string.grant_permission))
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
                Text(stringResource(Res.string.permission_granted))
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(Res.string.intro_step_2),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.intro_step_2_desc),
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
                Text(stringResource(Res.string.scan_finished))
            }
        } else {
            Button(
                onClick = onStartScan,
                modifier = Modifier.width(150.dp)
            ) {
                Text(stringResource(Res.string.scan))
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
            text = stringResource(Res.string.intro_step_3),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.intro_step_3_desc),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(max = 300.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onFinished,
            modifier = Modifier.width(150.dp)
        ) {
            Text(stringResource(Res.string.start_experience))
        }
    }
}
