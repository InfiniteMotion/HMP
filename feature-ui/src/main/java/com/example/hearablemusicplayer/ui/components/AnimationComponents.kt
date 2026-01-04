package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import com.example.hearablemusicplayer.ui.util.AnimationConfig
import com.example.hearablemusicplayer.ui.util.AnimationDirection
import kotlinx.coroutines.launch

/**
 * 3D缩放过渡动画组件
 */
@Composable
fun Scale3DTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = AnimationConfig.TRANSITION,
        easing = AnimationConfig.EASE_IN_OUT
    ),
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(if (visible) 1f else 0.8f) }
    val opacity = remember { Animatable(if (visible) 1f else 0f) }
    val zRotation = remember { Animatable(if (visible) 0f else 5f) }

    LaunchedEffect(visible) {
        if (visible) {
            launch {
                scale.animateTo(1f, animationSpec)
            }
            launch {
                opacity.animateTo(1f, animationSpec)
            }
            launch {
                zRotation.animateTo(0f, animationSpec)
            }
        } else {
            launch {
                scale.animateTo(0.8f, animationSpec)
            }
            launch {
                opacity.animateTo(0f, animationSpec)
            }
            launch {
                zRotation.animateTo(5f, animationSpec)
            }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                rotationZ = zRotation.value
                alpha = opacity.value
            }
    ) {
        content()
    }
}

/**
 * 视差滚动组件
 */
@Composable
fun ParallaxScroll(
    factor: Float = 0.5f,
    content: @Composable () -> Unit
) {
    // 使用普通的MutableState来跟踪偏移量
    val offsetY = remember { mutableFloatStateOf(0f) }
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return Offset.Zero
            }
            
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y * factor
                if (delta != 0f) {
                    // 直接更新偏移量，确保在合理范围内
                    offsetY.value = (offsetY.value + delta).coerceIn(-1000f, 1000f)
                }
                return Offset.Zero
            }
            
            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                return Velocity.Zero
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .graphicsLayer {
                translationY = offsetY.value
            }
    ) {
        content()
    }
}

/**
 * 波纹效果组件
 */
@Composable
fun RippleEffect(
    visible: Boolean,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val rippleRadius = remember { Animatable(0f) }
    val opacity = remember { Animatable(0f) }
    
    LaunchedEffect(visible) {
        if (visible) {
            // 重置动画状态
            rippleRadius.snapTo(0f)
            opacity.snapTo(0f)
            
            // 执行波纹动画
            opacity.animateTo(1f, tween(AnimationConfig.MICRO_INTERACTION))
            rippleRadius.animateTo(1f, tween(AnimationConfig.TRANSITION))
            opacity.animateTo(0f, tween(AnimationConfig.TRANSITION))
        } else {
            // 重置状态
            rippleRadius.snapTo(0f)
            opacity.snapTo(0f)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithContent { 
                drawContent()
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width * rippleRadius.value
                clipRect {
                    drawCircle(
                        color = color.copy(alpha = opacity.value),
                        center = center,
                        radius = radius
                    )
                }
            }
    ) {
        content()
    }
}

/**
 * 交错动画列表项
 */
fun <T> LazyListScope.itemsWithStaggeredAnimation(
    items: List<T>,
    key: ((T) -> Any)? = null,
    content: @Composable (T) -> Unit
) {
    itemsIndexed(items) { index, item ->
        AnimatedVisibility(
            visible = true,
            enter = staggerEnterTransition(index),
            exit = staggerExitTransition(index)
        ) {
            Box {
                content(item)
            }
        }
    }
}

/**
 * 交错进入动画
 */
private fun staggerEnterTransition(index: Int): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = AnimationConfig.TRANSITION,
            delayMillis = index * 50,
            easing = AnimationConfig.EASE_OUT
        )
    ) + scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(
            durationMillis = AnimationConfig.TRANSITION,
            delayMillis = index * 50,
            easing = AnimationConfig.EASE_OUT
        )
    ) + expandIn(
        animationSpec = tween(
            durationMillis = AnimationConfig.TRANSITION,
            delayMillis = index * 50,
            easing = AnimationConfig.EASE_OUT
        )
    )
}

/**
 * 交错退出动画
 */
private fun staggerExitTransition(index: Int): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            durationMillis = AnimationConfig.TRANSITION,
            delayMillis = index * 50,
            easing = AnimationConfig.EASE_IN
        )
    ) + scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(
            durationMillis = AnimationConfig.TRANSITION,
            delayMillis = index * 50,
            easing = AnimationConfig.EASE_IN
        )
    ) + shrinkOut(
        animationSpec = tween(
            durationMillis = AnimationConfig.TRANSITION,
            delayMillis = index * 50,
            easing = AnimationConfig.EASE_IN
        )
    )
}

/**
 * 方向过渡动画 - 进入
 */
