package com.jolotan.unraidapp.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.models.PlatformConfig
import com.jolotan.unraidapp.data.repositories.NasDataRepository
import com.jolotan.unraidapp.ui.utils.isValidIpAddress
import com.jolotan.unraidapp.ui.viewdata.FormData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectScreenViewModel(
    platformConfig: PlatformConfig,
    private val nasDataRepository: NasDataRepository,
) : ViewModel() {
    init {
        println("Starting in ${platformConfig.name}!!!")
    }

    private val ipAddressFormDataSharedFlow: MutableSharedFlow<FormData<String>> =
        MutableSharedFlow(replay = 1)
    val loginScreenUiStateStateFlow: StateFlow<GenericState<LoginScreenUiState, Exception>> =
        nasDataRepository.getNasConnectionDataFlow()
            .flatMapLatest { nasConnectionData ->
                ipAddressFormDataSharedFlow.emit(FormData(nasConnectionData?.ipAddress ?: ""))

                ipAddressFormDataSharedFlow.mapLatest { ipAddress ->
                    GenericState.Loaded(LoginScreenUiState(ipAddress))
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, GenericState.Loading)

    fun handleAction(action: LoginScreenAction) {
        viewModelScope.launch {
            when (action) {
                is LoginScreenAction.UpdateIpAddress -> {
                    val previousValidationResult = ipAddressFormDataSharedFlow.first().isValid
                    ipAddressFormDataSharedFlow.emit(
                        FormData(
                            value = action.ipAddress,
                            isValid = action.ipAddress.isValidIpAddress() || previousValidationResult
                        )
                    )
                }

                is LoginScreenAction.ValidateIpAddress -> {
                    val ipAddressFormData = ipAddressFormDataSharedFlow.first()
                    ipAddressFormDataSharedFlow.emit(
                        ipAddressFormData.copy(
                            isValid = ipAddressFormData.value.isValidIpAddress()
                        )
                    )
                }

                is LoginScreenAction.Connect -> {
                    val ipAddressFormData = ipAddressFormDataSharedFlow.first()
                    val ipAddress = ipAddressFormData.value
                    if (!ipAddress.isValidIpAddress()) {
                        ipAddressFormDataSharedFlow.emit(ipAddressFormData.copy(isValid = false))

                        return@launch
                    }

                    withContext(Dispatchers.IO) {
                        println("Connect to NAS with IP: $ipAddress")
                        nasDataRepository.connectToNas(ipAddress = ipAddress)
                    }
                }
            }
        }
    }

    sealed interface LoginScreenAction {
        data class UpdateIpAddress(val ipAddress: String) : LoginScreenAction
        data object ValidateIpAddress : LoginScreenAction
        data object Connect : LoginScreenAction
    }

    data class LoginScreenUiState(val ipAddressFormData: FormData<String>)
}