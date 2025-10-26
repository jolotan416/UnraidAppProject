package com.jolotan.unraidapp.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.models.backend.DashboardArrayCapacityData
import com.jolotan.unraidapp.data.models.backend.DashboardArraySizeData
import com.jolotan.unraidapp.data.models.backend.DashboardData
import com.jolotan.unraidapp.data.models.backend.DashboardDiskData
import com.jolotan.unraidapp.data.models.backend.DashboardDockerContainerAdditionalData
import com.jolotan.unraidapp.data.models.backend.DashboardDockerContainerData
import com.jolotan.unraidapp.data.models.backend.DashboardParityData
import com.jolotan.unraidapp.data.models.backend.DashboardRegistrationData
import com.jolotan.unraidapp.data.models.backend.DashboardServerData
import com.jolotan.unraidapp.data.models.backend.DashboardShareData
import com.jolotan.unraidapp.ui.components.LinearProgressIndicatorWithText
import com.jolotan.unraidapp.ui.utils.convertTimestampToDateOrNull
import com.jolotan.unraidapp.ui.utils.convertToDurationString
import com.jolotan.unraidapp.ui.utils.toFormattedFileSize
import com.jolotan.unraidapp.ui.viewmodels.dashboard.DashboardScreenViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import unraidappproject.composeapp.generated.resources.Res
import unraidappproject.composeapp.generated.resources.array_content_description
import unraidappproject.composeapp.generated.resources.disks_title
import unraidappproject.composeapp.generated.resources.docker_containers_title
import unraidappproject.composeapp.generated.resources.expires_at
import unraidappproject.composeapp.generated.resources.generic_error_text
import unraidappproject.composeapp.generated.resources.generic_loading_text
import unraidappproject.composeapp.generated.resources.ic_rack_server
import unraidappproject.composeapp.generated.resources.last_parity_check_done
import unraidappproject.composeapp.generated.resources.no_parity_checks_done
import unraidappproject.composeapp.generated.resources.reload
import unraidappproject.composeapp.generated.resources.shares_title
import unraidappproject.composeapp.generated.resources.text_limit
import unraidappproject.composeapp.generated.resources.unraid_registration
import unraidappproject.composeapp.generated.resources.used_disk_size
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val SHARE_ITEM_NAME_CHARACTER_LIMIT = 30

@Composable
fun DashboardScreen() {
    val dashboardScreenViewModel: DashboardScreenViewModel = koinViewModel()
    val dashboardScreenUiState by dashboardScreenViewModel.dashboardScreenUiStateStateFlow.collectAsStateWithLifecycle()
    when (val uiState = dashboardScreenUiState) {
        GenericState.Loading -> {
            LoadingDashboardScreen()
        }

        is GenericState.Loaded -> {
            LoadedDashboardScreen(uiState.value)
        }

        is GenericState.Error -> {
            ErrorDashboardScreen {
                dashboardScreenViewModel.handleAction(DashboardScreenViewModel.DashboardScreenAction.LoadDashboardData)
            }
        }
    }
}

@Preview
@Composable
fun LoadingDashboardScreen() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(all = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.generic_loading_text),
            style = MaterialTheme.typography.bodyMedium
        )
        CircularProgressIndicator()
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
            verticalItemSpacing = 12.dp,
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
                        dashboardData.parityHistory,
                        dashboardData.arrayData.disks + dashboardData.arrayData.parityDisks
                    )
                }
            }

            if (dashboardData.sharesData.isNotEmpty()) {
                item {
                    DashboardShares(dashboardData.sharesData)
                }
            }

            val dockerContainers =
                dashboardData.dockerData.containers.filter { it.names.isNotEmpty() }
            if (dockerContainers.isNotEmpty()) {
                item {
                    DashboardDockerContainers(dockerContainers)
                }
            }
        }
    }
}

