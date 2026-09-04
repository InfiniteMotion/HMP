package com.hearablemusic.player.ui.library.pages.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.hmp.domain.agent.sub.AnchorContent
import com.hmp.domain.agent.sub.AnniversaryContent
import com.hmp.domain.agent.sub.DiscoverContent
import com.hmp.domain.agent.sub.ForgottenContent
import com.hmp.domain.agent.sub.GreetingContent
import com.hmp.domain.agent.sub.RadioStatusContent
import com.hmp.domain.agent.sub.RecommendContent
import com.hmp.domain.agent.sub.SlideCard
import com.hmp.domain.agent.sub.SlideType
import com.hmp.domain.agent.sub.TimePhase
import com.hmp.domain.music.MusicRepository
import com.hearablemusic.player.ui.generated.resources.Res
import com.hearablemusic.player.ui.generated.resources.unknown
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

private const val AUTO_ROTATE_MS = 4000L
private const val FOCUS_DISPLAY_MS = 4000L

// ═══════════════════════════════════════════════════════════════════
// HelloSlideCardStack — 外部入口
// ═══════════════════════════════════════════════════════════════════

@Composable
fun HelloSlideCardStack(
    modifier: Modifier = Modifier,
    onCardClick: ((SlideCard) -> Unit)? = null,
) {
    val masterAgent: com.hmp.domain.agent.runtime.MasterAgent? = koinInject()
    val emptyFlow = remember { MutableStateFlow<List<SlideCard>>(emptyList()) }
    val allCards by remember(masterAgent) {
        masterAgent?.helloAgent()?.cards ?: emptyFlow
    }.collectAsState()
    val cardList = allCards.filter { it.visible }

    Box(modifier = modifier) {
        when {
            cardList.isEmpty() -> HelloCard(HelloFallbackCard(), Modifier.fillMaxSize())

            // 只有 1 张卡 → 直接显示
            cardList.size <= 1 -> HelloCard(cardList.first(), Modifier.fillMaxSize(), onCardClick)

            // 多张卡 → Pager 轮播
            else -> RotatingPersistentCards(
                cards = cardList,
                modifier = Modifier.fillMaxSize(),
                onCardClick = onCardClick,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// RotatingPersistentCards — Pager 轮播（所有卡统一进 Pager）
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun RotatingPersistentCards(
    cards: List<SlideCard>,
    modifier: Modifier = Modifier,
    onCardClick: ((SlideCard) -> Unit)? = null,
) {
    val pagerState = rememberPagerState(pageCount = { cards.size })
    var isPaused by remember { mutableStateOf(false) }
    var lastFocusedAt by remember { mutableStateOf(0L) }

    // A. focusedAt 聚焦：replace 时引擎层设 focusedAt → UI 检测到新值 → animateScrollToPage
    LaunchedEffect(cards) {
        val maxFocus = cards.maxOfOrNull { it.focusedAt } ?: 0L
        if (maxFocus > lastFocusedAt) {
            val focusedIndex = cards.indexOfFirst { it.focusedAt == maxFocus }
            if (focusedIndex >= 0 && focusedIndex != pagerState.currentPage) {
                pagerState.animateScrollToPage(focusedIndex)
                isPaused = true
                kotlinx.coroutines.delay(FOCUS_DISPLAY_MS)
                isPaused = false
            }
            lastFocusedAt = maxFocus
        }
    }

    // 自动轮播
    LaunchedEffect(isPaused, cards.size) {
        if (cards.size <= 1 || isPaused) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(AUTO_ROTATE_MS)
            val next = (pagerState.currentPage + 1) % cards.size
            pagerState.animateScrollToPage(next)
        }
    }

    // 6. cards 大小变化时平滑修正 page
    LaunchedEffect(cards.size) {
        if (pagerState.currentPage >= cards.size && cards.isNotEmpty()) {
            pagerState.animateScrollToPage(cards.size - 1)
        }
    }

    Box(modifier = modifier) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // 5. userScrollEnabled 永远 true（手势不受 isPaused 影响）
            userScrollEnabled = true,
        ) { page ->
            HelloCard(
                card = cards[page],
                modifier = Modifier.fillMaxSize(),
                // 4. 点击卡只暂停/恢复轮播（不触发业务）；长按触发 onCardClick
                onCardClick = null,
                onLongClick = {
                    isPaused = !isPaused
                    onCardClick?.invoke(cards[page])
                },
            )
        }

        // 右侧垂直 indicator dots
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            cards.forEachIndexed { index, _ ->
                val isActive = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (isActive) 6.dp else 4.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isActive) Color.White
                            else Color.White.copy(alpha = 0.35f)
                        )
                )
            }
        }

        // 暂停时右上角 ⏸
        if (isPaused) {
            Text(
                text = "⏸",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// HelloCard — 统一骨架
//   CardMeta 只含 UI 纯参数（不带 domain 对象）
//   异步封面加载独立为 AsyncCoverSlot
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun HelloCard(
    card: SlideCard,
    modifier: Modifier = Modifier,
    onCardClick: ((SlideCard) -> Unit)? = null,
    onLongClick: ((SlideCard) -> Unit)? = null,
    isFallback: Boolean = false,
) {
    val meta = remember(card.cardId) { buildCardMeta(card) }

    val clickMod = when {
        isFallback -> modifier
        onLongClick != null -> modifier.pointerInput(card.cardId) {
            detectTapGestures(
                onLongPress = { onLongClick?.invoke(card) },
                onTap = { /* 短按留给 Pager 手势（暂停/恢复轮播） */ },
            )
        }
        onCardClick != null -> modifier.clickable { onCardClick?.invoke(card) }
        else -> modifier
    }

    Card(
        modifier = clickMod.clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            meta.themeColor.copy(alpha = 0.95f),
                            meta.themeColor.copy(alpha = 0.80f),
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CardCoverSlot(meta, modifier = Modifier)

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val labelIcon = meta.labelEmoji ?: meta.emoji
                        Text(text = labelIcon, fontSize = meta.labelFontSize)
                        Text(
                            text = meta.label,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = meta.labelFontSize,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Title
                    Text(
                        text = meta.title,
                        color = Color.White,
                        fontSize = meta.titleFontSize,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = meta.titleMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )

                    // Subtitle
                    if (meta.subtitle != null) {
                        Text(
                            text = meta.subtitle,
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = meta.subtitleFontSize,
                            maxLines = meta.subtitleMaxLines,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }

                    // DISCOVER 特殊：3 迷你封面横排
                    if (card.content is DiscoverContent) {
                        DiscoverMiniCovers(
                            trackIds = (card.content as DiscoverContent).trackIds,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }

                    // RADIO_STATUS 特殊：运行中进度条
                    if (card.content is RadioStatusContent) {
                        LinearProgressIndicator(
                            progress = { 0.6f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .padding(top = 6.dp),
                            color = Color.White.copy(alpha = 0.6f),
                            trackColor = Color.White.copy(alpha = 0.1f),
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// 封面槽位 — 统一处理"引擎直给 URI"和"异步 trackId 查询"两种来源
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun CardCoverSlot(meta: CardMeta, modifier: Modifier) {
    // 封面 URI 来源链：
    // 1. 引擎直给（ANCHOR.albumArtUri）→ 直接展示
    // 2. 需要异步查询（RECOMMEND/FORGOTTEN/ANNIVERSARY 的 trackId → MusicRepository）
    val trackId = meta.asyncCoverTrackId
    var asyncUri by remember(trackId) { mutableStateOf<String?>(null) }

    if (meta.coverUri == null && trackId != null) {
        val repo: MusicRepository = koinInject()
        LaunchedEffect(trackId) {
            runCatching { repo.getMusicInfoByIds(listOf(trackId)) }
                .getOrNull()?.firstOrNull()?.let { asyncUri = it.music.albumArtUri }
        }
    }

    val finalUri = meta.coverUri ?: asyncUri

    Box(
        modifier = Modifier
            .size(meta.coverSizeDp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            finalUri != null -> AsyncImage(
                model = finalUri,
                contentDescription = null,
                contentScale = meta.coverScale,
                modifier = Modifier.fillMaxSize(),
            )
            meta.emoji.isNotEmpty() -> Text(
                text = meta.emoji,
                fontSize = meta.coverSizeDp.value.sp * 0.5f,
            )
            else -> Image(
                painter = painterResource(Res.drawable.unknown),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// DISCOVER 3 迷你封面横排
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun DiscoverMiniCovers(
    trackIds: List<Long>,
    modifier: Modifier = Modifier,
) {
    val repo: MusicRepository = koinInject()
    val uriMap = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(trackIds) {
        runCatching {
            repo.getMusicInfoByIds(trackIds).forEach { info ->
                uriMap[info.music.id] = info.music.albumArtUri
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        trackIds.take(3).forEach { id ->
            val uri = uriMap[id]
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                if (uri != null) AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// CardMeta — 纯 UI 参数模型（与 domain 解耦）
// ═══════════════════════════════════════════════════════════════════

private data class CardMeta(
    val label: String,
    val labelEmoji: String?,
    val title: String,
    val subtitle: String?,
    val emoji: String,
    val themeColor: Color,
    val coverSizeDp: androidx.compose.ui.unit.Dp,
    val coverScale: ContentScale,
    val titleMaxLines: Int,
    val subtitleMaxLines: Int,
    val labelFontSize: androidx.compose.ui.unit.TextUnit,
    val titleFontSize: androidx.compose.ui.unit.TextUnit,
    val subtitleFontSize: androidx.compose.ui.unit.TextUnit,
    /** 引擎直给的封面 URI（ANCHOR 有，其他卡型引擎没传）*/
    val coverUri: String?,
    /** 需要异步查询的 trackId（RECOMMEND/FORGOTTEN/ANNIVERSARY 有，其他 null）*/
    val asyncCoverTrackId: Long?,
)

private fun buildCardMeta(card: SlideCard): CardMeta {
    val c = card.content
    return when (c) {
        is AnchorContent -> CardMeta(
            label = "正在听", labelEmoji = phaseEmoji(c.phase),
            title = c.trackTitle ?: "未播放", subtitle = c.artistName ?: "点击开始播放",
            emoji = "🎧", themeColor = Color(0xFF3D5AFE),
            coverSizeDp = 80.dp, coverScale = ContentScale.Crop,
            titleMaxLines = 2, subtitleMaxLines = 2,
            labelFontSize = 12.sp, titleFontSize = 18.sp, subtitleFontSize = 13.sp,
            coverUri = c.albumArtUri, asyncCoverTrackId = null,
        )
        is RadioStatusContent -> CardMeta(
            label = "电台运行中", labelEmoji = null,
            title = "${c.targetCount} 首备选",
            subtitle = if (!c.nextTrackName.isNullOrEmpty()) "下首：${c.nextTrackName}" else "AI 正在挑选...",
            emoji = "📻", themeColor = Color(0xFF00C853),
            coverSizeDp = 64.dp, coverScale = ContentScale.Fit,
            titleMaxLines = 2, subtitleMaxLines = 2,
            labelFontSize = 12.sp, titleFontSize = 18.sp, subtitleFontSize = 13.sp,
            coverUri = null, asyncCoverTrackId = null,
        )
        is GreetingContent -> CardMeta(
            label = "嗨～", labelEmoji = stableGreetingEmoji(card.cardId),
            title = c.text,
            subtitle = null,
            emoji = "🎙️", themeColor = Color(0xFFFFB300),
            coverSizeDp = 64.dp, coverScale = ContentScale.Fit,
            titleMaxLines = 2, subtitleMaxLines = 1,
            labelFontSize = 12.sp, titleFontSize = 18.sp, subtitleFontSize = 13.sp,
            coverUri = null, asyncCoverTrackId = null,
        )
        is RecommendContent -> CardMeta(
            label = "为你推荐", labelEmoji = phaseEmoji(c.currentPhase),
            title = c.trackTitle, subtitle = c.reason,
            emoji = "✨", themeColor = Color(0xFF7C4DFF),
            coverSizeDp = 80.dp, coverScale = ContentScale.Crop,
            titleMaxLines = 2, subtitleMaxLines = 2,
            labelFontSize = 12.sp, titleFontSize = 18.sp, subtitleFontSize = 13.sp,
            coverUri = null, asyncCoverTrackId = c.trackId,
        )
        is DiscoverContent -> CardMeta(
            label = "探索 ${c.target}", labelEmoji = "🔍",
            title = c.reason, subtitle = null,
            emoji = "🔍", themeColor = Color(0xFF00ACC1),
            coverSizeDp = 64.dp, coverScale = ContentScale.Fit,
            titleMaxLines = 2, subtitleMaxLines = 1,
            labelFontSize = 12.sp, titleFontSize = 16.sp, subtitleFontSize = 12.sp,
            coverUri = null, asyncCoverTrackId = null,
        )
        is ForgottenContent -> CardMeta(
            label = "${c.daysSince} 天没听了", labelEmoji = "🔙",
            title = c.trackTitle, subtitle = "听过 ${c.playCount} 次",
            emoji = "🔙", themeColor = Color(0xFF78909C),
            coverSizeDp = 80.dp, coverScale = ContentScale.Crop,
            titleMaxLines = 2, subtitleMaxLines = 1,
            labelFontSize = 12.sp, titleFontSize = 18.sp, subtitleFontSize = 13.sp,
            coverUri = null, asyncCoverTrackId = c.trackId,
        )
        is AnniversaryContent -> CardMeta(
            label = "${c.yearsAgo} 年前的今天", labelEmoji = "⭐",
            title = c.trackTitle, subtitle = "共听 ${c.totalPlays} 次",
            emoji = "⭐", themeColor = Color(0xFFFFA000),
            coverSizeDp = 80.dp, coverScale = ContentScale.Crop,
            titleMaxLines = 2, subtitleMaxLines = 1,
            labelFontSize = 12.sp, titleFontSize = 18.sp, subtitleFontSize = 13.sp,
            coverUri = null, asyncCoverTrackId = c.trackId,
        )
    }
}

private fun phaseEmoji(phase: TimePhase?): String = when (phase) {
    TimePhase.NIGHT -> "🌙"
    TimePhase.MORNING_COMMUTE -> "🌅"
    TimePhase.WORK -> "💼"
    TimePhase.LUNCH -> "🍱"
    TimePhase.EVENING_COMMUTE -> "🚗"
    TimePhase.EVENING_LEISURE -> "🎵"
    TimePhase.UNKNOWN, null -> "🎧"
}

private fun stableGreetingEmoji(cardId: String): String {
    val list = listOf("🎙️", "✨", "👋", "💫")
    val hash = cardId.hashCode().let { if (it < 0) -it else it }
    return list[hash % list.size]
}

// ═══════════════════════════════════════════════════════════════════
// Fallback 卡池为空时的兜底卡（保持 ANCHOR 中性风格）
// ═══════════════════════════════════════════════════════════════════

private fun HelloFallbackCard() = SlideCard(
    cardId = "fallback",
    type = SlideType.ANCHOR,
    content = AnchorContent(null, null, null, null, null),
)
