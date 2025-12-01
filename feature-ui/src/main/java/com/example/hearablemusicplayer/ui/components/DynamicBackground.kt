package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.viewmodel.PaletteColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 动态背景组件（强化抽象化流动效果）
 * - 极度模糊的专辑封面作为底层
 * - 多层颜色混合与复杂流动动画
 * - 高度抽象化的色彩表达
 * - 支持浅色/深色主题模式
 */
@Composable
fun DynamicBackground(
    albumArtUri: String?,
    paletteColors: PaletteColors,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true

) {
    Box(modifier = modifier.fillMaxSize()) {
        // 动态背景动画参数（多层不同速度）
        val transition = rememberInfiniteTransition(label = "dynamicBackground")
        
        // 第一层流动参数（慢速旋转）
        val rotation1 by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 35000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation1"
        )
        
        // 第二层流动参数（中速旋转）
        val rotation2 by transition.animateFloat(
            initialValue = 0f,
            targetValue = -360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 25000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation2"
        )
        
        // 第三层流动参数（快速旋转）
        val rotation3 by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 15000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation3"
        )
        
        // 第四层流动参数（超慢速缩放）
        val scale by transition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 20000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        // 第四层流动参数（轻微偏移）
        val offset by transition.animateFloat(
            initialValue = -0.2f,
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 18000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset"
        )

        // 极度模糊的专辑封面作为底层（高度抽象化）
        val imageAlpha = if (isDarkTheme) 0.5f else 0.4f
        Image(
            painter = rememberAsyncImagePainter(
                model = albumArtUri,
                placeholder = painterResource(R.drawable.unknown)
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(100.dp) // 极度模糊实现高度抽象化
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation1 * 0.2f
                    alpha = imageAlpha
                },
            contentScale = ContentScale.Crop
        )

        // 多层流动渐变叠加
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val centerX = widthPx / 2
            val centerY = heightPx / 2
            val radius = maxOf(widthPx, heightPx) * 1.2f
            
            // 第一层：主色调流动渐变（大范围旋转）
            val layer1Alpha1 = if (isDarkTheme) 0.7f else 0.5f
            val layer1Alpha2 = if (isDarkTheme) 0.4f else 0.25f
            Canvas(modifier = Modifier.matchParentSize()) {
                val angle1 = rotation1 * PI / 180f
                val offset1X = centerX + cos(angle1).toFloat() * radius * 0.5f
                val offset1Y = centerY + sin(angle1).toFloat() * radius * 0.5f
                
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            paletteColors.dominantColor.copy(alpha = layer1Alpha1),
                            paletteColors.dominantColor.copy(alpha = layer1Alpha2),
                            Color.Transparent
                        ),
                        center = Offset(offset1X, offset1Y),
                        radius = radius
                    )
                )
            }
            
            // 第二层：次色调流动渐变（反向+偏移）
            val layer2Alpha1 = if (isDarkTheme) 0.6f else 0.4f
            val layer2Alpha2 = if (isDarkTheme) 0.3f else 0.2f
            Canvas(modifier = Modifier.matchParentSize()) {
                val angle2 = rotation2 * PI / 180f
                val offset2X = centerX + cos(angle2 + offset).toFloat() * radius * 0.6f
                val offset2Y = centerY + sin(angle2 + offset).toFloat() * radius * 0.6f
                
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            paletteColors.vibrantColor.copy(alpha = layer2Alpha1),
                            paletteColors.vibrantColor.copy(alpha = layer2Alpha2),
                            Color.Transparent
                        ),
                        center = Offset(offset2X, offset2Y),
                        radius = radius * 1.1f
                    )
                )
            }
            
            // 第三层：混合色流动渐变（快速旋转）
            val layer3Alpha1 = if (isDarkTheme) 0.5f else 0.35f
            val layer3Alpha2 = if (isDarkTheme) 0.2f else 0.15f
            Canvas(modifier = Modifier.matchParentSize()) {
                val angle3 = rotation3 * PI / 180f
                val offset3X = centerX + cos(angle3 + PI / 2).toFloat() * radius * 0.4f
                val offset3Y = centerY + sin(angle3 + PI / 2).toFloat() * radius * 0.4f
                
                val mixedColor = Color(
                    red = (paletteColors.dominantColor.red + paletteColors.vibrantColor.red) / 2,
                    green = (paletteColors.dominantColor.green + paletteColors.vibrantColor.green) / 2,
                    blue = (paletteColors.dominantColor.blue + paletteColors.vibrantColor.blue) / 2
                )
                
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            mixedColor.copy(alpha = layer3Alpha1),
                            mixedColor.copy(alpha = layer3Alpha2),
                            Color.Transparent
                        ),
                        center = Offset(offset3X, offset3Y),
                        radius = radius * 0.8f
                    )
                )
            }
            
            // 第四层：辅助色流动渐变（垂直流动）
            val layer4Alpha1 = if (isDarkTheme) 0.4f else 0.25f
            val layer4Alpha2 = if (isDarkTheme) 0.1f else 0.08f
            Canvas(modifier = Modifier.matchParentSize()) {
                val angle4 = rotation1 * 2 * PI / 180f
                val offset4X = centerX + offset * widthPx * 0.7f
                val offset4Y = centerY + cos(angle4).toFloat() * heightPx * 0.7f
                
                val altColor = Color(
                    red = 1 - paletteColors.dominantColor.red,
                    green = 1 - paletteColors.dominantColor.green,
                    blue = 1 - paletteColors.dominantColor.blue
                )
                
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            altColor.copy(alpha = layer4Alpha1),
                            altColor.copy(alpha = layer4Alpha2),
                            Color.Transparent
                        ),
                        center = Offset(offset4X, offset4Y),
                        radius = radius * 0.6f
                    )
                )
            }

            // 整体氛围渐变
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                paletteColors.dominantColor.copy(alpha = 0.3f),
                                Color.Transparent,
                                paletteColors.vibrantColor.copy(alpha = 0.2f)
                            )
                        )
                    )
            )

            // 顶层光晕效果（根据主题模式调整）
            val glowAlpha1 = if (isDarkTheme) 0.15f else 0.1f
            Canvas(modifier = Modifier.matchParentSize()) {
                val angleGlow = rotation2 * 0.5f * PI / 180f
                val glowOffsetX = widthPx / 2 + cos(angleGlow).toFloat() * widthPx * 0.4f
                val glowOffsetY = heightPx / 2 + sin(angleGlow).toFloat() * heightPx * 0.4f

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            paletteColors.dominantColor.copy(alpha = glowAlpha1),
                            Color.Transparent
                        ),
                        center = Offset(glowOffsetX, glowOffsetY),
                        radius = maxOf(widthPx, heightPx) * 0.6f
                    )
                )
            }
        }

        // 柔化遮罩提升对比度（根据主题模式调整）
        val scrimColor = if (isDarkTheme) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(scrimColor)
        )
    }
}
