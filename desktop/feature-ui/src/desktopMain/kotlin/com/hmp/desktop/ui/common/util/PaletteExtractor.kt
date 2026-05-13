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

    // ── 3D 颜色直方图 + 峰值检测 ─────────────────────────────────

    private data class ColorPeak(
        val r: Int, val g: Int, val b: Int,
        val weight: Float  // 占总采样像素的比例
    )

    /**
     * 分析像素数组，提取主要颜色。
     * 使用 16³ 3D 直方图 + 均值平滑 + 局部峰值检测，
     * 自适应提取封面上实际存在的颜色，不预设数量。
     */
    private fun analyzeColors(pixels: IntArray): PaletteColors {
        val total = pixels.size
        if (total == 0) return PaletteColors()

        // 1. 构建 24³ 直方图（乘除法映射保证 255→23，不会越界）
        val BINS = 24
        val hist = FloatArray(BINS * BINS * BINS)
        for (px in pixels) {
            if ((px ushr 24) <= 0x40) continue
            val r = ((px shr 16) and 0xFF) * BINS / 256
            val g = ((px shr 8) and 0xFF) * BINS / 256
            val b = (px and 0xFF) * BINS / 256
            val idx = (r * BINS * BINS) + (g * BINS) + b
            hist[idx] += 1f
        }

        // 2. 直接找局部极大值（不去平滑——24³ 本身已聚合，平滑会碾碎相邻峰）
        val rawPeaks = mutableListOf<ColorPeak>()
        for (r in 0 until BINS) {
            for (g in 0 until BINS) {
                for (b in 0 until BINS) {
                    val idx = (r * BINS * BINS) + (g * BINS) + b
                    val v = hist[idx]
                    if (v <= 0f) continue
                    var isPeak = true
                    check@ for (dr in -1..1) {
                        val nr = r + dr; if (nr !in 0 until BINS) continue
                        for (dg in -1..1) {
                            val ng = g + dg; if (ng !in 0 until BINS) continue
                            for (db in -1..1) {
                                if (dr == 0 && dg == 0 && db == 0) continue
                                val nb = b + db; if (nb !in 0 until BINS) continue
                                if (hist[(nr * BINS * BINS) + (ng * BINS) + nb] >= v) {
                                    isPeak = false; break@check
                                }
                            }
                        }
                    }
                    if (isPeak) {
                        rawPeaks.add(ColorPeak(
                            r = (r * 256 + 128) / BINS,
                            g = (g * 256 + 128) / BINS,
                            b = (b * 256 + 128) / BINS,
                            weight = v / total.toFloat()
                        ))
                    }
                }
            }
        }

        if (rawPeaks.isEmpty()) return PaletteColors()

        // 3. 按 weight 降序排列
        rawPeaks.sortByDescending { it.weight }

        // 4. 合并过近的峰（RGB 距离 < 30，仅合并肉眼不可分辨的）
        val merged = mutableListOf<ColorPeak>()
        for (candidate in rawPeaks) {
            if (merged.any { rgbDist(candidate, it) < 30f }) continue
            merged.add(candidate)
        }

        // 5. 过滤低于 0.6% 占比的峰
        val threshold = 0.006f
        val filtered = merged.filter { it.weight >= threshold }
        var detected = if (filtered.size >= 2) filtered
            else merged.take(2).ifEmpty { filtered }

        // 6. 峰不够 3 个时，用色相扩散 + 明度变异自行填充
        if (detected.size < 3) {
            val padded = detected.toMutableList()
            data class PadAnchor(val peak: ColorPeak, val sat: Float, val lgh: Float, val hue: Float)
            val anchors = detected.map { p ->
                val hsl = rgbToHsl(p.r, p.g, p.b)
                PadAnchor(p, hsl[1], hsl[2], hsl[0])
            }.sortedByDescending { it.sat }
            val minW = detected.minOf { it.weight }
            var idx = 0
            while (padded.size < 3) {
                val a = anchors[idx % anchors.size]
                val h = (a.hue + (padded.size - detected.size + 1) * (360f / (4 - detected.size))) % 360f
                val s = (a.sat * 1.4f).coerceIn(0.45f, 1f)
                val l = (a.lgh * (0.5f + (idx % 3) * 0.3f)).coerceIn(0.12f, 0.85f)
                val (r, g, b) = hslToRgb(h, s, l)
                padded.add(ColorPeak(r, g, b, minW * 0.4f))
                idx++
            }
            detected = padded
        }

        // 7. 计算每个峰的 HSL
        data class LabeledPeak(val peak: ColorPeak, val saturation: Float, val lightness: Float, val hue: Float)

        val labeled = detected.map { p ->
            val hsl = rgbToHsl(p.r, p.g, p.b)
            LabeledPeak(p, hsl[1], hsl[2], hsl[0])
        }

        fun ColorPeak.toColor() = Color(0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong())

        // 8. peaks + weights — 权重归一化（用于驱动光斑大小/数量）
        val totalW = detected.sumOf { it.weight.toDouble() }.toFloat()
        val peaks = detected.map { it.toColor() }
        val peakWeights = detected.map { (it.weight / totalW).coerceIn(0f, 1f) }

        // 9. background — 最暗柔的峰（亮度最低 + 饱和度低）
        val background = labeled
            .sortedBy { it.lightness + it.saturation * 0.3f }
            .first().peak.toColor()

        val bgLum = relativeLuminance(background)

        // 10. primary — 与 background 对比度 ≥ 3:1 的最鲜艳峰
        // 如果没有任何峰满足条件，取最鲜艳峰并向远离 background 方向增强
        val primaryCandidate = labeled
            .sortedByDescending { it.saturation }
            .firstOrNull { contrastRatio(it.peak.toColor(), bgLum) >= 3.0f }

        val primary = if (primaryCandidate != null) {
            primaryCandidate.peak.toColor()
        } else {
            // 全部太接近 background → 取最鲜艳峰，朝远离方向推进
            val best = labeled.maxByOrNull { it.saturation }!!.peak
            ensureContrast(best.toColor(), background, 3.0f)
        }

        // 11. accent — 色相离 primary 最远的峰
        val pr = (primary.red * 255f).toInt().coerceIn(0, 255)
        val pg = (primary.green * 255f).toInt().coerceIn(0, 255)
        val pb = (primary.blue * 255f).toInt().coerceIn(0, 255)
        val primaryHue = rgbToHsl(pr, pg, pb)[0]

        val accent = detected
            .map { it.toColor() }
            .maxByOrNull { hueDist(it, primaryHue) }!!
            .let { if (contrastRatio(it, bgLum) < 2.0f) primary else it }

        return PaletteColors(
            peaks = peaks,
            peakWeights = peakWeights,
            primary = primary,
            background = background,
            accent = accent
        )
    }

    private fun rgbDist(a: ColorPeak, b: ColorPeak): Float {
        val dr = a.r - b.r; val dg = a.g - b.g; val db = a.b - b.b
        return kotlin.math.sqrt((dr * dr + dg * dg + db * db).toFloat())
    }

    // ── WCAG 对比度与辅助函数 ──────────────────────────────────

    /** sRGB 单通道线性化 */
    private fun linearize(c: Float): Float {
        val s = c / 255f
        return if (s <= 0.04045f) s / 12.92f
        else Math.pow(((s + 0.055f) / 1.055f).toDouble(), 2.4).toFloat()
    }

    /** 相对亮度 (WCAG) */
    private fun relativeLuminance(r: Int, g: Int, b: Int): Float =
        0.2126f * linearize(r.toFloat()) + 0.7152f * linearize(g.toFloat()) + 0.0722f * linearize(b.toFloat())

    private fun relativeLuminance(color: Color): Float {
        val r = (color.red * 255f).toInt().coerceIn(0, 255)
        val g = (color.green * 255f).toInt().coerceIn(0, 255)
        val b = (color.blue * 255f).toInt().coerceIn(0, 255)
        return relativeLuminance(r, g, b)
    }

    /** WCAG 对比度 */
    private fun contrastRatio(lum1: Float, lum2: Float): Float {
        val lighter = maxOf(lum1, lum2)
        val darker = minOf(lum1, lum2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private fun contrastRatio(color: Color, bgLum: Float): Float =
        contrastRatio(relativeLuminance(color), bgLum)

    /** 将颜色朝远离背景方向推进直到对比度 ≥ target */
    private fun ensureContrast(color: Color, background: Color, target: Float): Color {
        val bgR = (background.red * 255f).toInt()
        val bgG = (background.green * 255f).toInt()
        val bgB = (background.blue * 255f).toInt()
        var r = (color.red * 255f).toInt()
        var g = (color.green * 255f).toInt()
        var b = (color.blue * 255f).toInt()

        for (i in 0 until 20) {
            if (contrastRatio(relativeLuminance(r, g, b), relativeLuminance(bgR, bgG, bgB)) >= target) break
            // 朝远离 background 方向移动
            r += ((r - bgR) * 0.15f).toInt().coerceIn(1, 30) * if (r > bgR) 1 else -1
            g += ((g - bgG) * 0.15f).toInt().coerceIn(1, 30) * if (g > bgG) 1 else -1
            b += ((b - bgB) * 0.15f).toInt().coerceIn(1, 30) * if (b > bgB) 1 else -1
            r = r.coerceIn(0, 255); g = g.coerceIn(0, 255); b = b.coerceIn(0, 255)
        }
        return Color(0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong())
    }

    /** 色相距离 (0-180°) */
    private fun hueDist(color: Color, refHue: Float): Float {
        val r = (color.red * 255f).toInt()
        val g = (color.green * 255f).toInt()
        val b = (color.blue * 255f).toInt()
        val h = rgbToHsl(r, g, b)[0]
        val diff = kotlin.math.abs(h - refHue)
        return if (diff > 180f) 360f - diff else diff
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
    /** HSL → RGB，返回 Triple(Int, Int, Int) */
    private fun hslToRgb(h: Float, s: Float, l: Float): Triple<Int, Int, Int> {
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f
        val (r1, g1, b1) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Triple(
            ((r1 + m) * 255f).toInt().coerceIn(0, 255),
            ((g1 + m) * 255f).toInt().coerceIn(0, 255),
            ((b1 + m) * 255f).toInt().coerceIn(0, 255))
    }

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
