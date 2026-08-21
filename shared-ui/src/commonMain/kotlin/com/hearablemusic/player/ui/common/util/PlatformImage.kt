package com.hearablemusic.player.ui.common.util

import androidx.compose.ui.graphics.ImageBitmap

/**
 * 图片字节流解码的多平台抽象。
 *
 * Android actual 用 BitmapFactory，Desktop/iOS actual 用 skiko Image.makeFromEncoded。
 */

/** PNG/JPEG 字节流 → ImageBitmap，解码失败返回 null。 */
expect fun ByteArray.decodeToImageBitmap(): ImageBitmap?
