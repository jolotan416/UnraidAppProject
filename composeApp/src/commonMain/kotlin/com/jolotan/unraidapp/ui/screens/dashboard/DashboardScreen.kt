package com.jolotan.unraidapp.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.models.backend.DashboardArrayCapacityData
import com.jolotan.unraidapp.data.models.backend.DashboardArraySizeData
import com.jolotan.unraidapp.data.models.backend.DashboardData
import com.jolotan.unraidapp.data.models.backend.DashboardDiskData
import com.jolotan.unraidapp.data.models.backend.DashboardRegistrationData
import com.jolotan.unraidapp.data.models.backend.DashboardServerData
import com.jolotan.unraidapp.data.models.backend.DashboardShareData
import com.jolotan.unraidapp.ui.components.LinearProgressIndicatorWithText
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
import unraidappproject.composeapp.generated.resources.disk_total
import unraidappproject.composeapp.generated.resources.expires_at
import unraidappproject.composeapp.generated.resources.generic_loading_text
import unraidappproject.composeapp.generated.resources.ic_rack_server
import unraidappproject.composeapp.generated.resources.text_limit
import unraidappproject.composeapp.generated.resources.unraid_registration

private const val SHARE_ITEM_NAME_CHARACTER_LIMIT = 30

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
            LoadedDashboardScreen(uiState.value)
        }

        is GenericState.Error -> {

        }
    }
}

@Composable
fun LoadedDashboardScreen(dashboardData: DashboardData) {
    BoxWithConstraints {
        val columnCount = when (maxWidth) {
            in 0.dp..600.dp -> 1
            in 601.dp..1200.dp -> 2
            else -> 3
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(count = columnCount),
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 20.dp),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                DashboardHeader(dashboardData.serverData, dashboardData.registrationData)
            }

            val disks = dashboardData.arrayData.disks + dashboardData.arrayData.parityDisks
            if (disks.isNotEmpty()) {
                item {
                    DashboardArray(
                        dashboardData.arrayData.arrayCapacityData,
                        dashboardData.arrayData.disks + dashboardData.arrayData.parityDisks
                    )
                }
            }

            if (dashboardData.sharesData.isNotEmpty()) {
                item {
                    DashboardShares(dashboardData.sharesData)
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(
    serverData: DashboardServerData,
    registrationData: DashboardRegistrationData
) {
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
fun DashboardArray(
    arrayCapacityData: DashboardArrayCapacityData,
    disks: List<DashboardDiskData>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(all = 20.dp),
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
                LinearProgressIndicatorWithText(
                    modifier = Modifier.weight(1f),
                    progress = { arrayCapacityData.arrayDiskSpaceSize.used.toFloat() / arrayCapacityData.arrayDiskSpaceSize.total.toFloat() },
                    numeratorText = arrayCapacityData.arrayDiskSpaceSize
                        .used
                        .toUInt().toFormattedFileSize(),
                    denominatorText = stringResource(
                        Res.string.disk_total, arrayCapacityData.arrayDiskSpaceSize
                            .total
                            .toUInt().toFormattedFileSize()
                    )
                )
            }

            disks.forEach {
                DashboardArrayDiskItem(it)
            }
        }
    }
}

@Composable
fun DashboardArrayDiskItem(disks: DashboardDiskData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = disks.name)
        Text(text = disks.status)
        Text(text = disks.size.toUInt().toFormattedFileSize())
        Text(text = "${disks.temperature}°C")
    }
}

@Composable
fun DashboardShares(shares: List<DashboardShareData>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            shares.forEach { share ->
                DashboardShareItem(share)
            }
        }
    }
}

@Composable
fun DashboardShareItem(shareData: DashboardShareData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val shareItemText =
            if (shareData.name.length <= SHARE_ITEM_NAME_CHARACTER_LIMIT) shareData.name
            else stringResource(Res.string.text_limit, shareData.name)
        val usedSize = shareData.used.toUInt()
        val freeSize = shareData.free.toUInt()
        val totalSize = usedSize + freeSize

        Text(text = shareItemText)
        LinearProgressIndicatorWithText(
            modifier = Modifier.weight(1f),
            progress = { usedSize.toFloat() / totalSize.toFloat() },
            numeratorText = usedSize.toFormattedFileSize(),
            denominatorText = stringResource(Res.string.disk_total, totalSize.toFormattedFileSize())
        )
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

@Composable
@Preview
fun DashboardSharesPreview() {
    DashboardShares(
        shares = listOf(
            DashboardShareData(
                name = "Games",
                used = "68855000",
                free = "5000"
            ),
            DashboardShareData(
                name = "A very very long name that would go beyond limit",
                used = "1",
                free = "24"
            ),
            DashboardShareData(
                name = "A very very long name that would go beyond limit",
                used = "69",
                free = "0"
            )
        )
    )
}