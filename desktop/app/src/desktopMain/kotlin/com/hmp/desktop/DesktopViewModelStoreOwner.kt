package com.hmp.desktop

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Desktop 应用级 ViewModelStoreOwner。
 *
 * Android 侧该角色由 MainActivity 承担（CompositionLocal provides this）；
 * Desktop 无 Activity 概念，主窗口生命周期 == 应用生命周期，
 * 用常驻 ViewModelStore 即可实现「应用级共享 VM」（播放状态/主题/弹窗跨页面单实例）。
 */
class DesktopViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}
