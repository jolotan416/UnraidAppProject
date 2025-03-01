package com.jolotan.unraidapp.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.PlatformConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class LoginScreenViewModel(platformConfig: PlatformConfig) : ViewModel() {
    private val ipAddressStateFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val portStateFlow: MutableStateFlow<String> = MutableStateFlow("")
    val uiStateStateFlow: StateFlow<GenericState<UiState, Exception>> =
        combine(ipAddressStateFlow, portStateFlow) { ipAddress, port ->
            GenericState.Loaded(UiState(ipAddress, port))
        }.stateIn(viewModelScope, SharingStarted.Eagerly, GenericState.Loading)

    init {
        println("Starting in ${platformConfig.name}!!!")
    }

    fun handleAction(action: LoginScreenAction) {
        when (action) {
            is LoginScreenAction.UpdateIpAddress -> {
                ipAddressStateFlow.value = action.ipAddress
            }

            is LoginScreenAction.UpdatePort -> {
                portStateFlow.value = action.port
            }

            is LoginScreenAction.Connect -> {
                println("Connect to NAS with IP: ${ipAddressStateFlow.value} and port: ${portStateFlow.value}")
            }
        }
    }

    sealed interface LoginScreenAction {
        data class UpdateIpAddress(val ipAddress: String) : LoginScreenAction
        data class UpdatePort(val port: String) : LoginScreenAction
        data object Connect : LoginScreenAction
    }

    data class UiState(
        val ipAddress: String,
        val port: String,
    )
}