@Composable
fun ErrorDashboardScreen(reload: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(all = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.generic_error_text),
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = reload) {
            Text(text = stringResource(Res.string.reload))
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
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(
                Res.string.unraid_registration,
                registrationData.registrationType,
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        registrationData.updateExpiration.convertTimestampToDateOrNull()?.let {
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
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun DashboardArray(
    arrayCapacityData: DashboardArrayCapacityData,
    parityHistory: List<DashboardParityData>,
    disks: List<DashboardDiskData>,
) {
    DashboardCardItem(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(Res.string.disks_title)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    text = stringResource(
                        Res.string.used_disk_size,
                        arrayCapacityData.arrayDiskSpaceSize
                            .used
                            .toUInt().toFormattedFileSize(),
                        arrayCapacityData.arrayDiskSpaceSize
                            .total
                            .toUInt().toFormattedFileSize()
                    ),
                )
            }

            val lastSuccessfulParity =
                parityHistory.filter { parityData -> parityData.status == "OK" }
            if (!lastSuccessfulParity.isEmpty()) {
                Text(
                    text = stringResource(
                        Res.string.last_parity_check_done,
                        lastSuccessfulParity.first().date.convertToDurationString()
                    ),
                    style = MaterialTheme.typography.titleSmall
                )
            } else {
                Text(
                    text = stringResource(Res.string.no_parity_checks_done),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

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
        BasicText(
            modifier = Modifier.weight(1f),
            text = disks.name,
            overflow = TextOverflow.Ellipsis
        )
        Text(text = disks.status)
        Text(text = disks.size.toUInt().toFormattedFileSize())
        Text(text = "${disks.temperature}°C")
    }
}

@Composable
fun DashboardShares(shares: List<DashboardShareData>) {
    DashboardCardItem(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(Res.string.shares_title)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val shareItemText =
            if (shareData.name.length <= SHARE_ITEM_NAME_CHARACTER_LIMIT) shareData.name
            else stringResource(Res.string.text_limit, shareData.name)
        val usedSize = shareData.used.toUInt()
        val freeSize = shareData.free.toUInt()
        val totalSize = usedSize + freeSize

        BasicText(
            text = shareItemText,
            overflow = TextOverflow.Ellipsis
        )
        LinearProgressIndicatorWithText(
            modifier = Modifier.weight(1f),
            progress = { usedSize.toFloat() / totalSize.toFloat() },
            text = stringResource(
                Res.string.used_disk_size,
                usedSize.toFormattedFileSize(),
                totalSize.toFormattedFileSize()
            ),
        )
    }
}

@Composable
fun DashboardDockerContainers(dockerContainers: List<DashboardDockerContainerData>) {
    DashboardCardItem(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(Res.string.docker_containers_title)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            dockerContainers.forEach { dockerContainer ->
                DashboardDockerContainerItem(dockerContainerData = dockerContainer)
            }
        }
    }
}

@Composable
fun DashboardDockerContainerItem(dockerContainerData: DashboardDockerContainerData) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AsyncImage(
            modifier = Modifier.size(24.dp),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(dockerContainerData.additionalData.icon)
                .build(),
            contentDescription = dockerContainerData.names.first(),
        )
        BasicText(
            modifier = Modifier.weight(1f),
            text = dockerContainerData.names.first().removePrefix("/"),
            overflow = TextOverflow.Ellipsis
        )
        Text(text = dockerContainerData.state)
    }
}

@Composable
fun DashboardCardItem(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            content()
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

@OptIn(ExperimentalTime::class)
@Composable
@Preview
fun DashboardArrayPreview() {
    DashboardArray(
        arrayCapacityData = DashboardArrayCapacityData(
            arrayDiskSpaceSize = DashboardArraySizeData("68855000", "69000000", "14500"),
            arrayDiskSlotsSize = DashboardArraySizeData("1", "24", "23")
        ),
        parityHistory = listOf(
            DashboardParityData(
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "OK"
            )
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
                name = "share_1",
                used = "68855000",
                free = "5000"
            ),
            DashboardShareData(
                name = "share_2",
                used = "1",
                free = "24"
            ),
            DashboardShareData(
                name = "share_3_has_a_very_very_long_name_here",
                used = "69",
                free = "0"
            )
        )
    )
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
fun DashboardDockerContainersPreview() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(Color.Red.toArgb())
    }

    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        DashboardDockerContainers(
            dockerContainers = listOf(
                DashboardDockerContainerData(
                    names = listOf("/nginx-webserver"),
                    state = "RUNNING",
                    additionalData = DashboardDockerContainerAdditionalData("https://raw.githubusercontent.com/linuxserver/docker-templates/master/linuxserver.io/img/mariadb-logo.png")
                ),
                DashboardDockerContainerData(
                    names = listOf("pihole-dns", "dns-blocker"),
                    state = "EXITED",
                    additionalData = DashboardDockerContainerAdditionalData(null)
                )
            )
        )
    }
}

@Preview
@Composable
fun ErrorDashboardScreenPreview() {
    ErrorDashboardScreen { }
}