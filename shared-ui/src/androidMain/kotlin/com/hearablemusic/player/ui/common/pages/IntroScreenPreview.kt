package com.hearablemusic.player.ui.common.pages

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.design.theme.HearableMusicPlayerTheme
import dev.chrisbanes.haze.rememberHazeState

/**
 * IntroScreen 视觉预览（不依赖 ViewModel）
 * 复刻 IntroScreen 的卡片布局结构，可直接在 Android Studio 中渲染。
 */
@Preview(name = "Intro - Step 1 Permission", showBackground = true, heightDp = 800, widthDp = 420)
@Preview(name = "Intro - Step 1 Dark", showBackground = true, heightDp = 800, widthDp = 420, uiMode = 0x20)
@Composable
fun IntroScreenPreview() {
    var currentStep by remember { mutableIntStateOf(0) }

    HearableMusicPlayerTheme(darkTheme = false) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        text = "Welcome to",
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
                    StepIndicatorPreview(
                        currentStep = currentStep,
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

                    // ── 步骤内容预览（固定高度，点击按钮切换步骤） ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (currentStep) {
                            0 -> PermissionStep(
                                isPermissionGiven = false,
                                onRequestPermission = { currentStep = 1 }
                            )
                            1 -> ScanMusicStep(
                                isScanCompleted = false,
                                showScanDialog = false,
                                onStartScan = { currentStep = 2 },
                                onScanComplete = {},
                                libraryViewModel = null,
                                hazeState = rememberHazeState()
                            )
                            2 -> AiExperienceStep(
                                onFinished = { currentStep = 0 }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * StepIndicator 的预览副本（原函数为 private，这里复制以便 Preview 调用）。
 */
@Composable
private fun StepIndicatorPreview(
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
