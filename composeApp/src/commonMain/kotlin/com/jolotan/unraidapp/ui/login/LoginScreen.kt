package com.jolotan.unraidapp.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jolotan.unraidapp.viewmodels.login.LoginScreenViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen() {
    val loginScreenViewModel: LoginScreenViewModel = koinViewModel()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { loginScreenViewModel.startServer() }) {
            Text("Click me!")
        }
    }
}