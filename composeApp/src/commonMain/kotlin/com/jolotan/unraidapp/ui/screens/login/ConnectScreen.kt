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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import unraidappproject.composeapp.generated.resources.api_key
import unraidappproject.composeapp.generated.resources.api_key_invalid_error_message
import unraidappproject.composeapp.generated.resources.compose_multiplatform
import unraidappproject.composeapp.generated.resources.connect
import unraidappproject.composeapp.generated.resources.ip_address
import unraidappproject.composeapp.generated.resources.ip_address_invalid_error_message
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
                    GenericState.Loading -> CircularProgressIndicator()
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
                            validateIpAddress = {
                                connectScreenViewModel.handleAction(ConnectScreenViewModel.LoginScreenAction.ValidateIpAddress)
                            },
                            updateApiKey = { apiKey ->
                                connectScreenViewModel.handleAction(
                                    ConnectScreenViewModel.LoginScreenAction.UpdateApiKey(
                                        apiKey
                                    )
                                )
                            },
                            validateApiKey = {
                                connectScreenViewModel.handleAction(ConnectScreenViewModel.LoginScreenAction.ValidateApiKey)
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
    validateIpAddress: () -> Unit,
    updateApiKey: (String) -> Unit,
    validateApiKey: () -> Unit,
    connect: () -> Unit,
    navigateToWakeOnLan: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester.Default }

    Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(Res.string.welcome_text),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    TextField(
        modifier = Modifier.fillMaxWidth()
            .onFocusChanged { focusState ->
                if (!focusState.hasFocus) {
                    validateIpAddress()
                } else {
                    updateIpAddress(loginScreenUiState.ipAddressFormData.value)
                }
            }.focusRequester(focusRequester),
        value = loginScreenUiState.ipAddressFormData.value,
        onValueChange = updateIpAddress,
        placeholder = { Text(text = stringResource(Res.string.ip_address)) },
        isError = !loginScreenUiState.ipAddressFormData.isValid,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = {
            focusManager.moveFocus(FocusDirection.Next)
        })
    )
    if (!loginScreenUiState.ipAddressFormData.isValid) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.ip_address_invalid_error_message),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.error,
            textAlign = TextAlign.Start
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    TextField(
        modifier = Modifier.fillMaxWidth()
            .onFocusChanged { focusState ->
                if (!focusState.hasFocus) {
                    validateApiKey()
                } else {
                    updateApiKey(loginScreenUiState.apiKeyFormData.value)
                }
            }.focusRequester(focusRequester),
        value = loginScreenUiState.apiKeyFormData.value,
        onValueChange = updateApiKey,
        placeholder = { Text(text = stringResource(Res.string.api_key)) },
        isError = !loginScreenUiState.apiKeyFormData.isValid,
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            connect()
        })
    )
    if (!loginScreenUiState.apiKeyFormData.isValid) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.api_key_invalid_error_message),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.error,
            textAlign = TextAlign.Start
        )
    }
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