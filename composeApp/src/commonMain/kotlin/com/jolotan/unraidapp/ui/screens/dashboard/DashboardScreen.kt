package com.jolotan.unraidapp.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.models.backend.DashboardArrayCapacityData
import com.jolotan.unraidapp.data.models.backend.DashboardArraySizeData
import com.jolotan.unraidapp.data.models.backend.DashboardDiskData
import com.jolotan.unraidapp.data.models.backend.DashboardRegistrationData
import com.jolotan.unraidapp.data.models.backend.DashboardServerData
import com.jolotan.unraidapp.ui.utils.toDateOrNull
import com.jolotan.unraidapp.ui.utils.toFormattedFileSize
import com.jolotan.unraidapp.ui.viewmodels.dashboard.DashboardScreenViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import unraidappproject.composeapp.generated.resources.Res
import unraidappproject.composeapp.generated.resources.array_content_description
import unraidappproject.composeapp.generated.resources.array_disk_size
import unraidappproject.composeapp.generated.resources.expires_at
import unraidappproject.composeapp.generated.resources.generic_loading_text
import unraidappproject.composeapp.generated.resources.ic_rack_server
import unraidappproject.composeapp.generated.resources.unraid_registration

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
            LazyVerticalStaggeredGrid(
                modifier = Modifier.fillMaxWidth().padding(all = 20.dp),
                columns = StaggeredGridCells.Fixed(2),
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    DashboardHeader(
                        serverData = uiState.value.serverData,
                        registrationData = uiState.value.registrationData
                    )
                }
                item {
                    DashboardArray(
                        arrayCapacityData = uiState.value.arrayData.arrayCapacityData,
                        disks = uiState.value.arrayData.parityDisks + uiState.value.arrayData.disks
                    )
                }
            }
        }

        is GenericState.Error -> {

        }
    }
}

@Composable
fun DashboardHeader(serverData: DashboardServerData, registrationData: DashboardRegistrationData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = serverData.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(
                Res.string.unraid_registration,
                registrationData.registrationType,
            ),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        registrationData.updateExpiration.toDateOrNull()?.let {
            Text(
                text = stringResource(
                    Res.string.expires_at,
                    it.format(LocalDateTime.Format {
                        monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); dayOfMonth();
                        chars(", "); year()
                        char(' ')

                        amPmHour(); char(':'); minute();
                        char(' '); amPmMarker("am", "pm")
                    })
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun DashboardArray(arrayCapacityData: DashboardArrayCapacityData, disks: List<DashboardDiskData>) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)
            .defaultMinSize(minWidth = 200.dp)
            .padding(all = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_rack_server),
                contentDescription = stringResource(Res.string.array_content_description),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    text = stringResource(
                        Res.string.array_disk_size,
                        arrayCapacityData.arrayDiskSpaceSize
                            .used
                            .toUInt()
                            .toFormattedFileSize(),
                        arrayCapacityData.arrayDiskSpaceSize
                            .total
                            .toUInt()
                            .toFormattedFileSize()
                    )
                )
                LinearProgressIndicator(
                    progress = arrayCapacityData.arrayDiskSpaceSize.used.toFloat() / arrayCapacityData.arrayDiskSpaceSize.total.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }

        disks.forEach {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = it.name)
                Text(text = it.status)
                Text(text = it.size.toUInt().toFormattedFileSize())
                Text(text = it.temperature.toString())
            }
        }
    }
}

@Composable
@Preview
fun DashboardHeaderPreview() {
    DashboardHeader(
        DashboardServerData("This is a very long NAS label"), DashboardRegistrationData(
            "STARTER",
            "1756656402335"
        )
    )
}

@Composable
@Preview
fun DashboardArrayPreview() {
    DashboardArray(
        arrayCapacityData = DashboardArrayCapacityData(
            arrayDiskSpaceSize = DashboardArraySizeData("68855000", "69000000", "14500"),
            arrayDiskSlotsSize = DashboardArraySizeData("1", "24", "23")
        ),
        disks = listOf(
            DashboardDiskData(
                name = "Parity 1",
                status = "Healthy",
                temperature = 36,
                size = "1230000000"
            ),
            DashboardDiskData(
                name = "Disk 1",
                status = "Not Healthy",
                temperature = 60,
                size = "1230000000"
            )
        )
    )
}