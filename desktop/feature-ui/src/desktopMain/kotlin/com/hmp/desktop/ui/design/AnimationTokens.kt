package com.hmp.desktop.ui.design

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object AnimationTokens {
    const val MICRO_INTERACTION = 200
    const val TRANSITION = 400
    const val COMPLEX = 650
    const val BACKGROUND = 3000

    val EASE_IN_OUT = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val EASE_OUT = CubicBezierEasing(0.2f, 0.0f, 0.1f, 1.0f)
    val EASE_IN = CubicBezierEasing(0.6f, 0.0f, 0.8f, 1.0f)

    val SPRING_MEDIUM = spring<Float>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioMediumBouncy
    )

    val SPRING_BOUNCY = spring<Float>(
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioHighBouncy
    )

    val SPRING_GENTLE = spring<Float>(
        stiffness = Spring.StiffnessLow,
        dampingRatio = Spring.DampingRatioHighBouncy
    )
}

enum class AnimationDirection {
    LEFT, RIGHT, TOP, BOTTOM, CENTER
}
