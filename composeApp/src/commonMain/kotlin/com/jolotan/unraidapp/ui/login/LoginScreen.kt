package com.jolotan.unraidapp.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.viewmodels.login.LoginScreenViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import unraidappproject.composeapp.generated.resources.Res
import unraidappproject.composeapp.generated.resources.compose_multiplatform
import unraidappproject.composeapp.generated.resources.connect
import unraidappproject.composeapp.generated.resources.ip_address
import unraidappproject.composeapp.generated.resources.login_other_options
import unraidappproject.composeapp.generated.resources.port
import unraidappproject.composeapp.generated.resources.wake_on_lan
import unraidappproject.composeapp.generated.resources.welcome_text

@Composable
@Preview
fun LoginScreen(navigateToWakeOnLan: () -> Unit) {
    val loginScreenViewModel: LoginScreenViewModel = koinViewModel()
    val loginScreenUiState by loginScreenViewModel.uiStateStateFlow.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(40.dp)
            .heightIn(min = 300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = 150.dp, max = 200.dp),
            painter = painterResource(Res.drawable.compose_multiplatform),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (loginScreenUiState) {
            GenericState.Loading -> LoginScreenLoadingState()
            is GenericState.Loaded -> {
                val uiState = loginScreenUiState as GenericState.Loaded
                LoginScreenLoadedState(
                    uiState = uiState.value,
                    updateIpAddress = { ipAddress ->
                        loginScreenViewModel.handleAction(
                            LoginScreenViewModel.LoginScreenAction.UpdateIpAddress(ipAddress)
                        )
                    },
                    updatePort = { port ->
                        loginScreenViewModel.handleAction(
                            LoginScreenViewModel.LoginScreenAction.UpdatePort(port)
                        )
                    },
                    connect = {
                        loginScreenViewModel.handleAction(LoginScreenViewModel.LoginScreenAction.Connect)
                    },
                    navigateToWakeOnLan = navigateToWakeOnLan
                )
            }

            is GenericState.Error -> {}
        }
    }
}

@Composable
@Preview
fun LoginScreenLoadingState() {
    CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
@Preview
fun LoginScreenLoadedState(
    uiState: LoginScreenViewModel.UiState,
    updateIpAddress: (String) -> Unit,
    updatePort: (String) -> Unit,
    connect: () -> Unit,
    navigateToWakeOnLan: () -> Unit
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(Res.string.welcome_text),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.ipAddress,
        onValueChange = updateIpAddress,
        placeholder = { Text(text = stringResource(Res.string.ip_address)) })
    Spacer(modifier = Modifier.height(16.dp))
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.port,
        onValueChange = updatePort,
        placeholder = { Text(text = stringResource(Res.string.port)) })
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = connect,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(Res.string.connect))
    }
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f)
                .height(1.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = stringResource(Res.string.login_other_options))
        Spacer(modifier = Modifier.width(16.dp))
        Divider(
            modifier = Modifier.weight(1f)
                .height(1.dp)
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = navigateToWakeOnLan) {
        Text(text = stringResource(Res.string.wake_on_lan))
    }
}