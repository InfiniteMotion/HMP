package com.hearablemusic.player.ui.common.dialogs.controller

import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogEvent
import com.hearablemusic.player.ui.common.dialogs.viewmodel.MessageDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DialogManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _dialogEvent = MutableSharedFlow<DialogEvent?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val dialogEvent: SharedFlow<DialogEvent?> = _dialogEvent.asSharedFlow()

    fun showDialog(event: DialogEvent) {
        scope.launch {
            _dialogEvent.emit(event)
        }
    }

    fun showMessage(message: String, duration: MessageDuration = MessageDuration.Short) {
        showDialog(DialogEvent.Message(message, duration))
    }

    fun showTimerDialog(onConfirm: (Int) -> Unit, onDismiss: () -> Unit = {}) {
        showDialog(DialogEvent.ShowTimerDialog(onConfirm, onDismiss))
    }

    fun dismissTimerDialog() {
        showDialog(DialogEvent.DismissTimerDialog)
    }

    fun dismissDialog() {
        scope.launch {
            _dialogEvent.emit(null)
        }
    }

    fun shareMusic(title: String, artist: String, album: String, filePath: String) {
        showDialog(DialogEvent.ShareMusic(title, artist, album, filePath))
    }
}
