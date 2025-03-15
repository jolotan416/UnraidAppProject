package com.jolotan.unraidapp.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.models.PlatformConfig
import com.jolotan.unraidapp.data.repositories.NasDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConnectScreenViewModel(
    platformConfig: PlatformConfig,
    private val nasDataRepository: NasDataRepository,
) : ViewModel() {
    private val ipAddressStateFlow: MutableStateFlow<String> = MutableStateFlow("")
    val loginScreenUiStateStateFlow: StateFlow<GenericState<LoginScreenUiState, Exception>> =
        nasDataRepository.getNasConnectionDataFlow()
            .onEach {
                ipAddressStateFlow.value = it.firstOrNull()?.ipAddress ?: ""
            }.combine(ipAddressStateFlow) { _, ipAddress ->
                GenericState.Loaded(LoginScreenUiState(ipAddress))
            }.stateIn(viewModelScope, SharingStarted.Eagerly, GenericState.Loading)

    init {
        println("Starting in ${platformConfig.name}!!!")
    }

    fun handleAction(action: LoginScreenAction) {
        when (action) {
            is LoginScreenAction.UpdateIpAddress -> {
                ipAddressStateFlow.value = action.ipAddress
            }

            is LoginScreenAction.Connect -> {
                println("Connect to NAS with IP: ${ipAddressStateFlow.value}")
                viewModelScope.launch {
                    nasDataRepository.connectToNas(ipAddress = ipAddressStateFlow.value)
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