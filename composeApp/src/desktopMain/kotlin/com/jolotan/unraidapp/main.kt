package com.jolotan.unraidapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jolotan.unraidapp.di.initializeKoin
import com.jolotan.unraidapp.ui.App
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun main() = application {
    initializeKoin()
    Napier.base(DebugAntilog())
    Window(
        onCloseRequest = ::exitApplication,
        title = "UnraidAppProject",
    ) {
        App()
    }
}