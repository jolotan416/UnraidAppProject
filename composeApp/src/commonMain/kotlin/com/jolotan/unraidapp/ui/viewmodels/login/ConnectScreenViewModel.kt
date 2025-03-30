package com.jolotan.unraidapp.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.models.PlatformConfig
import com.jolotan.unraidapp.data.repositories.NasDataRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectScreenViewModel(
    platformConfig: PlatformConfig,
    private val nasDataRepository: NasDataRepository,
) : ViewModel() {
    private val ipAddressSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
    val loginScreenUiStateStateFlow: StateFlow<GenericState<LoginScreenUiState, Exception>> =
        nasDataRepository.getNasConnectionDataFlow()
            .flatMapLatest { nasConnectionData ->
                ipAddressSharedFlow.emit(nasConnectionData?.ipAddress ?: "")

                ipAddressSharedFlow.mapLatest { ipAddress ->
                    GenericState.Loaded(LoginScreenUiState(ipAddress))
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, GenericState.Loading)

    init {
        println("Starting in ${platformConfig.name}!!!")
    }

    fun handleAction(action: LoginScreenAction) {
        when (action) {
            is LoginScreenAction.UpdateIpAddress -> {
                ipAddressSharedFlow.tryEmit(action.ipAddress)
            }

            is LoginScreenAction.Connect -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val ipAddress = ipAddressSharedFlow.first()
                    println("Connect to NAS with IP: $ipAddress")
                    nasDataRepository.connectToNas(ipAddress = ipAddress)
                }
            }
        }
    }

    sealed interface LoginScreenAction {
        data class UpdateIpAddress(val ipAddress: String) : LoginScreenAction
        data object Connect : LoginScreenAction
    }

    data class LoginScreenUiState(val ipAddress: String)
}