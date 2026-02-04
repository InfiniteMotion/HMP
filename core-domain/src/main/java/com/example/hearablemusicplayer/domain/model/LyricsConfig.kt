package com.example.hearablemusicplayer.domain.model



/**
 * 歌词配置数据模型
 * 包含用户可自定义的歌词显示参数
 */
data class LyricsConfig(
    // 文本大小配置
    val originalTextSize: Int = 14,
    val translatedTextSize: Int = 14,
    val currentTimeTextSize: Int = 16,
    
    // 间距配置
    val lineSpacing: Int = 6,
    
    // 显示模式配置
    val displayMode: DisplayMode = DisplayMode.DUAL
)

/**
 * 显示模式枚举
 */
enum class DisplayMode {
    LANG1,  // 只显示语言一
    LANG2,  // 只显示语言二
    DUAL    // 双语显示
}