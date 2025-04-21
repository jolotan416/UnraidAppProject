package com.jolotan.unraidapp.ui.viewmodels.wakeonlan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.repositories.NasDataRepository
import com.jolotan.unraidapp.ui.utils.InternalLog
import com.jolotan.unraidapp.ui.utils.isValidIpAddress
import com.jolotan.unraidapp.ui.utils.isValidMacAddress
import com.jolotan.unraidapp.ui.viewdata.FormData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "WakeOnLanViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
class WakeOnLanViewModel(private val nasDataRepository: NasDataRepository) : ViewModel() {
    private val macAddressFormDataSharedFlow: MutableSharedFlow<FormData<String>> =
        MutableSharedFlow(replay = 1)
    private val broadcastIpAddressFormDataSharedFlow: MutableSharedFlow<FormData<String>> =
        MutableSharedFlow(replay = 1)
    private val wakeOnLanPortFormDataSharedFlow: MutableSharedFlow<FormData<Int?>> =
        MutableSharedFlow(replay = 1)
    private val wakeOnLanConnectionStateFlow: MutableStateFlow<WakeOnLanConnectionState?> =
        MutableStateFlow(null)
    val wakeOnLanScreenUiStateStateFlow: StateFlow<GenericState<WakeOnLanScreenUiState, Exception>> =
        nasDataRepository.getNasConnectionDataFlow()
            .flatMapLatest { nasConnectionData ->
                nasConnectionData?.run {
                    macAddressFormDataSharedFlow.emit(FormData(macAddress ?: ""))
                    broadcastIpAddressFormDataSharedFlow.emit(FormData(broadcastIpAddress))
                    wakeOnLanPortFormDataSharedFlow.emit(FormData(wakeOnLanPort))
                }

                combine(
                    macAddressFormDataSharedFlow,
                    broadcastIpAddressFormDataSharedFlow,
                    wakeOnLanPortFormDataSharedFlow,
                    wakeOnLanConnectionStateFlow,
                ) { macAddress, ipAddress, port, wakeOnLanConnectionState ->
                    GenericState.Loaded(
                        WakeOnLanScreenUiState(
                            macAddress,
                            ipAddress,
                            port,
                            wakeOnLanConnectionState
                        )
                    )
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, GenericState.Loading)

    fun handleAction(wakeOnLanScreenAction: WakeOnLanScreenAction) {
        InternalLog.d(tag = TAG, message = "Received action: $wakeOnLanScreenAction")
        viewModelScope.launch {
            when (wakeOnLanScreenAction) {
                is WakeOnLanScreenAction.UpdateMacAddress -> {
                    val previousMacAddressValidationResult =
                        macAddressFormDataSharedFlow.first().isValid
                    macAddressFormDataSharedFlow.emit(
                        FormData(
                            value = wakeOnLanScreenAction.macAddress,
                            isValid = wakeOnLanScreenAction.macAddress.isValidMacAddress() || previousMacAddressValidationResult,
                            isPreviouslyUpdated = true,
                        )
                    )
                }

                is WakeOnLanScreenAction.ValidateMacAddress -> {
                    val macAddressFormData = macAddressFormDataSharedFlow.first()
                    macAddressFormDataSharedFlow.emit(
                        macAddressFormData.copy(
                            isValid = !macAddressFormData.isPreviouslyUpdated || macAddressFormData.value.isValidMacAddress()
                        )
                    )
                }

                is WakeOnLanScreenAction.UpdateBroadcastIpAddress -> {
                    val previousIpAddressValidationResult =
                        broadcastIpAddressFormDataSharedFlow.first().isValid
                    broadcastIpAddressFormDataSharedFlow.emit(
                        FormData(
                            value = wakeOnLanScreenAction.broadcastIpAddress,
                            isValid = wakeOnLanScreenAction.broadcastIpAddress.isValidIpAddress() || previousIpAddressValidationResult,
                            isPreviouslyUpdated = true
                        )
                    )
                }

                is WakeOnLanScreenAction.ValidateBroadcastIpAddress -> {
                    val broadcastIpAddressFormData = broadcastIpAddressFormDataSharedFlow.first()
                    broadcastIpAddressFormDataSharedFlow.emit(
                        broadcastIpAddressFormData.copy(
                            isValid = !broadcastIpAddressFormData.isPreviouslyUpdated || broadcastIpAddressFormData.value.isValidIpAddress()
                        )
                    )
                }

                is WakeOnLanScreenAction.UpdatePort -> {
                    val previousPortValidationResult =
                        wakeOnLanPortFormDataSharedFlow.first().isValid
                    wakeOnLanPortFormDataSharedFlow.emit(
                        FormData(
                            value = wakeOnLanScreenAction.port,
                            isValid = wakeOnLanScreenAction.port.isValidPort() || previousPortValidationResult,
                            isPreviouslyUpdated = true
                        )
                    )
                }

                is WakeOnLanScreenAction.ValidatePort -> {
                    val portFormData = wakeOnLanPortFormDataSharedFlow.first()
                    wakeOnLanPortFormDataSharedFlow.emit(
                        portFormData.copy(
                            isValid = !portFormData.isPreviouslyUpdated || portFormData.value != null
                        )
                    )
                }

                WakeOnLanScreenAction.ResetWakeOnLanConnectionState -> {
                    wakeOnLanConnectionStateFlow.value = null
                }

                WakeOnLanScreenAction.SendWakeOnLan -> {
                    var isFormValid = true
                    val macAddressFormData = macAddressFormDataSharedFlow.first()
                    val macAddress = macAddressFormData.value
                    if (!macAddress.isValidMacAddress()) {
                        macAddressFormDataSharedFlow.emit(macAddressFormData.copy(isValid = false))
                        isFormValid = false
                    }

                    val broadcastIpAddressFormData = broadcastIpAddressFormDataSharedFlow.first()
                    val broadcastIpAddress = broadcastIpAddressFormData.value
                    if (!broadcastIpAddress.isValidIpAddress()) {
                        broadcastIpAddressFormDataSharedFlow.emit(
                            broadcastIpAddressFormData.copy(
                                isValid = false
                            )
                        )
                        isFormValid = false
                    }

                    val portFormData = wakeOnLanPortFormDataSharedFlow.first()
                    val port = portFormData.value
                    if (port == null) {
                        wakeOnLanPortFormDataSharedFlow.emit(portFormData.copy(isValid = false))
                        isFormValid = false
                    }

                    if (isFormValid) {
                        wakeOnLan(broadcastIpAddress, macAddress, port!!)
                    }
                }
            }
        }
    }

    private suspend fun wakeOnLan(broadcastIpAddress: String, macAddress: String, port: Int) {
        wakeOnLanConnectionStateFlow.value = WakeOnLanConnectionState.Loading
        val nasConnectionData =
            nasDataRepository.getNasConnectionDataFlow().firstOrNull()?.copy(
                broadcastIpAddress = broadcastIpAddress,
                macAddress = macAddress,
                wakeOnLanPort = port
            ) ?: error("No NAS connection data to update.")
        withContext(Dispatchers.IO) {
            when (nasDataRepository.wakeOnLan(nasConnectionData)) {
                GenericState.Loading -> {
                    wakeOnLanConnectionStateFlow.value = WakeOnLanConnectionState.Loading
                }

                is GenericState.Loaded -> {
                    wakeOnLanConnectionStateFlow.value = WakeOnLanConnectionState.Connected
                }

                is GenericState.Error -> {
                    wakeOnLanConnectionStateFlow.value = WakeOnLanConnectionState.Error
                }
            }
        }
    }

    private fun Int?.isValidPort(): Boolean = this != null

    sealed interface WakeOnLanScreenAction {
        data class UpdateMacAddress(val macAddress: String) : WakeOnLanScreenAction
        data object ValidateMacAddress : WakeOnLanScreenAction
        data class UpdateBroadcastIpAddress(val broadcastIpAddress: String) : WakeOnLanScreenAction
        data object ValidateBroadcastIpAddress : WakeOnLanScreenAction
        data class UpdatePort(val port: Int?) : WakeOnLanScreenAction
        data object ValidatePort : WakeOnLanScreenAction
        data object ResetWakeOnLanConnectionState : WakeOnLanScreenAction
        data object SendWakeOnLan : WakeOnLanScreenAction
    }

    enum class WakeOnLanConnectionState {
        Loading,
        Connected,
        Error,
    }

    data class WakeOnLanScreenUiState(
        val macAddressFormData: FormData<String>,
        val broadcastIpAddressFormData: FormData<String>,
        val portFormData: FormData<Int?>,
        val wakeOnLanConnectionState: WakeOnLanConnectionState?,
    )
}