@file:OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveCompactDatePicker
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDateRangePicker
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDateRangePickerState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val NOT_SET = "Not set"
private const val MILLIS_PER_DAY = 86_400_000L
private const val SHORT_NAME_LENGTH = 3

@Composable
internal fun DepartureSection(state: AdaptiveDatePickerState) {
    DemoSection(
        title = "Flight",
        footer = "AdaptiveCompactDatePicker: a field that opens the calendar on tap.",
    ) {
        DemoListCard {
            DemoRow(
                title = "Departure",
                subtitle = "Lisbon to Porto",
                leading = { IconTile(icon = Icons.Outlined.FlightTakeoff, color = TileBlue) },
                trailing = {
                    AdaptiveCompactDatePicker(
                        state = state,
                        colors = sampleDatePickerColors(),
                        materialPlaceholder = "Pick a day",
                    )
                },
            )
        }
    }
}

@Composable
internal fun StaySection(state: AdaptiveDateRangePickerState) {
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis
    DemoSection(
        title = "Stay",
        footer = "Tap the check-in day, then the check-out day. Tap again to start a new range.",
    ) {
        PickerCard(
            readouts = {
                ValueRow(label = "Check-in", value = start.toDateLabel())
                ValueRow(label = "Check-out", value = end.toDateLabel())
                ValueRow(label = "Nights", value = nightsLabel(start, end))
            },
        ) {
            AdaptiveDateRangePicker(
                state = state,
                colors = sampleDatePickerColors(),
                showModeToggle = false,
            )
        }
    }
}

@Composable
internal fun TourSection(state: AdaptiveDatePickerState) {
    DemoSection(
        title = "City tour",
        footer = "AdaptiveDatePicker: the full calendar, always visible.",
    ) {
        PickerCard(
            readouts = { ValueRow(label = "Tour day", value = state.selectedDateMillis.toDateLabel()) },
        ) {
            AdaptiveDatePicker(
                state = state,
                colors = sampleDatePickerColors(),
                showModeToggle = false,
            )
        }
    }
}

/** iOS only: Material has no wheels, so the page shows it only where it exists. */
@Composable
internal fun ReturnFlightSection(state: AdaptiveDatePickerState) {
    DemoSection(
        title = "Return flight",
        footer = "UIDatePicker wheels cannot grey out days: a rejected pick snaps to the nearest " +
            "selectable day.",
    ) {
        PickerCard(
            readouts = { ValueRow(label = "Return", value = state.selectedDateMillis.toDateLabel()) },
        ) {
            AdaptiveDatePicker(
                state = state,
                colors = sampleDatePickerColors(),
                showModeToggle = false,
            )
        }
    }
}

/** A card with the live [readouts] on top and the picker below, on the card's own colour. */
@Composable
private fun PickerCard(
    readouts: @Composable () -> Unit,
    picker: @Composable () -> Unit,
) {
    DemoCard(
        contentPadding = PaddingValues(),
        verticalArrangement = Arrangement.Top,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.l),
        ) {
            readouts()
        }
        DemoDivider(startIndent = Spacing.l)
        picker()
    }
}

/** The pickers sit on the card, so they take its colour instead of Material's surface. */
@Composable
private fun sampleDatePickerColors(): DatePickerColors =
    DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    )

private fun nightsLabel(start: Long?, end: Long?): String =
    if (start != null && end != null) ((end - start) / MILLIS_PER_DAY).toString() else NOT_SET

/** "Sat, Oct 3, 2026": the picked day, read in UTC like the pickers report it. */
private fun Long?.toDateLabel(): String =
    this?.let { millis ->
        val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
        "${date.dayOfWeek.shortName()}, ${date.month.shortName()} ${date.day}, ${date.year}"
    } ?: NOT_SET

private fun DayOfWeek.shortName(): String = name.toShortName()

private fun Month.shortName(): String = name.toShortName()

private fun String.toShortName(): String =
    take(SHORT_NAME_LENGTH).lowercase().replaceFirstChar { it.uppercase() }
