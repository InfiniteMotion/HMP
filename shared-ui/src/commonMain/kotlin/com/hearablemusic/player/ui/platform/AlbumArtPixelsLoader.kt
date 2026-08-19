package com.hearablemusic.player.ui.platform

/**
 * 封面像素加载接口（第 4 步批 B 随 ThemeViewModel 拆分引入）。
 *
 * 取色分析需要封面缩略图的原始像素；图片解码属平台基础设施，
 * 故按 PlaybackController 同款模式：commonMain 冻结接口 + 各平台实现。
 * - Android：Coil3 解码为 Software Bitmap 后整块读像素
 * - Desktop（第 5 步）：适配对应图片解码栈
 */
interface AlbumArtPixelsLoader {

    /** 加载封面缩略图像素（供取色分析），失败/无封面返回 null。 */
    suspend fun loadPixels(albumArtUri: String): IntArray?
}
