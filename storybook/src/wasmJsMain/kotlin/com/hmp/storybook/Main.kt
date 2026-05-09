package com.hmp.storybook

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val container = document.getElementById("compose-container") ?: document.body!!
    ComposeViewport(container) {
        App()
    }
}
