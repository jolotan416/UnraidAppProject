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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.jolotan.unraidapp.ui.components.CustomButton
import com.jolotan.unraidapp.ui.components.CustomDialog
import com.jolotan.unraidapp.ui.viewmodels.login.LoginScreenViewModel
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
import unraidappproject.composeapp.generated.resources.nas_connection_error_message
import unraidappproject.composeapp.generated.resources.nas_other_error_message
import unraidappproject.composeapp.generated.resources.ok
import unraidappproject.composeapp.generated.resources.wake_on_lan
import unraidappproject.composeapp.generated.resources.welcome_text

@Composable
@Preview
fun LoginScreen(navigateToWakeOnLan: () -> Unit, navigateToDashboard: () -> Unit) {
    val loginScreenViewModel: LoginScreenViewModel = koinViewModel()
    val loginScreenUiState by loginScreenViewModel.loginScreenUiStateStateFlow.collectAsStateWithLifecycle()

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
                                loginScreenViewModel.handleAction(
                                    LoginScreenViewModel.LoginScreenAction.UpdateIpAddress(
                                        ipAddress
                                    )
                                )
                            },
                            validateIpAddress = {
                                loginScreenViewModel.handleAction(LoginScreenViewModel.LoginScreenAction.ValidateIpAddress)
                            },
                            updateApiKey = { apiKey ->
                                loginScreenViewModel.handleAction(
                                    LoginScreenViewModel.LoginScreenAction.UpdateApiKey(
                                        apiKey
                                    )
                                )
                            },
                            validateApiKey = {
                                loginScreenViewModel.handleAction(LoginScreenViewModel.LoginScreenAction.ValidateApiKey)
                            },
                            dismissLoginConnectionDialog = {
                                loginScreenViewModel.handleAction(LoginScreenViewModel.LoginScreenAction.ResetLoginConnectionState)
                            },
                            connect = {
                                loginScreenViewModel.handleAction(LoginScreenViewModel.LoginScreenAction.Connect)
                            },
                            navigateToDashboard = navigateToDashboard,
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
    loginScreenUiState: LoginScreenViewModel.LoginScreenUiState,
    updateIpAddress: (String) -> Unit,
    validateIpAddress: () -> Unit,
    updateApiKey: (String) -> Unit,
    validateApiKey: () -> Unit,
    dismissLoginConnectionDialog: () -> Unit,
    connect: () -> Unit,
    navigateToDashboard: () -> Unit,
    navigateToWakeOnLan: () -> Unit
) {
    LoginForm(
        loginScreenUiState = loginScreenUiState,
        updateIpAddress = updateIpAddress,
        validateIpAddress = validateIpAddress,
        updateApiKey = updateApiKey,
        validateApiKey = validateApiKey,
        connect = connect
    )
    LoginConnection(
        loginConnectionState = loginScreenUiState.loginConnectionState,
        navigateToDashboard = navigateToDashboard,
        navigateToWakeOnLan = navigateToWakeOnLan,
        dismissLoginConnectionDialog = dismissLoginConnectionDialog
    )
}

@Composable
fun LoginForm(
    loginScreenUiState: LoginScreenViewModel.LoginScreenUiState,
    updateIpAddress: (String) -> Unit,
    validateIpAddress: () -> Unit,
    updateApiKey: (String) -> Unit,
    validateApiKey: () -> Unit,
    connect: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester.Default }

    Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(Res.string.welcome_text),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
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
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
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
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Start
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
    CustomButton(
        modifier = Modifier.fillMaxWidth(),
        buttonText = stringResource(Res.string.connect),
        enabled = (loginScreenUiState.loginConnectionState != LoginScreenViewModel.LoginConnectionState.Loading) &&
                (loginScreenUiState.loginConnectionState != LoginScreenViewModel.LoginConnectionState.Connected),
        isLoading = loginScreenUiState.loginConnectionState == LoginScreenViewModel.LoginConnectionState.Loading,
        onClick = connect,
    )
}

@Composable
fun LoginConnection(
    loginConnectionState: LoginScreenViewModel.LoginConnectionState?,
    navigateToDashboard: () -> Unit,
    navigateToWakeOnLan: () -> Unit,
    dismissLoginConnectionDialog: () -> Unit
) {
    if (loginConnectionState == LoginScreenViewModel.LoginConnectionState.ConnectionError) {
        CustomDialog(
            dialogText = stringResource(Res.string.nas_connection_error_message),
            buttonText = stringResource(Res.string.wake_on_lan),
            onButtonClick = {
                navigateToWakeOnLan()
                dismissLoginConnectionDialog()
            },
            onDismissRequest = dismissLoginConnectionDialog
        )
    } else if (loginConnectionState == LoginScreenViewModel.LoginConnectionState.OtherError) {
        CustomDialog(
            dialogText = stringResource(Res.string.nas_other_error_message),
            buttonText = stringResource(Res.string.ok),
            onButtonClick = dismissLoginConnectionDialog,
            onDismissRequest = dismissLoginConnectionDialog
        )
    }

    LaunchedEffect(loginConnectionState) {
        if (loginConnectionState == LoginScreenViewModel.LoginConnectionState.Connected) {
            navigateToDashboard()
        }
    }
}