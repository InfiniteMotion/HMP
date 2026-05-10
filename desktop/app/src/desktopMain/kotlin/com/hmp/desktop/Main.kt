package com.hmp.desktop

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hmp.desktop.ui.common.pages.MainScreen

fun main() {
    HmpDesktopApplication.init()

    application {
        val state = rememberWindowState(
            size = DpSize(1200.dp, 800.dp),
            position = WindowPosition(Alignment.Center)
        )

        val backHandler = remember { mutableStateOf<(() -> Unit)?>(null) }

        Window(
            onCloseRequest = ::exitApplication,
            state = state,
            title = "HMP - Hearable Music Player",
            onKeyEvent = { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.Escape -> {
                            backHandler.value?.invoke()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
        ) {
            MainScreen(
                onBackHandlerReady = { handler -> backHandler.value = handler }
            )
        }
    }
}
