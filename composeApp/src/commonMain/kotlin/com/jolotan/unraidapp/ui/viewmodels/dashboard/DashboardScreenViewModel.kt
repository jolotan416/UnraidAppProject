package com.jolotan.unraidapp.ui.viewmodels.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.api.BackendConnectionError
import com.jolotan.unraidapp.data.api.UnraidNasQueryApi
import com.jolotan.unraidapp.data.models.backend.DashboardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardScreenViewModel(private val queryApi: UnraidNasQueryApi) : ViewModel() {
    private val dashboardDataStateFlow: MutableStateFlow<GenericState<DashboardData, BackendConnectionError>> =
        MutableStateFlow(GenericState.Loading)
    val dashboardScreenUiStateStateFlow: StateFlow<GenericState<DashboardData, BackendConnectionError>> =
        dashboardDataStateFlow

    init {
        handleAction(DashboardScreenAction.LoadDashboardData)
    }

    fun handleAction(action: DashboardScreenAction) {
        viewModelScope.launch {
            when (action) {
                DashboardScreenAction.LoadDashboardData -> {
                    dashboardDataStateFlow.value = queryApi.queryDashboardData()
                }
            }
        }
    }

    sealed interface DashboardScreenAction {
        data object LoadDashboardData : DashboardScreenAction
    }
}