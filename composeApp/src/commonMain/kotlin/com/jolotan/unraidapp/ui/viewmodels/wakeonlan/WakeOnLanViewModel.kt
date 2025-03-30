package com.jolotan.unraidapp.ui.viewmodels.wakeonlan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.repositories.NasDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WakeOnLanViewModel(private val nasDataRepository: NasDataRepository) : ViewModel() {
    private val macAddressSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
    private val broadcastIpAddressSharedFlow: MutableSharedFlow<String> =
        MutableSharedFlow(replay = 1)
    private val wakeOnLanPortSharedFlow: MutableSharedFlow<Int?> = MutableSharedFlow(replay = 1)
    val wakeOnLanScreenUiStateStateFlow: StateFlow<GenericState<WakeOnLanScreenUiState, Exception>> =
        nasDataRepository.getNasConnectionDataFlow()
            .flatMapLatest { nasConnectionData ->
                nasConnectionData?.run {
                    macAddressSharedFlow.emit(macAddress ?: "")
                    broadcastIpAddressSharedFlow.emit(broadcastIpAddress)
                    wakeOnLanPortSharedFlow.emit(wakeOnLanPort)
                }

                combine(
                    macAddressSharedFlow,
                    broadcastIpAddressSharedFlow,
                    wakeOnLanPortSharedFlow
                ) { macAddress, ipAddress, port ->
                    GenericState.Loaded(WakeOnLanScreenUiState(macAddress, ipAddress, port))
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, GenericState.Loading)

    fun handleAction(wakeOnLanScreenAction: WakeOnLanScreenAction) {
        when (wakeOnLanScreenAction) {
            is WakeOnLanScreenAction.UpdateMacAddress -> {
                macAddressSharedFlow.tryEmit(wakeOnLanScreenAction.macAddress)
            }

            is WakeOnLanScreenAction.UpdateBroadcastIpAddress -> {
                broadcastIpAddressSharedFlow.tryEmit(wakeOnLanScreenAction.broadcastIpAddress)
            }

            is WakeOnLanScreenAction.UpdatePort -> {
                wakeOnLanPortSharedFlow.tryEmit(wakeOnLanScreenAction.port)
            }

            WakeOnLanScreenAction.SendWakeOnLan -> {
                viewModelScope.launch(Dispatchers.IO) {
                    (wakeOnLanScreenUiStateStateFlow.value as? GenericState.Loaded)?.value?.run {
                        val nasConnectionData =
                            nasDataRepository.getNasConnectionDataFlow().firstOrNull()
                                ?: error("No NAS connection data to update.")
                        nasDataRepository.wakeOnLan(
                            nasConnectionData.copy(
                                broadcastIpAddress = broadcastIpAddress,
                                macAddress = macAddress,
                                wakeOnLanPort = port ?: error("No Wake on LAN port")
                            )
                        )
                    }
                }
            }
        }
    }

    sealed interface WakeOnLanScreenAction {
        data class UpdateMacAddress(val macAddress: String) : WakeOnLanScreenAction
        data class UpdateBroadcastIpAddress(val broadcastIpAddress: String) : WakeOnLanScreenAction
        data class UpdatePort(val port: Int?) : WakeOnLanScreenAction
        data object SendWakeOnLan : WakeOnLanScreenAction
    }

    data class WakeOnLanScreenUiState(
        val macAddress: String,
        val broadcastIpAddress: String,
        val port: Int?,
    )
}