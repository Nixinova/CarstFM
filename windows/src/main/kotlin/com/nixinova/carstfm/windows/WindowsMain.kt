package com.nixinova.carstfm.windows

import androidx.compose.material.Text
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CarstFM"
    ) {
        Text("Hello World - Windows")
    }
}
