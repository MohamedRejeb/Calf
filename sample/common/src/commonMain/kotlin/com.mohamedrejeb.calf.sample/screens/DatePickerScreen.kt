@file:OptIn(ExperimentalTime::class)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.ui.datepicker.DateBounds
import com.mohamedrejeb.calf.ui.datepicker.UIKitDisplayMode
import com.mohamedrejeb.calf.ui.datepicker.and
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDateRangePickerState
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

private const val USAGE = """
val state = rememberAdaptiveDatePickerState(
    // Bookable from today, never on a weekend.
    selectableDates = DateBounds(minDateMillis = todayMillis) and WeekdaysOnly,
)

AdaptiveDatePicker(state = state)          // the full calendar
AdaptiveCompactDatePicker(state = state)   // a field that opens it

val selected: Long? = state.selectedDateMillis
"""

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
fun DatePickerScreen(navigateBack: () -> Unit) {
    var rangeOption by rememberSaveable { mutableStateOf(RangeOption.AnyDate) }
    var weekdaysOnly by rememberSaveable { mutableStateOf(false) }
    val selectableDates = rememberSelectableDates(rangeOption, weekdaysOnly)

    // Every picker on the page shares the same rule, so the options above restrict them all.
    val departureState = rememberAdaptiveDatePickerState(selectableDates = selectableDates)
    val stayState = rememberAdaptiveDateRangePickerState(selectableDates = selectableDates)
    val tourState = rememberAdaptiveDatePickerState(selectableDates = selectableDates)
    val returnState = rememberAdaptiveDatePickerState(
        initialUIKitDisplayMode = UIKitDisplayMode.Wheels,
        selectableDates = selectableDates,
    )

    SampleScreenScaffold(title = Screen.DatePicker.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.DatePicker)
            BookableDaysSection(
                rangeOption = rangeOption,
                onRangeOptionChange = { rangeOption = it },
                weekdaysOnly = weekdaysOnly,
                onWeekdaysOnlyChange = { weekdaysOnly = it },
            )
            DepartureSection(state = departureState)
            StaySection(state = stayState)
            TourSection(state = tourState)
            if (currentPlatform.isIOS) {
                ReturnFlightSection(state = returnState)
            }
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

/** The rule of the chosen options: a [DateBounds] for the range, combined with [WeekdaysOnly]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberSelectableDates(rangeOption: RangeOption, weekdaysOnly: Boolean): SelectableDates {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val todayMillis = remember(today) { today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() }
    val endOfRangeMillis = remember(today) {
        today.plus(RANGE_LENGTH_DAYS, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    }
    return remember(rangeOption, weekdaysOnly, todayMillis, endOfRangeMillis) {
        val bounds: SelectableDates = when (rangeOption) {
            RangeOption.AnyDate -> DatePickerDefaults.AllDates
            RangeOption.FromToday -> DateBounds(minDateMillis = todayMillis)
            RangeOption.NextThirtyDays -> DateBounds(minDateMillis = todayMillis, maxDateMillis = endOfRangeMillis)
        }
        if (weekdaysOnly) bounds and WeekdaysOnly else bounds
    }
}

@Composable
private fun BookableDaysSection(
    rangeOption: RangeOption,
    onRangeOptionChange: (RangeOption) -> Unit,
    weekdaysOnly: Boolean,
    onWeekdaysOnlyChange: (Boolean) -> Unit,
) {
    DemoSection(
        title = "Bookable days",
        footer = "Every picker on this page follows these rules: a DateBounds for the range, " +
            "combined with a weekdays rule through and(). Days outside are greyed out.",
    ) {
        DemoListCard {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.l, vertical = Spacing.s),
            ) {
                RangeOption.entries.forEach { option ->
                    FilterChip(
                        selected = rangeOption == option,
                        onClick = { onRangeOptionChange(option) },
                        label = { Text(option.label) },
                    )
                }
            }
            DemoDivider()
            DemoRow(
                title = "Weekdays only",
                leading = { IconTile(icon = Icons.Outlined.EventBusy, color = TileAmber) },
                trailing = { AdaptiveSwitch(checked = weekdaysOnly, onCheckedChange = onWeekdaysOnlyChange) },
            )
        }
    }
}

