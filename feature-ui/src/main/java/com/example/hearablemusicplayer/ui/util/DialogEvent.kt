
package com.example.hearablemusicplayer.ui.util

sealed class DialogEvent {
    data class Message(
        val message: String,
        val duration: MessageDuration = MessageDuration.Short,
        val id: Long = System.currentTimeMillis() // 添加唯一标识符
    ) : DialogEvent()
}

enum class MessageDuration {
    Short,
    Long
}