fun directionEnterTransition(direction: AnimationDirection): EnterTransition {
    return when (direction) {
        AnimationDirection.LEFT -> {
            fadeIn() + slideInHorizontally(initialOffsetX = { -it }) + scaleIn(initialScale = 0.95f)
        }
        AnimationDirection.RIGHT -> {
            fadeIn() + slideInHorizontally(initialOffsetX = { it }) + scaleIn(initialScale = 0.95f)
        }
        AnimationDirection.TOP -> {
            fadeIn() + slideInVertically(initialOffsetY = { -it }) + scaleIn(initialScale = 0.95f)
        }
        AnimationDirection.BOTTOM -> {
            fadeIn() + slideInVertically(initialOffsetY = { it }) + scaleIn(initialScale = 0.95f)
        }
        AnimationDirection.CENTER -> {
            fadeIn() + scaleIn(initialScale = 0.9f)
        }
    }
}

/**
 * 方向过渡动画 - 退出
 */
fun directionExitTransition(direction: AnimationDirection): ExitTransition {
    return when (direction) {
        AnimationDirection.LEFT -> {
            fadeOut() + slideOutHorizontally(targetOffsetX = { -it }) + scaleOut(targetScale = 0.95f)
        }
        AnimationDirection.RIGHT -> {
            fadeOut() + slideOutHorizontally(targetOffsetX = { it }) + scaleOut(targetScale = 0.95f)
        }
        AnimationDirection.TOP -> {
            fadeOut() + slideOutVertically(targetOffsetY = { -it }) + scaleOut(targetScale = 0.95f)
        }
        AnimationDirection.BOTTOM -> {
            fadeOut() + slideOutVertically(targetOffsetY = { it }) + scaleOut(targetScale = 0.95f)
        }
        AnimationDirection.CENTER -> {
            fadeOut() + scaleOut(targetScale = 0.9f)
        }
    }
}

/**
 * 弹性数字动画组件
 */
@Composable
fun AnimatedNumber(
    targetValue: Int,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = AnimationConfig.TRANSITION,
        easing = AnimationConfig.EASE_OUT
    ),
    formatter: (Int) -> String = { it.toString() },
    content: @Composable (String) -> Unit
) {
    val animatedValue = remember { Animatable(0f) }
    
    LaunchedEffect(targetValue) {
        animatedValue.animateTo(targetValue.toFloat(), animationSpec)
    }
    
    content(formatter(animatedValue.value.toInt()))
}

/**
 * 脉冲动画组件
 */
@Composable
fun PulseAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    scale: Float = 1.2f,
    duration: Int = AnimationConfig.BACKGROUND,
    content: @Composable () -> Unit = {}
) {
    val scaleAnim = remember { Animatable(1f) }
    
    LaunchedEffect(visible) {
        if (visible) {
            while (true) {
                scaleAnim.animateTo(scale, tween(duration))
                scaleAnim.animateTo(1f, tween(duration))
            }
        } else {
            // 停止动画时重置为初始状态
            scaleAnim.snapTo(1f)
        }
    }
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            }
    ) {
        content()
    }
}

/**
 * 滑动过渡动画组件
 */
@Composable
fun SlideTransition(
    visible: Boolean,
    direction: AnimationDirection = AnimationDirection.LEFT,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val opacity = remember { Animatable(0f) }
    
    LaunchedEffect(visible) {
        if (visible) {
            // 设置初始位置根据方向
            when (direction) {
                AnimationDirection.LEFT -> {
                    offsetX.snapTo(-100f)
                    offsetY.snapTo(0f)
                }
                AnimationDirection.RIGHT -> {
                    offsetX.snapTo(100f)
                    offsetY.snapTo(0f)
                }
                AnimationDirection.TOP -> {
                    offsetX.snapTo(0f)
                    offsetY.snapTo(-100f)
                }
                AnimationDirection.BOTTOM -> {
                    offsetX.snapTo(0f)
                    offsetY.snapTo(100f)
                }
                AnimationDirection.CENTER -> {
                    offsetX.snapTo(0f)
                    offsetY.snapTo(0f)
                }
            }
            
            // 执行进入动画
            launch {
                offsetX.animateTo(0f, tween(AnimationConfig.TRANSITION))
            }
            launch {
                offsetY.animateTo(0f, tween(AnimationConfig.TRANSITION))
            }
            launch {
                opacity.animateTo(1f, tween(AnimationConfig.TRANSITION))
            }
        } else {
            // 执行退出动画到指定方向
            launch {
                when (direction) {
                    AnimationDirection.LEFT -> offsetX.animateTo(-100f, tween(AnimationConfig.TRANSITION))
                    AnimationDirection.RIGHT -> offsetX.animateTo(100f, tween(AnimationConfig.TRANSITION))
                    else -> offsetX.animateTo(0f, tween(AnimationConfig.TRANSITION))
                }
            }
            launch {
                when (direction) {
                    AnimationDirection.TOP -> offsetY.animateTo(-100f, tween(AnimationConfig.TRANSITION))
                    AnimationDirection.BOTTOM -> offsetY.animateTo(100f, tween(AnimationConfig.TRANSITION))
                    else -> offsetY.animateTo(0f, tween(AnimationConfig.TRANSITION))
                }
            }
            launch {
                opacity.animateTo(0f, tween(AnimationConfig.TRANSITION))
            }
        }
    }
    
    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                alpha = opacity.value
            }
    ) {
        content()
    }
}
