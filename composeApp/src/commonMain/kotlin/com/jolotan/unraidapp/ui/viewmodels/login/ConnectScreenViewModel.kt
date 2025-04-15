package com.jolotan.unraidapp.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.models.PlatformConfig
import com.jolotan.unraidapp.data.repositories.NasDataRepository
import com.jolotan.unraidapp.ui.utils.isValidIpAddress
import com.jolotan.unraidapp.ui.viewdata.FormData
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ConnectScreenViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectScreenViewModel(
    platformConfig: PlatformConfig,
    private val nasDataRepository: NasDataRepository,
) : ViewModel() {
    init {
        Napier.d(tag = TAG, message = "Starting in ${platformConfig.name}!!!")
    }

    private val ipAddressFormDataSharedFlow: MutableSharedFlow<FormData<String>> =
        MutableSharedFlow(replay = 1)
    private val apiKeyFormDataSharedFlow: MutableSharedFlow<FormData<String>> =
        MutableSharedFlow(replay = 1)
    val loginScreenUiStateStateFlow: StateFlow<GenericState<LoginScreenUiState, Exception>> =
        nasDataRepository.getNasConnectionDataFlow()
            .flatMapLatest { nasConnectionData ->
                ipAddressFormDataSharedFlow.emit(FormData(nasConnectionData?.ipAddress ?: ""))
                apiKeyFormDataSharedFlow.emit(FormData(nasConnectionData?.apiKey ?: ""))

                combine(
                    ipAddressFormDataSharedFlow,
                    apiKeyFormDataSharedFlow
                ) { ipAddress, apiKey ->
                    GenericState.Loaded(LoginScreenUiState(ipAddress, apiKey))
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

                LoginScreenAction.ValidateIpAddress -> {
                    val ipAddressFormData = ipAddressFormDataSharedFlow.first()
                    ipAddressFormDataSharedFlow.emit(
                        ipAddressFormData.copy(
                            isValid = ipAddressFormData.value.isValidIpAddress()
                        )
                    )
                }

                is LoginScreenAction.UpdateApiKey -> {
                    val previousValidationResult = apiKeyFormDataSharedFlow.first().isValid
                    apiKeyFormDataSharedFlow.emit(
                        FormData(
                            value = action.apiKey,
                            isValid = action.apiKey.isValidApiKey() || previousValidationResult
                        )
                    )
                }

                LoginScreenAction.ValidateApiKey -> {
                    val apiKeyFormData = apiKeyFormDataSharedFlow.first()
                    apiKeyFormDataSharedFlow.emit(
                        apiKeyFormData.copy(
                            isValid = apiKeyFormData.value.isValidApiKey()
                        )
                    )
                }

                LoginScreenAction.Connect -> {
                    val ipAddressFormData = ipAddressFormDataSharedFlow.first()
                    val ipAddress = ipAddressFormData.value
                    val apiKeyFormData = apiKeyFormDataSharedFlow.first()
                    val apiKey = apiKeyFormData.value

                    when {
                        !ipAddress.isValidIpAddress() -> {
                            ipAddressFormDataSharedFlow.emit(ipAddressFormData.copy(isValid = false))
                        }

                        !apiKey.isValidApiKey() -> {
                            apiKeyFormDataSharedFlow.emit(apiKeyFormData.copy(isValid = false))
                        }

                        else -> {
                            withContext(Dispatchers.IO) {
                                Napier.d(tag = TAG, message = "Connect to NAS with IP: $ipAddress")
                                nasDataRepository.connectToNas(
                                    ipAddress = ipAddress,
                                    apiKey = apiKey
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun String.isValidApiKey(): Boolean = isNotBlank()

    sealed interface LoginScreenAction {
        data class UpdateIpAddress(val ipAddress: String) : LoginScreenAction
        data object ValidateIpAddress : LoginScreenAction
        data class UpdateApiKey(val apiKey: String) : LoginScreenAction
        data object ValidateApiKey : LoginScreenAction
        data object Connect : LoginScreenAction
    }

    data class LoginScreenUiState(
        val ipAddressFormData: FormData<String>,
        val apiKeyFormData: FormData<String>
    )
}