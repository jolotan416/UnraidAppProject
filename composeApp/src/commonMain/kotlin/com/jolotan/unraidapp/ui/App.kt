package com.jolotan.unraidapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jolotan.unraidapp.viewmodels.StartScreenViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinApplication(application = {
            modules(
                module { viewModelOf(::StartScreenViewModel) }
            )
        }) {
            val startScreenViewModel: StartScreenViewModel = koinViewModel()
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { startScreenViewModel.startServer() }) {
                    Text("Click me!")
                }
            }
        }
    }
}