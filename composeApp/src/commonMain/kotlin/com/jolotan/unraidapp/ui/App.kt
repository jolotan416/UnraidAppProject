package com.jolotan.unraidapp.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.jolotan.unraidapp.di.KoinUnraidApplication
import com.jolotan.unraidapp.ui.login.LoginScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinUnraidApplication {
            LoginScreen()
        }
    }
}