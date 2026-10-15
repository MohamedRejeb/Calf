package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp

/**
 * A date picker that adapts to the platform it is running on.
 *
 * On Material platforms it is the Material3 `DatePicker`. On iOS it is a `UICalendarView`
 * (iOS 16+) or a `UIDatePicker`, inline or as wheels depending on the state's
 * [AdaptiveDatePickerState.initialUIKitDisplayMode].
 *
 * @param state the state that holds the selection, the displayed month and the selectable dates.
 * @param modifier the modifier applied to the picker.
 * @param dateFormatter formats dates on Material platforms.
 * @param title the title slot on Material platforms; hidden when `null`.
 * @param headline the headline slot on Material platforms; hidden when `null`.
 * @param showModeToggle whether Material platforms offer the text input mode.
 * @param colors colors of the picker; the selected day color tints the native iOS picker.
 * @param focusRequester the requester the Material text field takes in input mode, so it is
 * focused as soon as the mode opens; `null` leaves the field unfocused. iOS has no input mode and
 * ignores it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun AdaptiveDatePicker(
    state: AdaptiveDatePickerState,
    modifier: Modifier = Modifier,
    dateFormatter: DatePickerFormatter = remember { DatePickerDefaults.dateFormatter() },
    title: (@Composable () -> Unit)? = null,
    headline: (@Composable () -> Unit)? = null,
    showModeToggle: Boolean = true,
    colors: DatePickerColors = DatePickerDefaults.colors(),
    focusRequester: FocusRequester? = remember { FocusRequester() },
)

internal val DatePickerTitlePadding = PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp)
internal val DatePickerHeadlinePadding = PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp)
