package com.hearablemusic.player.ui.common.util

import androidx.compose.ui.graphics.ImageBitmap

/**
 * 图片字节流解码的多平台抽象（第 4 步随 SharedLabelIcon 迁入）。
 *
 * 原实现基于 android.graphics.BitmapFactory（JVM-only）；
 * Android actual 用 BitmapFactory，Desktop/iOS（第 5 步）actual 用 skiko Image.makeFromEncoded。
 */

/** PNG/JPEG 字节流 → ImageBitmap，解码失败返回 null。 */
expect fun ByteArray.decodeToImageBitmap(): ImageBitmap?
