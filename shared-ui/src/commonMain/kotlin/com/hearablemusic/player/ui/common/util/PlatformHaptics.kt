package com.hearablemusic.player.ui.common.util

import androidx.compose.runtime.Composable
import com.hearablemusic.player.ui.platform.HapticService

/**
 * commonMain 组件触觉入口（PlatformServices.haptic 契约版）。
 *
 * 触觉依赖窗口 View（平台 UI 基础设施，非无状态纯逻辑），故用 expect/actual composable
 * 而非 Koin 注入（对方案 C5 的偏离）：Android actual 包装 LocalView.performHapticFeedback，
 * Desktop actual 给空实现；冻结的 PlatformServices.haptic 接口不变，供非 Compose 上下文使用。
 * 组件层高频使用的手势语义入口见 HapticFeedbackHelper（同为 expect/actual composable）。
 */
@Composable
expect fun rememberPlatformHaptics(): HapticService
