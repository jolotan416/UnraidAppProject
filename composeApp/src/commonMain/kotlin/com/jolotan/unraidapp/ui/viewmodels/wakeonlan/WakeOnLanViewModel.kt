package com.jolotan.unraidapp.ui.viewmodels.wakeonlan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.datasource.UdpSocketDataSource
import com.jolotan.unraidapp.data.models.DEFAULT_WAKE_ON_LAN_PORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WakeOnLanViewModel(private val udpSocketDataSource: UdpSocketDataSource) : ViewModel() {
    private val macAddressStateFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val ipAddressStateFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val portStateFlow: MutableStateFlow<Int?> = MutableStateFlow(DEFAULT_WAKE_ON_LAN_PORT)
    val wakeOnLanScreenUiStateStateFlow: StateFlow<GenericState<WakeOnLanScreenUiState, Exception>> =
        combine(
            macAddressStateFlow,
            ipAddressStateFlow,
            portStateFlow
        ) { macAddress, ipAddress, port ->
            GenericState.Loaded(WakeOnLanScreenUiState(macAddress, ipAddress, port))
        }.stateIn(viewModelScope, SharingStarted.Eagerly, GenericState.Loading)

    fun handleAction(wakeOnLanScreenAction: WakeOnLanScreenAction) {
        when (wakeOnLanScreenAction) {
            is WakeOnLanScreenAction.UpdateMacAddress -> {
                macAddressStateFlow.value = wakeOnLanScreenAction.macAddress
            }

            is WakeOnLanScreenAction.UpdateIpAddress -> {
                ipAddressStateFlow.value = wakeOnLanScreenAction.ipAddress
            }

            is WakeOnLanScreenAction.UpdatePort -> {
                portStateFlow.value = wakeOnLanScreenAction.port
            }

            WakeOnLanScreenAction.SendWakeOnLan -> {
                (wakeOnLanScreenUiStateStateFlow.value as? GenericState.Loaded)?.value?.run {
                    viewModelScope.launch(Dispatchers.IO) {
                        udpSocketDataSource.sendWakeOnLanPacket(
                            macAddress,
                            ipAddress,
                            port ?: DEFAULT_WAKE_ON_LAN_PORT
                        )
                    }
                }
            }
        }
    }

    sealed interface WakeOnLanScreenAction {
        data class UpdateMacAddress(val macAddress: String) : WakeOnLanScreenAction
        data class UpdateIpAddress(val ipAddress: String) : WakeOnLanScreenAction
        data class UpdatePort(val port: Int?) : WakeOnLanScreenAction
        data object SendWakeOnLan : WakeOnLanScreenAction
    }

    data class WakeOnLanScreenUiState(
        val macAddress: String,
        val ipAddress: String,
        val port: Int?,
    )
}