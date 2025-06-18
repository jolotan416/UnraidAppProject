package com.jolotan.unraidapp.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.ui.viewmodels.dashboard.DashboardScreenViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import unraidappproject.composeapp.generated.resources.Res
import unraidappproject.composeapp.generated.resources.generic_loading_text

@Composable
fun DashboardScreen() {
    val dashboardScreenViewModel: DashboardScreenViewModel = koinViewModel()
    val dashboardScreenUiState by dashboardScreenViewModel.dashboardScreenUiStateStateFlow.collectAsStateWithLifecycle()
    when (val uiState = dashboardScreenUiState) {
        GenericState.Loading -> {
            Column(
                modifier = Modifier.fillMaxWidth().padding(all = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(Res.string.generic_loading_text))
                CircularProgressIndicator()
            }
        }

        is GenericState.Loaded -> {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth().padding(all = 20.dp),
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(all = 8.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
                        }
                    }
                }
            }
        }

        is GenericState.Error -> {

        }
    }
}