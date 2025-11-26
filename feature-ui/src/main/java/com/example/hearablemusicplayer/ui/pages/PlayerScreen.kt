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
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.PlayContent
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback

import kotlinx.coroutines.launch

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

    // 预加载当前播放音乐信息
    LaunchedEffect(Unit) {
        viewModel.preloadCurrentMusicInfo()
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
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            // 开始拖动
                        },
                        onDragEnd = {
                            scope.launch {
                                val currentOffset = offsetY.value
                                
                                // 判断是否达到退出阈值
                                if (currentOffset > dismissThreshold) {
                                    // 执行退出流程
                                    haptic.performGestureEnd()
                                    offsetY.animateTo(
                                        targetValue = with(density) { 1000.dp.toPx() },
                                        animationSpec = tween(durationMillis = 300)
                                    )
                                    navController.popBackStack()
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
                                }
                            }
                        },
                        onDragCancel = {
                            // 取消拖动，回弹
                            scope.launch {
                                if (offsetY.value > 0f) {
                                    haptic.performLightClick()
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            if (!isDismissing && dragAmount > 0) {
                                scope.launch {
                                    val newOffset = (offsetY.value + dragAmount).coerceAtLeast(0f)
                                    offsetY.snapTo(newOffset)
                                    // 当拖动到一定程度时给予触觉反馈
                                    if (newOffset > dismissThreshold * 0.5f && newOffset < dismissThreshold * 0.6f) {
                                        haptic.performLightClick()
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            PlayContent(
                viewModel,
                navController
            )
        }
    }
}

