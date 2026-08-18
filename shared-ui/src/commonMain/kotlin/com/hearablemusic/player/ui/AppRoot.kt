package com.hearablemusic.player.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.hearablemusic.player.ui.common.design.theme.HearableMusicPlayerTheme
import kotlinx.serialization.Serializable

/**
 * 新 UI 共享层应用壳（方案 §7 第 1 步「让空的能跑」）。
 *
 * 第 1 步形态：单占位 entry 的 NavHost（无真实页面）+ 主题。
 * 第 2a 步起迁入 Tab 壳与首页骨架，替换为真实导航图（NavRoutes.Main.Tabs 起始）。
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
                        // 占位内容：证明新层主题（HarmonyOS Sans 字体/配色）与 nav3 渲染已生效；第 2a 步移除
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Shared UI Shell",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
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
