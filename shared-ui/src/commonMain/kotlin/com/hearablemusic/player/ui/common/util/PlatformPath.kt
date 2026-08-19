package com.hearablemusic.player.ui.common.util

/**
 * 文件路径工具的多平台抽象（第 4 步随 LibraryViewModel 迁入）。
 *
 * 原实现基于 java.io.File（JVM-only）；Android actual 沿用 File.parent 语义
 * （同时处理 '/' 与 '\' 分隔符），Desktop/iOS（第 5 步）actual 用各自路径 API。
 */
expect fun fileParentOf(path: String): String?
