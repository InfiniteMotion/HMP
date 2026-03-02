package com.example.hearablemusicplayer.ui.pages.base

import android.annotation.SuppressLint
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.viewmodel.PaletteColors
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 动态背景风格枚举
 */
enum class BackgroundStyle {
    FLUID,  // 流体极光 (推荐)
    SPOTS,  // 沉浸光斑 (经典)
    BLUR    // 复古模糊 (简约)
}

/**
 * 辅助函数：色相偏移
 * @param amount 偏移角度 (0-360)
 */
fun Color.shiftHue(amount: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[0] = (hsv[0] + amount + 360) % 360
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/**
 * 动态背景主入口组件
 */
@Composable
fun DynamicBackground(
    albumArtUri: String?,
    paletteColors: PaletteColors,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    style: BackgroundStyle = BackgroundStyle.FLUID // 默认为流体效果
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (style) {
            BackgroundStyle.FLUID -> FluidBackground(albumArtUri, isDarkTheme)
            BackgroundStyle.SPOTS -> SpotsBackground(paletteColors, isDarkTheme)
            BackgroundStyle.BLUR -> BlurBackground(albumArtUri, isDarkTheme)
        }
        
        // 通用遮罩层：确保内容可读性
        // 暗色模式：顶部和底部渐变遮罩
        if (isDarkTheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Transparent,
                                Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        } else {
            // 亮色模式：整体极淡的白色遮罩，增加通透感
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.1f))
            )
        }
    }
}

/**
 * 1. 流体极光背景 (Fluid Aurora)
 * 基于双层图片相位漂移，模拟液体流动效果
 */
