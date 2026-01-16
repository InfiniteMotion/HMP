package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.viewmodel.PaletteColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * 智能颜色调整函数
 * 根据主题调整颜色的亮度和饱和度
 * - 暗色主题：降低亮度，保持饱和度
 * - 亮色主题：提高亮度，降低饱和度
 */
@Composable
fun adjustColorForTheme(color: Color, isDarkTheme: Boolean): Color {
    // 将Color转换为HSL值
    val hsl = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsl)
    
    if (isDarkTheme) {
        // 暗色主题：降低亮度，保持饱和度
        hsl[2] = max(0.1f, hsl[2] * 0.7f)
    } else {
        // 亮色主题：提高亮度，降低饱和度
        hsl[2] = min(0.9f, hsl[2] * 1.3f)
        hsl[1] = max(0.2f, hsl[1] * 0.8f)
    }
    
    // 将HSL转换回Color
    return Color(android.graphics.Color.HSVToColor(hsl))
}

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
        // 动态背景动画参数（优化为5个核心图层）
        val transition = rememberInfiniteTransition(label = "dynamicBackground")
        
        // 定义非线性缓动参数
        val mainEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
        val backgroundEasing = CubicBezierEasing(0.6f, 0f, 0.4f, 1f)
        
        // 第一层流动参数（慢速旋转 - 主色调）
        val rotation1 by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 40000, easing = backgroundEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation1"
        )
        
        // 第二层流动参数（中速反向旋转 - 次色调）
        val rotation2 by transition.animateFloat(
            initialValue = 0f,
            targetValue = -360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 30000, easing = mainEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation2"
        )
        
        // 第三层流动参数（快速旋转 - 混合色）
        val rotation3 by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 20000, easing = mainEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation3"
        )
        
        // 缩放动画（平滑缩放，解决跳变）
        val scale by transition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 25000, easing = backgroundEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        // 偏移动画（更平缓的偏移，减少脉冲感）
        val offset by transition.animateFloat(
            initialValue = -0.1f,
            targetValue = 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 22000, easing = backgroundEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset"
        )

        // 智能调整颜色，适配不同主题
        val adjustedDominant = adjustColorForTheme(paletteColors.dominantColor, isDarkTheme)
        val adjustedVibrant = adjustColorForTheme(paletteColors.vibrantColor, isDarkTheme)
        val adjustedAccent = adjustColorForTheme(paletteColors.accentColor, isDarkTheme)
        val adjustedPrimary = adjustColorForTheme(paletteColors.primaryColor, isDarkTheme)

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

        // 多层流动渐变叠加（优化为5个核心图层）
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val centerX = widthPx / 2
            val centerY = heightPx / 2
            val radius = maxOf(widthPx, heightPx) * 1.2f
            
            // 核心图层1：主色调流动渐变（大范围旋转）
            val layer1Alpha1 = if (isDarkTheme) 0.7f else 0.4f
            val layer1Alpha2 = if (isDarkTheme) 0.4f else 0.2f
            Canvas(modifier = Modifier.matchParentSize()) {
                val angle1 = rotation1 * PI / 180f
                val offset1X = centerX + cos(angle1).toFloat() * radius * 0.5f
                val offset1Y = centerY + sin(angle1).toFloat() * radius * 0.5f
                
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            adjustedDominant.copy(alpha = layer1Alpha1),
                            adjustedDominant.copy(alpha = layer1Alpha2),
                            Transparent
                        ),
                        center = Offset(offset1X, offset1Y),
                        radius = radius
                    )
                )
            }
            
            // 核心图层2：次色调流动渐变（反向+偏移）
            val layer2Alpha1 = if (isDarkTheme) 0.6f else 0.35f
            val layer2Alpha2 = if (isDarkTheme) 0.3f else 0.15f
            Canvas(modifier = Modifier.matchParentSize()) {
                val angle2 = rotation2 * PI / 180f
                val offset2X = centerX + cos(angle2 + offset).toFloat() * radius * 0.6f
                val offset2Y = centerY + sin(angle2 + offset).toFloat() * radius * 0.6f
                
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            adjustedVibrant.copy(alpha = layer2Alpha1),
                            adjustedVibrant.copy(alpha = layer2Alpha2),
                            Transparent
                        ),
                        center = Offset(offset2X, offset2Y),
                        radius = radius * 1.1f
                    )
                )
            }
            
            // 核心图层3：混合色流动渐变（快速旋转）
            val layer3Alpha1 = if (isDarkTheme) 0.5f else 0.3f
            val layer3Alpha2 = if (isDarkTheme) 0.2f else 0.1f
            Canvas(modifier = Modifier.matchParentSize()) {
                val angle3 = rotation3 * PI / 180f
                val offset3X = centerX + cos(angle3 + PI / 2).toFloat() * radius * 0.4f
                val offset3Y = centerY + sin(angle3 + PI / 2).toFloat() * radius * 0.4f
                
                val mixedColor = Color(
                    red = (adjustedDominant.red + adjustedVibrant.red) / 2,
                    green = (adjustedDominant.green + adjustedVibrant.green) / 2,
                    blue = (adjustedDominant.blue + adjustedVibrant.blue) / 2
                )
                
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            mixedColor.copy(alpha = layer3Alpha1),
                            mixedColor.copy(alpha = layer3Alpha2),
                            Transparent
                        ),
                        center = Offset(offset3X, offset3Y),
                        radius = radius * 0.8f
                    )
                )
            }
            
            // 核心图层4：辅助色垂直流动（慢速偏移）
            val layer4Alpha1 = if (isDarkTheme) 0.4f else 0.25f
            val layer4Alpha2 = if (isDarkTheme) 0.1f else 0.08f
            Canvas(modifier = Modifier.matchParentSize()) {
                val angle4 = rotation1 * PI / 180f
                val offset4X = centerX + offset * widthPx * 0.5f
                val offset4Y = centerY + cos(angle4).toFloat() * heightPx * 0.5f
                
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            adjustedAccent.copy(alpha = layer4Alpha1),
                            adjustedAccent.copy(alpha = layer4Alpha2),
                            Transparent
                        ),
                        center = Offset(offset4X, offset4Y),
                        radius = radius * 0.6f
                    )
                )
            }
            
            // 核心图层5：整体氛围与光晕效果（优化合并）
            Box(modifier = Modifier.matchParentSize()) {
                // 整体氛围渐变
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    adjustedDominant.copy(alpha = 0.3f),
                                    Transparent,
                                    adjustedVibrant.copy(alpha = 0.2f)
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
                                adjustedPrimary.copy(alpha = glowAlpha1),
                                Transparent
                            ),
                            center = Offset(glowOffsetX, glowOffsetY),
                            radius = maxOf(widthPx, heightPx) * 0.6f
                        )
                    )
                }
            }
        }
    }
}
