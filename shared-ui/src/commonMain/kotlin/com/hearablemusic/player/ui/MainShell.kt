package com.hearablemusic.player.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hearablemusic.player.ui.common.components.BottomFusionBar
import com.hearablemusic.player.ui.common.components.TabPageIndicator
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.common.layout.WindowWidthSizeClass
import com.hearablemusic.player.ui.common.layout.rememberAppWindowSizeInfo
import com.hearablemusic.player.ui.generated.resources.Res
import com.hearablemusic.player.ui.generated.resources.tab_gallery
import com.hearablemusic.player.ui.generated.resources.tab_home
import com.hearablemusic.player.ui.generated.resources.tab_list
import com.hearablemusic.player.ui.generated.resources.tab_user
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Tab 壳 + 首页骨架（方案 §7 第 2a 步「壳与首页骨架能跑」）。
 *
 * 形态：HorizontalPager 4 页骨架 + 顶部 TabPageIndicator + 底部 BottomFusionBar。
 * 播放控制回调为占位空实现（musicInfo=null → 仅导航胶囊），真实接线留第 3 步；
 * 首页推荐卡片为静态骨架，列表主路径（MusicList）第 2b 步迁入。
 */
@Composable
fun MainShell() {
    val tabCount = 4
    val pagerState = rememberPagerState { tabCount }
    val scope = rememberCoroutineScope()
    val windowSizeInfo = rememberAppWindowSizeInfo()

    CompositionLocalProvider(LocalWindowSizeInfo provides windowSizeInfo) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                TabPageIndicator(
                    currentPage = pagerState.currentPage,
                    totalPages = tabCount,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 3,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> HomeSkeleton()
                        1 -> SkeletonPlaceholder(title = stringResource(Res.string.tab_gallery))
                        2 -> SkeletonPlaceholder(title = stringResource(Res.string.tab_list))
                        3 -> SkeletonPlaceholder(title = stringResource(Res.string.tab_user))
                    }
                }
            }

            // 底部融合栏：导航真实可用；播放区占位（musicInfo=null，回调空实现，第 3 步接 PlaybackController）
            BottomFusionBar(
                musicInfo = null,
                isPlaying = false,
                progress = 0f,
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                onPlayPause = {},
                onNext = {},
                onPrev = {},
                onOpenPlayer = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                showNavText = windowSizeInfo.isLandscape,
                showNavCapsule = true,
                maxWidth = when (windowSizeInfo.widthSizeClass) {
                    WindowWidthSizeClass.Compact -> 480.dp
                    WindowWidthSizeClass.Medium -> 640.dp
                    WindowWidthSizeClass.Expanded -> null
                }
            )
        }
    }
}

/** 首页骨架：大标题 + 推荐入口卡片网格占位（真实推荐流第 2b/后续接 ViewModel）。 */
@Composable
private fun HomeSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(Res.string.tab_home),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonCard(modifier = Modifier.weight(1f), height = 120)
                SkeletonCard(modifier = Modifier.weight(1f), height = 120)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonCard(modifier = Modifier.weight(1f), height = 120)
                SkeletonCard(modifier = Modifier.weight(1f), height = 120)
            }
        }
    }
}

/** 其余 Tab 页骨架占位：大标题 + 居中提示。 */
@Composable
private fun SkeletonPlaceholder(title: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "$title · 2b",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun SkeletonCard(modifier: Modifier = Modifier, height: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.height(height.dp)
    ) {}
}
