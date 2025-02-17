package com.jolotan.unraidapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jolotan.unraidapp.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "UnraidAppProject",
    ) {
        App()
    }
}