package com.hearablemusic.player.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.hearablemusic.player.ui.common.design.theme.HearableMusicPlayerTheme
import kotlinx.serialization.Serializable

/**
 * 新 UI 共享层应用壳（方案 §7 第 1 步「让空的能跑」，第 2a 步接入 Tab 壳骨架）。
 *
 * 第 2a 步形态：单占位 entry 承载 MainShell（4 Tab 骨架 + BottomFusionBar）。
 * 第 2b 步起迁入真实导航图（Routes/NavigationGraph）与列表页。
 *
 * @param darkTheme 由 app 壳（MainActivity）按用户主题偏好计算后传入
 */
@Composable
fun AppRoot(darkTheme: Boolean) {
    HearableMusicPlayerTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            // NavDisplay 要求 backStack 非空（运行时强校验），故给单占位 entry 走通 nav3 渲染链路
            val backStack = rememberNavBackStack(ShellPlaceholder)
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                entryProvider = { key ->
                    NavEntry(key) {
                        // 第 2a 步：Tab 壳 + 首页骨架（播放控制占位，第 3 步接 PlaybackController）
                        MainShell()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** 第 1 步占位路由；第 2a 步替换为真实导航图后删除 */
@Serializable
private object ShellPlaceholder : NavKey