@Composable
fun FluidBackground(
    albumArtUri: String?,
    isDarkTheme: Boolean
) {
    // 外层容器必须裁切，否则模糊效果会溢出屏幕边界
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(RectangleShape)
    ) {
        val density = LocalDensity.current
        
        // 背景底色 - 亮色模式改用极淡灰，增加层次感
        val baseColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF5F5F5)
        Box(modifier = Modifier.fillMaxSize().background(baseColor))

        // 动画控制
        val transition = rememberInfiniteTransition(label = "fluidBackground")
        
        // 缓慢的流动曲线
        val flowEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        
        // 图层1动画：水平漂移 + 顺时针旋转
        val offsetX1 by transition.animateFloat(
            initialValue = -150f, // 增加位移幅度
            targetValue = 150f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 20000, easing = flowEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetX1"
        )
        val rotation1 by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 60000, easing = LinearEasing), // 60s 周期
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation1"
        )
        
        // 图层2动画：垂直漂移 + 逆时针旋转 (对冲)
        val offsetY2 by transition.animateFloat(
            initialValue = -120f, // 增加位移幅度
            targetValue = 120f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 25000, easing = flowEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetY2"
        )
        val rotation2 by transition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 90000, easing = LinearEasing), // 90s 周期
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation2"
        )
        
        // 底层旋转 (新增) - 逆时针缓慢旋转
        val baseRotation by transition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 120000, easing = LinearEasing), // 120s 极慢周期
                repeatMode = RepeatMode.Restart
            ),
            label = "baseRotation"
        )

        // 缩放呼吸
        val scale by transition.animateFloat(
            initialValue = 3.0f,
            targetValue = 3.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 30000, easing = flowEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        // 图片透明度控制 - 亮色模式提升透明度，防止太淡
        val imageAlpha = if (isDarkTheme) 0.6f else 0.4f
        
        // 增加饱和度和对比度的矩阵
        val colorMatrix = ColorMatrix().apply {
            setToSaturation(1.6f) // 提升60%饱和度，去灰
            
            // 亮色模式额外增加对比度
            if (!isDarkTheme) {
                val contrast = 1.2f
                val translate = (-.5f * contrast + .5f) * 255f
                this.timesAssign(
                    ColorMatrix(
                        floatArrayOf(
                            contrast, 0f, 0f, 0f, translate,
                            0f, contrast, 0f, 0f, translate,
                            0f, 0f, contrast, 0f, translate,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }
        }
        val colorFilter = ColorFilter.colorMatrix(colorMatrix)

        // 使用 Crossfade 实现淡入淡出切换
        Crossfade(
            targetState = albumArtUri,
            animationSpec = tween(durationMillis = 1000), // 1秒淡入淡出
            label = "albumArtCrossfade"
        ) { currentAlbumArtUri ->
            Box(modifier = Modifier.fillMaxSize()) {
                // 图层1 - 氛围层 (底层)
                Image(
                    painter = rememberAsyncImagePainter(
                        model = currentAlbumArtUri,
                        placeholder = painterResource(R.drawable.unknown)
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .offset { IntOffset(offsetX1.roundToInt(), 0) }
                        .graphicsLayer { 
                            rotationZ = baseRotation // 底层独立旋转
                            alpha = imageAlpha
                        }
                        .blur(40.dp) // 大幅降低模糊，保留轮廓
                )

                // 图层2 (叠加层) - 流动层 (顶层)
                Image(
                    painter = rememberAsyncImagePainter(
                        model = currentAlbumArtUri,
                        placeholder = painterResource(R.drawable.unknown)
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale * 1.1f)
                        .offset { IntOffset(0, offsetY2.roundToInt()) }
                        .graphicsLayer { 
                            rotationZ = rotation1 // 顶层顺时针
                            alpha = imageAlpha * 0.7f
                        }
                        .blur(25.dp) // 极低模糊，清晰的流动纹理
                )
            }
        }
    }
}

/**
 * 2. 沉浸光斑背景 (Immersion Spots)
 * 原有的光斑实现，适合极简风格
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SpotsBackground(
    paletteColors: PaletteColors,
    isDarkTheme: Boolean
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        
        // 背景底色
        val baseBackgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFFFFFFF)
        Box(modifier = Modifier.fillMaxSize().background(baseBackgroundColor))

        // 颜色提取
        val rawPrimary = if (isDarkTheme) paletteColors.dominantColor else paletteColors.lightVibrantColor
        val rawSecondary = if (isDarkTheme) paletteColors.darkVibrantColor else paletteColors.vibrantColor
        val rawTertiary = if (isDarkTheme) paletteColors.darkMutedColor else paletteColors.lightMutedColor
        
        // 强制差异化
        val primaryColor = rawPrimary
        val secondaryColor = if (rawSecondary == rawPrimary) rawPrimary.shiftHue(60f) else rawSecondary
        val tertiaryColor = if (rawTertiary == rawPrimary || rawTertiary == rawSecondary) secondaryColor.shiftHue(120f) else rawTertiary
        val quaternaryColor = primaryColor.shiftHue(180f)
        
        // 透明度
        val alphaPrimary = if (isDarkTheme) 0.40f else 0.30f
        val alphaSecondary = if (isDarkTheme) 0.35f else 0.25f
        val alphaTertiary = if (isDarkTheme) 0.30f else 0.20f
        val alphaQuaternary = if (isDarkTheme) 0.25f else 0.15f

        // 动画
        val transition = rememberInfiniteTransition(label = "spotsBackground")
        val breathingEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        
        val rotation1 by transition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(60000, easing = breathingEasing), RepeatMode.Restart)
        )
        val rotation2 by transition.animateFloat(
            initialValue = 360f, targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(45000, easing = breathingEasing), RepeatMode.Restart)
        )
        val scale by transition.animateFloat(
            initialValue = 1.0f, targetValue = 1.3f,
            animationSpec = infiniteRepeatable(tween(20000, easing = breathingEasing), RepeatMode.Reverse)
        )

        // 绘制
        Canvas(modifier = Modifier.fillMaxSize()) {
            val widthPx = size.width
            val heightPx = size.height
            val maxDim = max(widthPx, heightPx)
            val minDim = min(widthPx, heightPx)
            val centerX = widthPx / 2
            val centerY = heightPx / 2
            
            // 光斑 1
            val angle1 = rotation1 * (PI / 180f)
            val offset1X = centerX + cos(angle1).toFloat() * (minDim * 0.35f)
            val offset1Y = centerY + sin(angle1).toFloat() * (minDim * 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = alphaPrimary), primaryColor.copy(alpha = alphaPrimary * 0.6f), Transparent),
                    center = Offset(offset1X, offset1Y), radius = maxDim * 1.0f * scale
                ),
                center = Offset(offset1X, offset1Y), radius = maxDim * 1.0f * scale
            )

            // 光斑 2
            val angle2 = rotation2 * (PI / 180f)
            val offset2X = centerX + cos(angle2).toFloat() * (minDim * 0.45f)
            val offset2Y = centerY + sin(angle2).toFloat() * (minDim * 0.45f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryColor.copy(alpha = alphaSecondary), secondaryColor.copy(alpha = alphaSecondary * 0.6f), Transparent),
                    center = Offset(offset2X, offset2Y), radius = maxDim * 0.9f * scale
                ),
                center = Offset(offset2X, offset2Y), radius = maxDim * 0.9f * scale
            )

            // 光斑 3
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tertiaryColor.copy(alpha = alphaTertiary), Transparent),
                    center = Offset(centerX, centerY), radius = maxDim * 1.1f * (2.3f - scale)
                ),
                center = Offset(centerX, centerY), radius = maxDim * 1.1f
            )

            // 光斑 4
            val angle4 = rotation1 * 1.5f * (PI / 180f)
            val offset4X = centerX + cos(angle4 + PI).toFloat() * (minDim * 0.5f)
            val offset4Y = centerY + sin(angle4 + PI).toFloat() * (minDim * 0.5f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(quaternaryColor.copy(alpha = alphaQuaternary), quaternaryColor.copy(alpha = alphaQuaternary * 0.5f), Transparent),
                    center = Offset(offset4X, offset4Y), radius = maxDim * 0.6f * scale
                ),
                center = Offset(offset4X, offset4Y), radius = maxDim * 0.6f * scale
            )
        }
    }
}

/**
 * 3. 复古模糊背景 (Retro Blur)
 * 极简的模糊放大效果，最省电
 */
@Composable
fun BlurBackground(
    albumArtUri: String?,
    isDarkTheme: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val baseColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFFFFFFF)
        Box(modifier = Modifier.fillMaxSize().background(baseColor))

        val transition = rememberInfiniteTransition(label = "blurBackground")
        val scale by transition.animateFloat(
            initialValue = 1.2f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 30000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        val imageAlpha = if (isDarkTheme) 0.4f else 0.3f

        Image(
            painter = rememberAsyncImagePainter(
                model = albumArtUri,
                placeholder = painterResource(R.drawable.unknown)
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .graphicsLayer { alpha = imageAlpha }
                .blur(50.dp) // 适度模糊，保留更多轮廓
        )
    }
}
