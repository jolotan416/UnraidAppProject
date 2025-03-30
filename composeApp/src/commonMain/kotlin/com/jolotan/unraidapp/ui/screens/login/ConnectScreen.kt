package com.jolotan.unraidapp.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
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
import com.jolotan.unraidapp.ui.viewmodels.login.ConnectScreenViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import unraidappproject.composeapp.generated.resources.Res
import unraidappproject.composeapp.generated.resources.compose_multiplatform
import unraidappproject.composeapp.generated.resources.connect
import unraidappproject.composeapp.generated.resources.ip_address
import unraidappproject.composeapp.generated.resources.wake_on_lan
import unraidappproject.composeapp.generated.resources.welcome_text

@Composable
@Preview
fun ConnectScreen(navigateToWakeOnLan: () -> Unit) {
    val connectScreenViewModel: ConnectScreenViewModel = koinViewModel()
    val loginScreenUiState by connectScreenViewModel.loginScreenUiStateStateFlow.collectAsStateWithLifecycle()

    LazyColumn {
        item {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(all = 20.dp)
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
                    GenericState.Loading -> CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                    is GenericState.Loaded -> {
                        val uiState = loginScreenUiState as GenericState.Loaded
                        LoginScreenLoadedState(
                            loginScreenUiState = uiState.value,
                            updateIpAddress = { ipAddress ->
                                connectScreenViewModel.handleAction(
                                    ConnectScreenViewModel.LoginScreenAction.UpdateIpAddress(
                                        ipAddress
                                    )
                                )
                            },
                            connect = {
                                connectScreenViewModel.handleAction(ConnectScreenViewModel.LoginScreenAction.Connect)
                            },
                            navigateToWakeOnLan = navigateToWakeOnLan
                        )
                    }

                    is GenericState.Error -> {}
                }
            }
        }
    }
}

@Composable
@Preview
fun LoginScreenLoadedState(
    loginScreenUiState: ConnectScreenViewModel.LoginScreenUiState,
    updateIpAddress: (String) -> Unit,
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
        value = loginScreenUiState.ipAddress,
        onValueChange = updateIpAddress,
        placeholder = { Text(text = stringResource(Res.string.ip_address)) })
    Spacer(modifier = Modifier.height(20.dp))
    Button(
        onClick = connect,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(Res.string.connect))
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = navigateToWakeOnLan,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant)
    ) {
        Text(text = stringResource(Res.string.wake_on_lan))
    }
}