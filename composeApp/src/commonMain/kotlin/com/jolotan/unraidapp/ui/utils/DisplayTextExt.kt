package com.jolotan.unraidapp.ui.utils

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import unraidappproject.composeapp.generated.resources.Res
import unraidappproject.composeapp.generated.resources.disk_size_gb
import unraidappproject.composeapp.generated.resources.disk_size_kb
import unraidappproject.composeapp.generated.resources.disk_size_mb
import unraidappproject.composeapp.generated.resources.disk_size_tb
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val KB_IN_MB = 1000U
private const val KB_IN_GB = 1000000U
private const val KB_IN_TB = 1000000000U
private const val TWO_DECIMAL_FORMATTER = 100f

@OptIn(ExperimentalTime::class)
fun String.toDateOrNull(): LocalDateTime? = toLongOrNull()?.let {
    Instant.fromEpochMilliseconds(it)
        .toLocalDateTime(TimeZone.currentSystemDefault())
}

@Composable
fun UInt.toFormattedFileSize(): String =
    when {
        (this / KB_IN_TB) > 0U -> stringResource(
            Res.string.disk_size_tb,
            (this.toFloat() / KB_IN_TB.toFloat()).toTwoDecimalFormat()
        )

        (this / KB_IN_GB) > 0U -> stringResource(
            Res.string.disk_size_gb,
            (this.toFloat() / KB_IN_GB.toFloat()).toTwoDecimalFormat()
        )

        (this / KB_IN_MB) > 0U -> stringResource(
            Res.string.disk_size_mb,
            (this.toFloat() / KB_IN_MB.toFloat()).toTwoDecimalFormat()
        )

        else -> stringResource(
            Res.string.disk_size_kb,
            this.toFloat().toTwoDecimalFormat()
        )
    }

fun Float.toTwoDecimalFormat(): String =
    ((this * TWO_DECIMAL_FORMATTER).roundToInt() / TWO_DECIMAL_FORMATTER).toString()