@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package com.example.hearablemusicplayer.ui.pages

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.PlayContent
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
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
    viewModel: PlayControlViewModel,
    navController: NavController
) {
    val density = LocalDensity.current
    val dismissThreshold = with(density) { 220.dp.toPx() }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var visible by remember { mutableStateOf(false) }
    var isDismissing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()

    LaunchedEffect(Unit) {
        visible = true
        viewModel.toastEvent.collect { event ->
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
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
                .nestedScroll(remember(density, dismissThreshold) {
                    object : NestedScrollConnection {
                        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                            // 退出过程中不响应任何滚动
                            if (isDismissing) return Offset.Zero
                            
                            val isAtTop = listState.firstVisibleItemIndex == 0 && 
                                         listState.firstVisibleItemScrollOffset == 0
                            
                            // 只在列表顶部且向下拖动时拦截
                            if (isAtTop && available.y > 0) {
                                scope.launch {
                                    val newOffset = (offsetY.value + available.y).coerceAtLeast(0f)
                                    offsetY.snapTo(newOffset)
                                    // 当拖动到一定程度时给予触觉反馈
                                    if (newOffset > dismissThreshold * 0.5f && newOffset < dismissThreshold * 0.6f) {
                                        haptic.performLightClick()
                                    }
                                }
                                return Offset(0f, available.y)
                            }
                            return Offset.Zero
                        }

                        override suspend fun onPreFling(available: Velocity): Velocity {
                            // 退出过程中不响应fling
                            if (isDismissing) return Velocity.Zero
                            
                            val currentOffset = offsetY.value
                            
                            // 判断是否达到退出阈值
                            if (currentOffset > dismissThreshold) {
                                // 执行退出流程
                                haptic.performGestureEnd()
                                isDismissing = true
                                visible = false
                                offsetY.animateTo(
                                    targetValue = with(density) { 1000.dp.toPx() },
                                    animationSpec = tween(durationMillis = 300)
                                )
                                navController.popBackStack()
                                return available
                            } else if (currentOffset > 0f) {
                                // 未达到阈值，执行回弹
                                haptic.performLightClick()
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                return Velocity.Zero
                            }
                            
                            return Velocity.Zero
                        }
                    }
                })
        ) {
            PlayContent(
                listState,
                viewModel,
                navController
            )
        }
    }
}


