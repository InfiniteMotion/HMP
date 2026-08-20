package com.hearablemusic.player.ui.common.util

/**
 * 文件路径工具的多平台抽象。
 *
 * Android actual 沿用 File.parent 语义（同时处理 '/' 与 '\' 分隔符），
 * Desktop/iOS actual 用各自路径 API。
 */
expect fun fileParentOf(path: String): String?
