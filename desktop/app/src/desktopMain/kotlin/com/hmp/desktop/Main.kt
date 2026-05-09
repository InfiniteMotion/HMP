package com.hmp.desktop

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hmp.desktop.ui.DesktopApp

fun main() {
    HmpDesktopApplication.init()

    application {
        val state = rememberWindowState(
            size = DpSize(1200.dp, 800.dp),
            position = WindowPosition(Alignment.Center)
        )

        Window(
            onCloseRequest = ::exitApplication,
            state = state,
            title = "HMP - Hearable Music Player"
        ) {
            DesktopApp()
        }
    }
}
