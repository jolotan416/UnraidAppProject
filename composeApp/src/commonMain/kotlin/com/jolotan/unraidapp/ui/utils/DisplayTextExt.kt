package com.jolotan.unraidapp.ui.utils

import androidx.compose.runtime.Composable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import unraidappproject.composeapp.generated.resources.Res
import unraidappproject.composeapp.generated.resources.disk_size_gb
import unraidappproject.composeapp.generated.resources.disk_size_kb
import unraidappproject.composeapp.generated.resources.disk_size_mb
import unraidappproject.composeapp.generated.resources.disk_size_tb
import unraidappproject.composeapp.generated.resources.few_hours
import unraidappproject.composeapp.generated.resources.few_minutes
import unraidappproject.composeapp.generated.resources.few_seconds
import unraidappproject.composeapp.generated.resources.last_week
import unraidappproject.composeapp.generated.resources.yesterday
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val KB_IN_MB = 1000U
private const val KB_IN_GB = 1000000U
private const val KB_IN_TB = 1000000000U
private const val TWO_DECIMAL_FORMATTER = 100f
private val UI_DATE_FORMAT = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    chars(", ")
    year()
}

@OptIn(ExperimentalTime::class)
fun String.convertTimestampToDateOrNull(): LocalDateTime? = toLongOrNull()?.let {
    Instant.fromEpochMilliseconds(it)
        .toLocalDateTime(TimeZone.currentSystemDefault())
}

@OptIn(ExperimentalTime::class)
@Composable
fun LocalDateTime.convertToDurationString(): String {
    val durationSinceApiDate = Clock.System.now() - toInstant(TimeZone.currentSystemDefault())

    return when {
        durationSinceApiDate.inWholeNanoseconds < DateTimeUnit.MINUTE.nanoseconds -> stringResource(
            Res.string.few_seconds
        )

        durationSinceApiDate.inWholeNanoseconds < DateTimeUnit.HOUR.nanoseconds -> stringResource(
            Res.string.few_minutes
        )

        durationSinceApiDate.inWholeDays < DateTimeUnit.DAY.days -> stringResource(Res.string.few_hours)
        durationSinceApiDate.inWholeDays < (DateTimeUnit.DAY * 2).days -> stringResource(Res.string.yesterday)
        durationSinceApiDate.inWholeDays < DateTimeUnit.WEEK.days -> stringResource(Res.string.last_week)
        else -> format(UI_DATE_FORMAT)
    }
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