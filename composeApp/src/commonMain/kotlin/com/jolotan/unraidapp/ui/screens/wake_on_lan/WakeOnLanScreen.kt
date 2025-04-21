package com.jolotan.unraidapp.ui.screens.wake_on_lan

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.ui.components.CustomButton
import com.jolotan.unraidapp.ui.components.CustomDialog
import com.jolotan.unraidapp.ui.viewmodels.wakeonlan.WakeOnLanViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import unraidappproject.composeapp.generated.resources.Res
import unraidappproject.composeapp.generated.resources.broadcast_ip_address
import unraidappproject.composeapp.generated.resources.ip_address_invalid_error_message
import unraidappproject.composeapp.generated.resources.mac_address
import unraidappproject.composeapp.generated.resources.mac_address_invalid_error_message
import unraidappproject.composeapp.generated.resources.nas_other_error_message
import unraidappproject.composeapp.generated.resources.ok
import unraidappproject.composeapp.generated.resources.port_invalid_error_message
import unraidappproject.composeapp.generated.resources.send_wake_on_lan
import unraidappproject.composeapp.generated.resources.wake_on_lan_port

@Composable
fun WakeOnLanScreen() {
    val wakeOnLanViewModel: WakeOnLanViewModel = koinViewModel()
    val wakeOnLanScreenUiState by wakeOnLanViewModel.wakeOnLanScreenUiStateStateFlow.collectAsStateWithLifecycle()

    when (wakeOnLanScreenUiState) {
        GenericState.Loading -> CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
        is GenericState.Loaded -> {
            val uiState = wakeOnLanScreenUiState as GenericState.Loaded
            WakeOnLanScreenLoadedState(
                uiState = uiState.value,
                updateMacAddress = {
                    wakeOnLanViewModel.handleAction(
                        WakeOnLanViewModel.WakeOnLanScreenAction.UpdateMacAddress(it)
                    )
                },
                validateMacAddress = {
                    wakeOnLanViewModel.handleAction(WakeOnLanViewModel.WakeOnLanScreenAction.ValidateMacAddress)
                },
                updateIpAddress = {
                    wakeOnLanViewModel.handleAction(
                        WakeOnLanViewModel.WakeOnLanScreenAction.UpdateBroadcastIpAddress(it)
                    )
                },
                validateIpAddress = {
                    wakeOnLanViewModel.handleAction(WakeOnLanViewModel.WakeOnLanScreenAction.ValidateBroadcastIpAddress)
                },
                updatePort = {
                    wakeOnLanViewModel.handleAction(
                        WakeOnLanViewModel.WakeOnLanScreenAction.UpdatePort(it.toIntOrNull())
                    )
                },
                validatePort = {
                    wakeOnLanViewModel.handleAction(WakeOnLanViewModel.WakeOnLanScreenAction.ValidatePort)
                },
                dismissWakeOnLanErrorDialog = {
                    wakeOnLanViewModel.handleAction(WakeOnLanViewModel.WakeOnLanScreenAction.ResetWakeOnLanConnectionState)
                },
                sendWakeOnLan = {
                    wakeOnLanViewModel.handleAction(WakeOnLanViewModel.WakeOnLanScreenAction.SendWakeOnLan)
                })
        }

        is GenericState.Error -> {}
    }
}

@Composable
fun WakeOnLanScreenLoadedState(
    uiState: WakeOnLanViewModel.WakeOnLanScreenUiState,
    updateMacAddress: (String) -> Unit,
    validateMacAddress: () -> Unit,
    updateIpAddress: (String) -> Unit,
    validateIpAddress: () -> Unit,
    updatePort: (String) -> Unit,
    validatePort: () -> Unit,
    dismissWakeOnLanErrorDialog: () -> Unit,
    sendWakeOnLan: () -> Unit,
) {
    WakeOnLanForm(
        uiState = uiState,
        updateMacAddress = updateMacAddress,
        validateMacAddress = validateMacAddress,
        updateIpAddress = updateIpAddress,
        validateIpAddress = validateIpAddress,
        updatePort = updatePort,
        validatePort = validatePort,
        sendWakeOnLan = sendWakeOnLan
    )
    WakeOnLanConnection(
        wakeOnLanConnectionState = uiState.wakeOnLanConnectionState,
        dismissWakeOnLanErrorDialog = dismissWakeOnLanErrorDialog
    )
}

