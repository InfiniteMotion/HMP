
package com.example.hearablemusicplayer.ui.util

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
}

enum class MessageDuration {
    Short,
    Long
}
