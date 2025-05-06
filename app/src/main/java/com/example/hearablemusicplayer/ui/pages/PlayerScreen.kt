package com.example.hearablemusicplayer.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.PlayContent
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel
import kotlinx.coroutines.launch

// 格式化时间为 mm:ss
fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

// 播放器主界面
@Composable
fun PlayerScreen(
    musicViewModel: MusicViewModel,
    viewModel: PlayControlViewModel,
    navController: NavController
) {
    val density = LocalDensity.current
    val dismissThreshold = with(density) { 220.dp.toPx() }

    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut()
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.value.toInt()) }
                .graphicsLayer {
                    alpha = 1f - (offsetY.value / (2 * dismissThreshold)).coerceIn(0f, 1f)
                }
                .nestedScroll(remember {
                    object : NestedScrollConnection {
                        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                            val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                            if (available.y > 0 && isAtTop) {
                                // 向下拖且列表在顶部
                                scope.launch {
                                    offsetY.snapTo(offsetY.value + available.y)
                                }
                                return Offset(0f, available.y)
                            }
                            return Offset.Zero
                        }

                        override suspend fun onPreFling(available: Velocity): Velocity {
                            if (offsetY.value > dismissThreshold) {
                                offsetY.animateTo(with(density) { 1000.dp.toPx() }, tween(300))
                                navController.popBackStack()
                                return available
                            } else {
                                offsetY.animateTo(0f, spring())
                                return Velocity.Zero
                            }
                        }
                    }
                })
        ) {
            PlayContent(
                listState,
                musicViewModel,
                viewModel,
                navController
            )
        }
    }
}


