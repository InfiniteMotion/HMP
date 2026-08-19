package com.hearablemusic.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hearablemusic.player.ui.common.components.BottomFusionBar
import com.hearablemusic.player.ui.common.components.TabPageIndicator
import com.hearablemusic.player.ui.common.components.base.UiStateContent
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.common.layout.WindowWidthSizeClass
import com.hearablemusic.player.ui.common.layout.rememberAppWindowSizeInfo
import com.hearablemusic.player.ui.generated.resources.Res
import com.hearablemusic.player.ui.generated.resources.tab_gallery
import com.hearablemusic.player.ui.generated.resources.tab_home
import com.hearablemusic.player.ui.generated.resources.tab_list
import com.hearablemusic.player.ui.generated.resources.tab_user
import com.hearablemusic.player.ui.library.pages.components.musiclist.CurrentPlayingConfig
import com.hearablemusic.player.ui.library.pages.components.musiclist.EditConfig
import com.hearablemusic.player.ui.library.pages.components.musiclist.MusicList
import com.hearablemusic.player.ui.library.pages.components.musiclist.FullItemOptions
import com.hearablemusic.player.ui.library.pages.components.musiclist.HeaderConfig
import com.hearablemusic.player.ui.library.pages.components.musiclist.ItemConfig
import com.hearablemusic.player.ui.library.pages.components.musiclist.ItemVariant
import com.hearablemusic.player.ui.library.pages.components.musiclist.MusicListCallbacksAdapter
import com.hearablemusic.player.ui.library.pages.components.musiclist.defaultMusicListConfig
import com.hearablemusic.player.ui.library.viewmodel.LibraryListViewModel
import com.hmp.domain.music.MusicInfo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Tab 壳 + 首页列表主路径（方案 §7 第 2a/2b 步）。
 *
 * 形态：HorizontalPager 4 页 + 顶部 TabPageIndicator + 底部 BottomFusionBar。
 * 首页为真实音乐列表（LibraryListViewModel → MusicList）；列表项点击为占位空实现
 * （仅触觉反馈，播放接线留第 3 步 PlaybackController）；
 * 播放胶囊 musicInfo=null 占位，真实接线同留第 3 步。
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
                        0 -> HomeTab()
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

/**
 * 首页列表主路径（2b）：真实音乐列表，LazyColumn 可滚动。
 * 点击播放占位：仅触觉反馈（第 3 步接 PlaybackController.playWith）。
 */
@Composable
private fun HomeTab(
    viewModel: LibraryListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

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
        UiStateContent(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) { musicList ->
            MusicListSection(musicList)
        }
    }
}

/** 与 androidMain 整页列表（ListScreen 等）同构：MusicList 懒加载版，内部 LazyColumn 自滚动。 */
@Composable
private fun MusicListSection(musicList: List<MusicInfo>) {
    // 点击播放占位空实现（方案 2b：真实接线第 3 步 PlaybackController）
    val callbacks = remember {
        object : MusicListCallbacksAdapter() {
            override fun onItemClick(musicInfo: MusicInfo, index: Int) {
                // TODO(第 3 步): PlaybackController.playWith(musicInfo)
            }
        }
    }
    val config = remember(callbacks) {
        defaultMusicListConfig(callbacks).copy(
            header = HeaderConfig.None,
            item = ItemConfig(
                variant = ItemVariant.Full,
                showIndex = true,
                fullOptions = FullItemOptions(
                    showPinButton = false,
                    showRemoveButton = false,
                    showMenuButton = true
                ),
            ),
            edit = EditConfig(enabled = false),
            currentPlaying = CurrentPlayingConfig(
                index = null,
                autoScrollToCurrent = false
            ),
        )
    }
    // MusicList：内部 LazyColumn，自带滚动（FixedMusicList 为非懒版，仅用于外层已滚动的场景）
    MusicList(
        musicInfoList = musicList,
        config = config,
        modifier = Modifier.fillMaxSize(),
        isPlaying = false,
    )
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
                text = "$title · 2c",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}
