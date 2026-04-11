
package com.example.hearablemusicplayer.ui.controller

import com.example.hearablemusicplayer.ui.util.DialogEvent
import com.example.hearablemusicplayer.ui.util.MessageDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialogManager @Inject constructor() {
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

    fun dismissDialog() {
        scope.launch {
            _dialogEvent.emit(null)
        }
    }
}
