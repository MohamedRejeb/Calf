package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A date range picker that adapts to the platform it is running on.
 *
 * On Material platforms it is the Material3 `DateRangePicker`. On iOS 16+ it is a native
 * `UICalendarView`: the first tap picks the start, a tap on or after it picks the end, a tap
 * before it moves the start, and any tap on a completed range starts a new one. Below iOS 16
 * the Material3 picker is shown.
 *
 * @param state the state that holds the selected range and the selectable dates.
 * @param modifier the modifier applied to the picker.
 * @param dateFormatter formats dates on Material platforms.
 * @param title the title slot on Material platforms; hidden when `null`.
 * @param headline the headline slot on Material platforms; hidden when `null`.
 * @param showModeToggle whether Material platforms offer the text input mode.
 * @param colors colors of the picker; the selected day color tints the native iOS calendar.
 *
 * On Material platforms the picker scrolls through months, so it needs a bounded height. When
 * the parent gives none, for example inside a vertically scrolling column, it falls back to
 * [DateRangePickerFallbackHeight]; pass a `height` modifier to choose your own.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun AdaptiveDateRangePicker(
    state: AdaptiveDateRangePickerState,
    modifier: Modifier = Modifier,
    dateFormatter: DatePickerFormatter = remember { DatePickerDefaults.dateFormatter() },
    title: (@Composable () -> Unit)? = null,
    headline: (@Composable () -> Unit)? = null,
    showModeToggle: Boolean = true,
    colors: DatePickerColors = DatePickerDefaults.colors(),
)

/** Height of the Material range picker when its parent gives no vertical bound. */
val DateRangePickerFallbackHeight: Dp = 568.dp

/** Paddings of the Material3 range picker defaults, which Material3 keeps private. */
internal val DateRangePickerTitlePadding = PaddingValues(start = 64.dp, end = 12.dp)
internal val DateRangePickerHeadlinePadding = PaddingValues(start = 64.dp, end = 12.dp, bottom = 12.dp)

/** The Material3 range picker bound to [state]; shared by Material platforms and the iOS fallback. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialDateRangePicker(
    state: AdaptiveDateRangePickerState,
    modifier: Modifier,
    dateFormatter: DatePickerFormatter,
    title: (@Composable () -> Unit)?,
    headline: (@Composable () -> Unit)?,
    showModeToggle: Boolean,
    colors: DatePickerColors,
) {
    BoundedMaterial3DateRangePicker(
        state = state.dateRangePickerState,
        modifier = modifier,
        dateFormatter = dateFormatter,
        title = title,
        headline = headline,
        showModeToggle = showModeToggle,
        colors = colors,
    )
}

/** A Material3 range picker that falls back to [DateRangePickerFallbackHeight] under an unbounded height. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BoundedMaterial3DateRangePicker(
    state: DateRangePickerState,
    modifier: Modifier,
    dateFormatter: DatePickerFormatter,
    title: (@Composable () -> Unit)?,
    headline: (@Composable () -> Unit)?,
    showModeToggle: Boolean,
    colors: DatePickerColors,
) {
    // Material3's DateRangePicker scrolls its months and throws under an unbounded height.
    BoxWithConstraints(modifier = modifier) {
        val boundedHeight = if (maxHeight == Dp.Infinity) {
            Modifier.height(DateRangePickerFallbackHeight)
        } else {
            Modifier
        }
        DateRangePicker(
            state = state,
            modifier = boundedHeight,
            dateFormatter = dateFormatter,
            colors = colors,
            title = title,
            headline = headline,
            showModeToggle = showModeToggle,
        )
    }
}
