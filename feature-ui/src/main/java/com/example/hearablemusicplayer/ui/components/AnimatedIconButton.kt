package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.ui.util.AnimationConfig
import kotlinx.coroutines.launch

/**
 * 带点击反馈动画的IconButton组件
 */
@Composable
fun AnimatedIconButton(
    onClick: () -> Unit,
    iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: Modifier = Modifier.size(24.dp),
    buttonSize: Modifier = Modifier.size(32.dp),
    rippleColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    // 缩放动画
    val scale = remember { Animatable(1f) }
    
    // 波纹动画
    val rippleRadius = remember { Animatable(0f) }
    val rippleOpacity = remember { Animatable(0f) }
    
    // 处理点击事件
    val handleClick: () -> Unit = {
        // 触发触觉反馈
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        
        // 执行点击操作
        onClick()
        
        // 触发缩放动画
        scope.launch {
            scale.animateTo(0.9f, tween(AnimationConfig.MICRO_INTERACTION))
            scale.animateTo(1f, tween(AnimationConfig.MICRO_INTERACTION))
        }
        
        // 触发波纹动画
        scope.launch {
            rippleOpacity.animateTo(1f, tween(AnimationConfig.MICRO_INTERACTION))
            rippleRadius.animateTo(1f, tween(AnimationConfig.TRANSITION))
            rippleOpacity.animateTo(0f, tween(AnimationConfig.TRANSITION))
            rippleRadius.animateTo(0f, tween(0))
        }
    }
    
    Box(
        modifier = buttonSize
            .graphicsLayer {
                this.scaleX = scale.value
                this.scaleY = scale.value
            }
            .drawWithContent {
                // 绘制波纹效果
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width * rippleRadius.value
                drawCircle(
                    color = rippleColor.copy(alpha = rippleOpacity.value),
                    center = center,
                    radius = radius
                )
                
                // 绘制内容
                drawContent()
            }
            .clickable(
                onClick = handleClick,
                interactionSource = interactionSource,
                indication = null
            )
    ) {
        Icon(
            painter = painterResource(iconRes),
            tint = tint,
            contentDescription = contentDescription,
            modifier = iconSize
        )
    }
}

/**
 * 带点击反馈动画的FloatingActionButton组件
 */
@Composable
fun AnimatedFloatingActionButton(
    onClick: () -> Unit,
    iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    iconSize: Modifier = Modifier.size(24.dp),
    buttonSize: Modifier = Modifier.size(48.dp)
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    // 缩放动画
    val scale = remember { Animatable(1f) }
    
    // 处理点击事件
    val handleClick: () -> Unit = {
        // 触发触觉反馈
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        
        // 执行点击操作
        onClick()
        
        // 触发缩放动画
        scope.launch {
            scale.animateTo(0.9f, tween(AnimationConfig.MICRO_INTERACTION))
            scale.animateTo(1f, tween(AnimationConfig.MICRO_INTERACTION))
        }
    }
    
    Box(
        modifier = buttonSize
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.extraLarge
            )
            .graphicsLayer {
                this.scaleX = scale.value
                this.scaleY = scale.value
            }
            .clickable(onClick = handleClick)
    ) {
        Icon(
            painter = painterResource(iconRes),
            tint = iconColor,
            contentDescription = contentDescription,
            modifier = iconSize
        )
    }
}
