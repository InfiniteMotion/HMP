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
import androidx.compose.runtime.LaunchedEffect
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
import com.hearablemusic.player.ui.platform.PlaybackController
import com.hmp.domain.music.MusicInfo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Tab 壳 + 首页列表主路径 + 播放接线（方案 §7 第 2a/2b/3 步）。
 *
 * 形态：HorizontalPager 4 页 + 顶部 TabPageIndicator + 底部 BottomFusionBar。
 * 首页为真实音乐列表（LibraryListViewModel → MusicList）；
 * 第 3 步：经冻结接口 [PlaybackController] 接通播放——
 * 列表点击 playWith 真播放、播放胶囊实时显示当前曲目/进度、暂停/切歌可用、当前播放行高亮。
 */
@Composable
fun MainShell() {
    val tabCount = 4
    val pagerState = rememberPagerState { tabCount }
    val scope = rememberCoroutineScope()
    val windowSizeInfo = rememberAppWindowSizeInfo()
    val controller: PlaybackController = koinInject()
    val currentMusic by controller.currentPlayingMusic.collectAsState()
    val isPlaying by controller.isPlaying.collectAsState()
    val position by controller.currentPosition.collectAsState()
    val totalDuration by controller.duration.collectAsState()

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
                        0 -> HomeTab(controller = controller, currentMusic = currentMusic)
                        1 -> SkeletonPlaceholder(title = stringResource(Res.string.tab_gallery))
                        2 -> SkeletonPlaceholder(title = stringResource(Res.string.tab_list))
                        3 -> SkeletonPlaceholder(title = stringResource(Res.string.tab_user))
                    }
                }
            }

            // 底部融合栏（第 3 步接线）：导航 + 播放胶囊实时状态（当前曲目/暂停态/进度环）
            BottomFusionBar(
                musicInfo = currentMusic,
                isPlaying = isPlaying,
                progress = if (totalDuration > 0) position.toFloat() / totalDuration else 0f,
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                onPlayPause = { if (isPlaying) controller.pauseMusic() else controller.playOrResume() },
                onNext = { controller.playNext() },
                onPrev = { controller.playPrevious() },
                onOpenPlayer = { /* 播放页第 4 步迁入 */ },
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
 * 首页列表主路径（2b+3）：真实音乐列表 + 播放接线。
 * 与旧 UI 页面模式一致：列表加载成功即建播放队列（addAllToPlaylistInOrder），
 * 点击单曲 playWith 真播放；当前播放行高亮（id 匹配列表下标）。
 */
@Composable
private fun HomeTab(
    controller: PlaybackController,
    currentMusic: MusicInfo?,
    viewModel: LibraryListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

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
            // 列表加载成功即作为播放队列上下文（与旧 UI ArtistScreen/AlbumScreen 页面模式一致）
            LaunchedEffect(musicList) {
                controller.addAllToPlaylistInOrder(musicList)
            }
            val currentPlayingIndex = musicList.indexOfFirst {
                it.music.id == currentMusic?.music?.id
            }.takeIf { it >= 0 }
            MusicListSection(
                musicList = musicList,
                currentPlayingIndex = currentPlayingIndex,
                onItemClick = { musicInfo ->
                    scope.launch { controller.playWith(musicInfo) }
                },
            )
        }
    }
}

/** 与 androidMain 整页列表（ListScreen 等）同构：MusicList 懒加载版，内部 LazyColumn 自滚动。 */
@Composable
private fun MusicListSection(
    musicList: List<MusicInfo>,
    currentPlayingIndex: Int?,
    onItemClick: (MusicInfo) -> Unit,
) {
    val callbacks = remember(onItemClick) {
        object : MusicListCallbacksAdapter() {
            override fun onItemClick(musicInfo: MusicInfo, index: Int) {
                onItemClick(musicInfo)
            }
        }
    }
    val config = remember(callbacks, currentPlayingIndex) {
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
                index = currentPlayingIndex,
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
