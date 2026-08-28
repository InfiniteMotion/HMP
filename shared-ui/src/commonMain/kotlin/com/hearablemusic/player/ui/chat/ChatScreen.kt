package com.hearablemusic.player.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hearablemusic.player.ui.common.pages.base.SubScreen
import com.hearablemusic.player.ui.platform.PlaybackController
import com.hmp.domain.music.MusicInfo
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * M5-T1 ChatScreen —— 听歌伙伴对话页（确认卡片流宿主）。
 *
 * 布局（配合 SubScreen 基座后的内容区）：
 * - 顶部「正在听」胶囊（接 [PlaybackController]，无曲目时隐藏）
 * - 中部对话流 LazyColumn（新增消息在底部自动滚；用户上滑翻看时不打断）
 * - 确认卡（非模态）：`pendingConfirm` 未决/已提交时悬浮于输入条上方，不阻塞对话流回看
 * - 底部输入条（多平台 IME padding）
 */
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = koinViewModel(),
    navController: NavBackStack<NavKey>,
    playbackController: PlaybackController = koinInject(),
    chatEntryBroker: ChatEntryBroker = koinInject(),
) {
    val state by chatViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    // M1 锚点 → M5 对话入口：进入时消费待带话，作为首条消息发送（同 session_id 语义）
    LaunchedEffect(Unit) {
        chatEntryBroker.pendingInput.value?.let { input ->
            chatEntryBroker.pendingInput.value = null
            chatViewModel.sendPreloaded(input)
        }
    }

    ChatScreenContent(
        state = state,
        onInputChange = chatViewModel::onInputChange,
        onSend = chatViewModel::send,
        onToggleConfirm = chatViewModel::toggleConfirmItem,
        onSubmitConfirm = chatViewModel::submitConfirm,
        onSkipConfirm = chatViewModel::skipConfirm,
        onSongClick = { music -> scope.launch { playbackController.playWith(music) } },
        onSongMenu = {},
        onPlaylistPlayAll = {},
        currentPlaying = playbackController.currentPlayingMusic.collectAsState().value,
        isPlaying = playbackController.isPlaying.collectAsState().value,
        onBackClick = { navController.removeLastOrNull() },
    )
}

@Composable
private fun ChatScreenContent(
    state: ChatUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onToggleConfirm: (String) -> Unit,
    onSubmitConfirm: () -> Unit,
    onSkipConfirm: () -> Unit,
    onSongClick: (MusicInfo) -> Unit,
    onSongMenu: (MusicInfo) -> Unit,
    onPlaylistPlayAll: () -> Unit,
    currentPlaying: MusicInfo?,
    isPlaying: Boolean,
    onBackClick: () -> Unit,
) {
    val callbacks = CompanionCallbacks(
        onSongClick = onSongClick,
        onSongMenu = onSongMenu,
        onPlaylistPlayAll = onPlaylistPlayAll,
        onToggleConfirm = onToggleConfirm,
        onSubmitConfirm = onSubmitConfirm,
        onSkipConfirm = onSkipConfirm,
    )

    SubScreen(onBackClick = onBackClick, title = "听歌伙伴") {
        Column(modifier = Modifier.fillMaxSize()) {
            if (currentPlaying != null) {
                NowPlayingBar(currentPlaying, isPlaying)
            }

            ChatMessageList(
                messages = state.messages,
                running = state.running,
                runningHint = state.runningHint,
                callbacks = callbacks,
                modifier = Modifier.weight(1f),
            )

            AnimatedVisibility(
                visible = state.pendingConfirm != null,
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                state.pendingConfirm?.let { p ->
                    ConfirmMatrixCard(
                        items = p.items,
                        submitted = p.submitted,
                        receipt = "",
                        callbacks = callbacks,
                    )
                }
            }

            ChatInputBar(
                input = state.input,
                running = state.running || (state.pendingConfirm != null && !state.pendingConfirm.submitted),
                onInputChange = onInputChange,
                onSend = onSend,
            )
        }
    }
}

@Composable
private fun NowPlayingBar(music: MusicInfo, isPlaying: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(4.dp),
                ),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "正在听 ${music.music.title} · ${music.music.artist}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun ChatMessageList(
    messages: List<CompanionMessage>,
    running: Boolean,
    runningHint: String,
    callbacks: CompanionCallbacks,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var autoScroll by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= info.totalItemsCount - 2
        }.collect { atBottom -> autoScroll = atBottom }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && autoScroll) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(messages, key = { it.id }) { message ->
                CompanionBubble(message = message, callbacks = callbacks)
            }
            if (running) {
                item(key = "running-hint") {
                    RunningHint(runningHint)
                }
            }
        }
    }
}

@Composable
private fun RunningHint(hint: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChatInputBar(
    input: String,
    running: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            placeholder = { Text("想找歌、整理歌单，或看看听歌排行？") },
            shape = RoundedCornerShape(22.dp),
            singleLine = true,
            enabled = !running,
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = input.isNotBlank() && !running,
        ) {
            Text(
                text = "发送",
                style = MaterialTheme.typography.labelMedium,
                color = if (input.isNotBlank() && !running) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.widthIn(min = 40.dp),
            )
        }
    }
}