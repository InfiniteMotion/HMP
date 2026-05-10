package com.hmp.desktop.ui.common.util

import androidx.compose.ui.graphics.Color
import com.hmp.desktop.ui.common.viewmodel.PaletteColors
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import java.io.File

/**
 * 从专辑封面图片中提取调色板颜色
 * 使用 Skia 进行图片解码和颜色分析
 */
object PaletteExtractor {

    /**
     * 从图片文件路径提取调色板颜色
     * @param imagePath 图片文件路径或 URI
     * @return 提取的调色板颜色，失败时返回默认颜色
     */
    fun extract(imagePath: String?): PaletteColors {
        if (imagePath.isNullOrBlank()) return PaletteColors()

        try {
            val bytes = loadImageBytes(imagePath) ?: return PaletteColors()
            val skiaImage = Image.makeFromEncoded(bytes)
            val bitmap = Bitmap()
            bitmap.allocPixels(ImageInfo.makeN32Premul(skiaImage.width, skiaImage.height, null))
            val canvas = Canvas(bitmap)
            canvas.drawImage(skiaImage, 0f, 0f)

            // 采样像素 - 每隔一定间隔采样
            val step = maxOf(1, minOf(skiaImage.width, skiaImage.height) / 32)
            val sampledPixels = mutableListOf<Int>()

            for (y in 0 until skiaImage.height step step) {
                for (x in 0 until skiaImage.width step step) {
                    val pixel = bitmap.getColor(x, y)
                    sampledPixels.add(pixel)
                }
            }

            return analyzeColors(sampledPixels.toIntArray())
        } catch (e: Exception) {
            return PaletteColors()
        }
    }

