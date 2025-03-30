package com.jolotan.unraidapp.ui.screens.wake_on_lan

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.ui.viewmodels.wakeonlan.WakeOnLanViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import unraidappproject.composeapp.generated.resources.Res
import unraidappproject.composeapp.generated.resources.broadcast_ip_address
import unraidappproject.composeapp.generated.resources.mac_address
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
                updateIpAddress = {
                    wakeOnLanViewModel.handleAction(
                        WakeOnLanViewModel.WakeOnLanScreenAction.UpdateBroadcastIpAddress(it)
                    )
                },
                updatePort = {
                    wakeOnLanViewModel.handleAction(
                        WakeOnLanViewModel.WakeOnLanScreenAction.UpdatePort(it.toIntOrNull())
                    )
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
    updateIpAddress: (String) -> Unit,
    updatePort: (String) -> Unit,
    sendWakeOnLan: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(all = 20.dp)) {
        item {
            TextField(
                value = uiState.macAddress,
                onValueChange = updateMacAddress,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(Res.string.mac_address)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = uiState.broadcastIpAddress,
                onValueChange = updateIpAddress,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(Res.string.broadcast_ip_address)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = uiState.port?.toString() ?: "",
                onValueChange = updatePort,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(Res.string.wake_on_lan_port)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = sendWakeOnLan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.send_wake_on_lan))
            }
        }
    }
}