package com.hearablemusic.player.ui.common.util

import androidx.compose.runtime.Composable
import com.hearablemusic.player.ui.platform.HapticService

/**
 * commonMain 组件触觉入口。
 *
 * 实现方式说明（对方案 C5 的偏离记录）：触觉依赖窗口 View（平台 UI 基础设施，
 * 非无状态纯逻辑），故此处用 expect/actual composable 而非 Koin——
 * Android actual 包装 LocalView.performHapticFeedback，Desktop（第 5 步）actual 给空实现。
 * 冻结的 PlatformServices.haptic 接口不变，供非 Compose 上下文使用。
 *
 * 与旧 androidMain 的 rememberHapticFeedback()（View 直调）并存：
 * 迁移中的 commonMain 组件用本函数，全量迁移后（第 6 步收尾）旧实现删除。
 */
@Composable
expect fun rememberPlatformHaptics(): HapticService
