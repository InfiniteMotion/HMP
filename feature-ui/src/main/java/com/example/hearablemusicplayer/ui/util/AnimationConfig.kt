package com.example.hearablemusicplayer.ui.util

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * 动画配置类，定义统一的动画参数和缓动函数
 */
object AnimationConfig {
    // 持续时间配置（毫秒）
    const val MICRO_INTERACTION = 200 // 微交互动画
    const val TRANSITION = 400 // 过渡动画
    const val COMPLEX = 650 // 复杂动画
    const val BACKGROUND = 3000 // 背景动画
    
    // 缓动函数配置
    val EASE_IN_OUT = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f) // 现代缓入缓出
    val EASE_OUT = CubicBezierEasing(0.2f, 0.0f, 0.1f, 1.0f) // 快速缓出
    val EASE_IN = CubicBezierEasing(0.6f, 0.0f, 0.8f, 1.0f) // 快速缓入
    
    // 弹性动画配置
    val SPRING_MEDIUM = spring<Float>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioMediumBouncy
    ) // 中等弹性
    
    val SPRING_BOUNCY = spring<Float>(
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioHighBouncy
    ) // 高弹性
    
    val SPRING_GENTLE = spring<Float>(
        stiffness = Spring.StiffnessLow,
        dampingRatio = Spring.DampingRatioHighBouncy
    ) // 轻微弹性
}

/**
 * 动画方向枚举
 */
enum class AnimationDirection {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    CENTER
}
