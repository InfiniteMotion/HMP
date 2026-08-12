package com.hearablemusic.player.ui.common.dialogs.viewmodel

import androidx.navigation3.runtime.NavKey

sealed class DialogEvent {
    data class Message(
        val message: String,
        val duration: MessageDuration = MessageDuration.Short,
        val id: Long = System.currentTimeMillis()
    ) : DialogEvent()

    data class ShowTimerDialog(
        val onConfirm: (Int) -> Unit,
        val onDismiss: () -> Unit = {}
    ) : DialogEvent()

    object DismissTimerDialog : DialogEvent()

    data class ShareMusic(
        val title: String,
        val artist: String,
        val album: String,
        val filePath: String
    ) : DialogEvent()

    /**
     * 导航请求：由 UI 层（持有 RouteNavigator 的组合点）消费。
     * ViewModel 不持有导航器引用，避免生命周期/旋转后引用失效。
     * id 保证每次事件实例唯一（即使 route 相同），使消费方 LaunchedEffect 可重新触发。
     */
    data class NavRequest(
        val route: NavKey,
        val id: Long = System.currentTimeMillis()
    ) : DialogEvent()
}

enum class MessageDuration {
    Short,
    Long
}
