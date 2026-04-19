package com.example.hearablemusicplayer.ui.common.dialogs.viewmodel

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
}

enum class MessageDuration {
    Short,
    Long
}