    private fun loadImageBytes(path: String): ByteArray? {
        return try {
            val file = File(path)
            if (file.exists() && file.length() > 0) {
                file.readBytes()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 分析像素数组，提取主要颜色
     * 使用简化的颜色量化算法
     */
    private fun analyzeColors(pixels: IntArray): PaletteColors {
        // 收集所有有效颜色（忽略透明像素）
        val colors = pixels
            .filter { (it ushr 24) > 0x40 } // 过滤低透明度像素
            .map { pixel ->
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                Triple(r, g, b)
            }

        if (colors.isEmpty()) return PaletteColors()

        // 按亮度和饱和度分类颜色
        val vibrant = mutableListOf<Triple<Int, Int, Int>>()
        val darkVibrant = mutableListOf<Triple<Int, Int, Int>>()
        val lightVibrant = mutableListOf<Triple<Int, Int, Int>>()
        val muted = mutableListOf<Triple<Int, Int, Int>>()
        val darkMuted = mutableListOf<Triple<Int, Int, Int>>()
        val lightMuted = mutableListOf<Triple<Int, Int, Int>>()

        for (color in colors) {
            val (r, g, b) = color
            val hsl = rgbToHsl(r, g, b)
            val h = hsl[0]
            val s = hsl[1]
            val l = hsl[2]

            when {
                s > 0.5f && l in 0.3f..0.7f -> vibrant.add(color)
                s > 0.5f && l < 0.3f -> darkVibrant.add(color)
                s > 0.5f && l > 0.7f -> lightVibrant.add(color)
                s <= 0.5f && l in 0.3f..0.7f -> muted.add(color)
                s <= 0.5f && l < 0.3f -> darkMuted.add(color)
                s <= 0.5f && l > 0.7f -> lightMuted.add(color)
            }
        }

        // 计算主导颜色（出现频率最高的颜色区域）
        val dominant = findDominantColor(colors)

        // 如果某些类别为空，使用备选方案
        val vibrantColor = vibrant.averageColor() ?: dominant
        val darkVibrantColor = darkVibrant.averageColor() ?: vibrantColor.darken(0.6f)
        val lightVibrantColor = lightVibrant.averageColor() ?: vibrantColor.lighten(0.4f)
        val mutedColor = muted.averageColor() ?: dominant.desaturate(0.5f)
        val darkMutedColor = darkMuted.averageColor() ?: mutedColor.darken(0.6f)
        val lightMutedColor = lightMuted.averageColor() ?: mutedColor.lighten(0.4f)
        val accentColor = vibrantColor.takeIf { it != dominant } ?: lightVibrantColor

        return PaletteColors(
            dominantColor = Color(dominant),
            primaryColor = Color(vibrantColor).copy(alpha = 0.8f),
            vibrantColor = Color(vibrantColor),
            darkVibrantColor = Color(darkVibrantColor),
            lightVibrantColor = Color(lightVibrantColor),
            mutedColor = Color(mutedColor),
            darkMutedColor = Color(darkMutedColor),
            lightMutedColor = Color(lightMutedColor),
            accentColor = Color(accentColor)
        )
    }

    private fun findDominantColor(colors: List<Triple<Int, Int, Int>>): Long {
        // 简单的聚类：将颜色空间划分为 8x8x8 的网格
        val buckets = mutableMapOf<Int, MutableList<Triple<Int, Int, Int>>>()

        for (color in colors) {
            val (r, g, b) = color
            val bucketKey = ((r / 32) shl 16) or ((g / 32) shl 8) or (b / 32)
            buckets.getOrPut(bucketKey) { mutableListOf() }.add(color)
        }

        // 找到最大的桶
        val dominantBucket = buckets.values.maxByOrNull { it.size } ?: return 0xFF121212

        // 返回该桶中颜色的平均值
        val avg = dominantBucket.averageColor() ?: return 0xFF121212
        return avg
    }

    private fun List<Triple<Int, Int, Int>>.averageColor(): Long? {
        if (isEmpty()) return null
        val r = sumOf { it.first } / size
        val g = sumOf { it.second } / size
        val b = sumOf { it.third } / size
        return 0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
    }

    private fun Long.darken(factor: Float): Long {
        val r = ((this shr 16) and 0xFF)
        val g = ((this shr 8) and 0xFF)
        val b = (this and 0xFF)
        val nr = (r * factor).toInt().coerceIn(0, 255)
        val ng = (g * factor).toInt().coerceIn(0, 255)
        val nb = (b * factor).toInt().coerceIn(0, 255)
        return 0xFF000000L or (nr.toLong() shl 16) or (ng.toLong() shl 8) or nb.toLong()
    }

    private fun Long.lighten(factor: Float): Long {
        val r = ((this shr 16) and 0xFF)
        val g = ((this shr 8) and 0xFF)
        val b = (this and 0xFF)
        val nr = (r + (255 - r) * factor).toInt().coerceIn(0, 255)
        val ng = (g + (255 - g) * factor).toInt().coerceIn(0, 255)
        val nb = (b + (255 - b) * factor).toInt().coerceIn(0, 255)
        return 0xFF000000L or (nr.toLong() shl 16) or (ng.toLong() shl 8) or nb.toLong()
    }

    private fun Long.desaturate(factor: Float): Long {
        val r = ((this shr 16) and 0xFF).toFloat()
        val g = ((this shr 8) and 0xFF).toFloat()
        val b = (this and 0xFF).toFloat()
        val gray = 0.299f * r + 0.587f * g + 0.114f * b
        val nr = (r + (gray - r) * factor).toInt().coerceIn(0, 255)
        val ng = (g + (gray - g) * factor).toInt().coerceIn(0, 255)
        val nb = (b + (gray - b) * factor).toInt().coerceIn(0, 255)
        return 0xFF000000L or (nr.toLong() shl 16) or (ng.toLong() shl 8) or nb.toLong()
    }

    /**
     * RGB 转 HSL
     * @return floatArrayOf(hue 0-360, saturation 0-1, lightness 0-1)
     */
    private fun rgbToHsl(r: Int, g: Int, b: Int): FloatArray {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f

        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val l = (max + min) / 2f

        if (max == min) {
            return floatArrayOf(0f, 0f, l)
        }

        val d = max - min
        val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)

        val h = when (max) {
            rf -> ((gf - bf) / d + (if (gf < bf) 6 else 0)) / 6f
            gf -> ((bf - rf) / d + 2) / 6f
            else -> ((rf - gf) / d + 4) / 6f
        }

        return floatArrayOf(h * 360f, s, l)
    }
}
