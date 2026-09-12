@file:OptIn(ExperimentalTime::class)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.datepicker.DateBounds
import com.mohamedrejeb.calf.ui.datepicker.UIKitDisplayMode
import com.mohamedrejeb.calf.ui.datepicker.and
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val RANGE_LENGTH_DAYS = 30

private enum class RangeOption(val label: String) {
    AnyDate("Any date"),
    FromToday("From today"),
    NextThirtyDays("Next $RANGE_LENGTH_DAYS days"),
}

/** Rejects Saturdays and Sundays; a stable object so the pickers are not re-evaluated needlessly. */
@OptIn(ExperimentalMaterial3Api::class)
private object WeekdaysOnly : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val dayOfWeek = Instant.fromEpochMilliseconds(utcTimeMillis)
            .toLocalDateTime(TimeZone.UTC)
            .date
            .dayOfWeek
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerScreen(
    navigateBack: () -> Unit
) {
    var rangeOption by remember { mutableStateOf(RangeOption.AnyDate) }
    var weekdaysOnly by remember { mutableStateOf(false) }

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val todayMillis = remember(today) {
        today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    }
    val endOfRangeMillis = remember(today) {
        today.plus(RANGE_LENGTH_DAYS, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    }

    val selectableDates: SelectableDates = remember(rangeOption, weekdaysOnly, todayMillis, endOfRangeMillis) {
        val bounds: SelectableDates = when (rangeOption) {
            RangeOption.AnyDate -> DatePickerDefaults.AllDates
            RangeOption.FromToday -> DateBounds(minDateMillis = todayMillis)
            RangeOption.NextThirtyDays -> DateBounds(minDateMillis = todayMillis, maxDateMillis = endOfRangeMillis)
        }
        if (weekdaysOnly) bounds and WeekdaysOnly else bounds
    }

    val inlineState = rememberAdaptiveDatePickerState(selectableDates = selectableDates)
    val wheelsState = rememberAdaptiveDatePickerState(
        initialUIKitDisplayMode = UIKitDisplayMode.Wheels,
        selectableDates = selectableDates,
    )

    SampleScreenScaffold(
        title = "Adaptive Date Picker",
        navigateBack = navigateBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Date selection using the native iOS controls and the Material3 DatePicker. " +
                    "The controls below restrict which days can be picked on both platforms.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Selectable range",
                style = MaterialTheme.typography.labelLarge,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                RangeOption.entries.forEachIndexed { index, option ->
                    FilterChip(
                        selected = rangeOption == option,
                        onClick = { rangeOption = option },
                        label = { Text(option.label) },
                    )
                    if (index < RangeOption.entries.lastIndex) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Weekdays only",
                    modifier = Modifier.weight(1f),
                )
                AdaptiveSwitch(
                    checked = weekdaysOnly,
                    onCheckedChange = { weekdaysOnly = it },
                )
            }

            SectionDivider()

            SectionTitle(
                title = "Inline picker",
                description = "The full calendar, always visible.",
            )

            Text(
                text = "Selected: ${inlineState.selectedDateMillis.toDateLabel()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            AdaptiveDatePicker(
                state = inlineState,
                colors = sampleDatePickerColors(),
            )

            if (currentPlatform.isIOS) {
                SectionDivider()

                SectionTitle(
                    title = "Wheels picker (iOS)",
                    description = "UIDatePicker wheels cannot grey out days: a rejected pick snaps " +
                        "to the nearest selectable day.",
                )

                Text(
                    text = "Selected: ${wheelsState.selectedDateMillis.toDateLabel()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(8.dp))

                AdaptiveDatePicker(
                    state = wheelsState,
                    colors = sampleDatePickerColors(),
                )
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun SectionTitle(title: String, description: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun sampleDatePickerColors(): DatePickerColors =
    DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
    )

private fun Long?.toDateLabel(): String =
    this?.let { millis ->
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date.toString()
    } ?: "None"
