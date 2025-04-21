package com.jolotan.unraidapp.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.api.BackendConnectionError
import com.jolotan.unraidapp.data.models.PlatformConfig
import com.jolotan.unraidapp.data.repositories.NasDataRepository
import com.jolotan.unraidapp.ui.utils.InternalLog
import com.jolotan.unraidapp.ui.utils.isValidIpAddress
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ConnectScreenViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
class LoginScreenViewModel(
    platformConfig: PlatformConfig,
    private val nasDataRepository: NasDataRepository,
) : ViewModel() {
    private val ipAddressFormDataSharedFlow: MutableSharedFlow<FormData<String>> =
        MutableSharedFlow(replay = 1)
    private val apiKeyFormDataSharedFlow: MutableSharedFlow<FormData<String>> =
        MutableSharedFlow(replay = 1)
    private val loginConnectionStateFlow: MutableStateFlow<LoginConnectionState?> =
        MutableStateFlow(null)
    val loginScreenUiStateStateFlow: StateFlow<GenericState<LoginScreenUiState, Exception>> =
        nasDataRepository.getNasConnectionDataFlow()
            .flatMapLatest { nasConnectionData ->
                ipAddressFormDataSharedFlow.emit(FormData(nasConnectionData?.ipAddress ?: ""))
                apiKeyFormDataSharedFlow.emit(FormData(nasConnectionData?.apiKey ?: ""))

                combine(
                    ipAddressFormDataSharedFlow,
                    apiKeyFormDataSharedFlow,
                    loginConnectionStateFlow
                ) { ipAddress, apiKey, loginConnectionState ->
                    GenericState.Loaded(LoginScreenUiState(ipAddress, apiKey, loginConnectionState))
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, GenericState.Loading)

    init {
        InternalLog.d(tag = TAG, message = "Starting in ${platformConfig.name}!!!")
        handleAction(LoginScreenAction.Connect)
    }

    fun handleAction(action: LoginScreenAction) {
        InternalLog.d(tag = TAG, message = "Received action: $action")
        viewModelScope.launch {
            when (action) {
                is LoginScreenAction.UpdateIpAddress -> {
                    val previousValidationResult = ipAddressFormDataSharedFlow.first().isValid
                    ipAddressFormDataSharedFlow.emit(
                        FormData(
                            value = action.ipAddress,
                            isValid = action.ipAddress.isValidIpAddress() || previousValidationResult,
                            isPreviouslyUpdated = true,
                        )
                    )
                }

                LoginScreenAction.ValidateIpAddress -> {
                    val ipAddressFormData = ipAddressFormDataSharedFlow.first()
                    ipAddressFormDataSharedFlow.emit(
                        ipAddressFormData.copy(
                            isValid = !ipAddressFormData.isPreviouslyUpdated || ipAddressFormData.value.isValidIpAddress()
                        )
                    )
                }

                is LoginScreenAction.UpdateApiKey -> {
                    val previousValidationResult = apiKeyFormDataSharedFlow.first().isValid
                    apiKeyFormDataSharedFlow.emit(
                        FormData(
                            value = action.apiKey,
                            isValid = action.apiKey.isValidApiKey() || previousValidationResult,
                            isPreviouslyUpdated = true,
                        )
                    )
                }

                LoginScreenAction.ValidateApiKey -> {
                    val apiKeyFormData = apiKeyFormDataSharedFlow.first()
                    apiKeyFormDataSharedFlow.emit(
                        apiKeyFormData.copy(
                            isValid = !apiKeyFormData.isPreviouslyUpdated || apiKeyFormData.value.isValidApiKey()
                        )
                    )
                }

                LoginScreenAction.ResetLoginConnectionState -> {
                    loginConnectionStateFlow.value = null
                }

                LoginScreenAction.Connect -> {
                    var isFormValid = true
                    val ipAddressFormData = ipAddressFormDataSharedFlow.first()
                    val ipAddress = ipAddressFormData.value
                    if (!ipAddress.isValidIpAddress()) {
                        ipAddressFormDataSharedFlow.emit(ipAddressFormData.copy(isValid = false))
                        isFormValid = false
                    }

                    val apiKeyFormData = apiKeyFormDataSharedFlow.first()
                    val apiKey = apiKeyFormData.value
                    if (!apiKey.isValidApiKey()) {
                        apiKeyFormDataSharedFlow.emit(apiKeyFormData.copy(isValid = false))
                        isFormValid = false
                    }

                    if (isFormValid) {
                        connectToNas(ipAddress, apiKey)
                    }
                }
            }
        }
    }

    private suspend fun connectToNas(ipAddress: String, apiKey: String) {
        loginConnectionStateFlow.value = LoginConnectionState.Loading
        withContext(Dispatchers.IO) {
            InternalLog.d(tag = TAG, message = "Connect to NAS with IP: $ipAddress")
            when (val nasConnectionResult = nasDataRepository.connectToNas(
                ipAddress = ipAddress,
                apiKey = apiKey
            )) {
                GenericState.Loading -> {
                    loginConnectionStateFlow.value = LoginConnectionState.Loading
                }

                is GenericState.Loaded -> {
                    loginConnectionStateFlow.value = LoginConnectionState.Connected
                }

                is GenericState.Error if nasConnectionResult.error == BackendConnectionError.ConnectionError -> {
                    loginConnectionStateFlow.value = LoginConnectionState.ConnectionError
                }

                else -> {
                    loginConnectionStateFlow.value = LoginConnectionState.OtherError
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
        data object ResetLoginConnectionState : LoginScreenAction
        data object Connect : LoginScreenAction
    }

    data class LoginScreenUiState(
        val ipAddressFormData: FormData<String>,
        val apiKeyFormData: FormData<String>,
        val loginConnectionState: LoginConnectionState?
    )

    enum class LoginConnectionState {
        Loading,
        Connected,
        ConnectionError,
        OtherError,
    }
}