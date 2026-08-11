package com.hearablemusic.player.ui.common.util

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import org.koin.androidx.compose.koinViewModel

/**
 * 获取绑定到 Activity 作用域的共享 ViewModel。
 *
 * Navigation3 的 entry 通过 rememberViewModelStoreNavEntryDecorator() 提供
 * entry 级 ViewModelStoreOwner；跨页面共享的 ViewModel（播放状态、弹窗、主题）
 * 必须显式绑定 Activity，否则会在每个 entry 产生独立实例导致状态分裂。
 */
@Composable
inline fun <reified T : ViewModel> activityViewModel(): T {
    val viewModelStoreOwner = LocalActivity.current as? ViewModelStoreOwner
        ?: error("activityViewModel() requires an Activity composition context")
    return koinViewModel(viewModelStoreOwner = viewModelStoreOwner)
}