@Composable
fun WakeOnLanForm(
    uiState: WakeOnLanViewModel.WakeOnLanScreenUiState,
    updateMacAddress: (String) -> Unit,
    validateMacAddress: () -> Unit,
    updateIpAddress: (String) -> Unit,
    validateIpAddress: () -> Unit,
    updatePort: (String) -> Unit,
    validatePort: () -> Unit,
    sendWakeOnLan: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester.Default }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(all = 20.dp)) {
        item {
            TextField(
                value = uiState.macAddressFormData.value,
                onValueChange = updateMacAddress,
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged { focusState: FocusState ->
                        if (!focusState.isFocused) {
                            validateMacAddress()
                        } else {
                            updateMacAddress(uiState.macAddressFormData.value)
                        }
                    }.focusRequester(focusRequester),
                placeholder = { Text(text = stringResource(Res.string.mac_address)) },
                isError = !uiState.macAddressFormData.isValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                })
            )
            if (!uiState.macAddressFormData.isValid) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.mac_address_invalid_error_message),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.error,
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = uiState.broadcastIpAddressFormData.value,
                onValueChange = updateIpAddress,
                modifier = Modifier.fillMaxWidth()
                    .onFocusEvent { focusState: FocusState ->
                        if (!focusState.hasFocus) {
                            validateIpAddress()
                        } else {
                            updateIpAddress(uiState.broadcastIpAddressFormData.value)
                        }
                    }.focusRequester(focusRequester),
                placeholder = { Text(text = stringResource(Res.string.broadcast_ip_address)) },
                isError = !uiState.broadcastIpAddressFormData.isValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                })
            )
            if (!uiState.broadcastIpAddressFormData.isValid) {
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
                value = uiState.portFormData.value?.toString() ?: "",
                onValueChange = updatePort,
                modifier = Modifier.fillMaxWidth()
                    .onFocusEvent { focusState: FocusState ->
                        if (!focusState.hasFocus) {
                            validatePort()
                        } else {
                            updatePort(uiState.portFormData.value.toString())
                        }
                    },
                placeholder = { Text(text = stringResource(Res.string.wake_on_lan_port)) },
                isError = !uiState.portFormData.isValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    sendWakeOnLan()
                })
            )
            if (!uiState.portFormData.isValid) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.port_invalid_error_message),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.error,
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            CustomButton(
                modifier = Modifier.fillMaxWidth(),
                buttonText = stringResource(Res.string.send_wake_on_lan),
                enabled =
                (uiState.wakeOnLanConnectionState != WakeOnLanViewModel.WakeOnLanConnectionState.Loading) &&
                        (uiState.wakeOnLanConnectionState != WakeOnLanViewModel.WakeOnLanConnectionState.Connected),
                isLoading = uiState.wakeOnLanConnectionState == WakeOnLanViewModel.WakeOnLanConnectionState.Loading,
                onClick = sendWakeOnLan,
            )
        }
    }
}

@Composable
fun WakeOnLanConnection(
    wakeOnLanConnectionState: WakeOnLanViewModel.WakeOnLanConnectionState?,
    dismissWakeOnLanErrorDialog: () -> Unit
) {
    if (wakeOnLanConnectionState == WakeOnLanViewModel.WakeOnLanConnectionState.Error) {
        CustomDialog(
            dialogText = stringResource(Res.string.nas_other_error_message),
            buttonText = stringResource(Res.string.ok),
            onButtonClick = dismissWakeOnLanErrorDialog,
            onDismissRequest = dismissWakeOnLanErrorDialog
        )
    }
}