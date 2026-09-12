package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * A compact date picker that shows the selected date and opens the full picker on tap.
 *
 * On iOS this is the system compact `UIDatePicker`, which pops its calendar over the content.
 * It applies bounds coming from a [DateBounds] natively, but cannot grey out days any other rule
 * rejects: such a pick snaps to the nearest selectable day. On Material platforms it is an
 * outlined button showing the formatted date that opens a `DatePickerDialog` holding a Material3
 * `DatePicker`. Both share [state], including its selectable-dates rule.
 *
 * @param state the state that holds the selection and the selectable range.
 * @param modifier the modifier applied to the field.
 * @param enabled whether the user can open the picker.
 * @param dateFormatter formats the date shown in the field and the dialog on Material platforms.
 * @param colors colors of the picker; the selected day color tints the native iOS control.
 * @param materialPlaceholder text shown in the field on Material platforms while nothing is
 * selected; the iOS control always shows a date.
 * @param materialConfirmText label of the dialog button that keeps the selection on Material platforms.
 * @param materialDismissText label of the dialog button that discards the day picked in the
 * dialog on Material platforms. Dismissing the dialog by tapping outside behaves the same way.
 * The shared [state] only changes when the dialog is confirmed.
 * @param materialTitle the dialog title slot on Material platforms. Unlike [AdaptiveDatePicker],
 * `null` shows the Material3 default rather than hiding it, because a dialog needs a header.
 * @param materialHeadline the dialog headline slot on Material platforms; `null` shows the
 * Material3 default, which displays the day picked so far.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun AdaptiveCompactDatePicker(
    state: AdaptiveDatePickerState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dateFormatter: DatePickerFormatter = remember { DatePickerDefaults.dateFormatter() },
    colors: DatePickerColors = DatePickerDefaults.colors(),
    materialPlaceholder: String = "Select date",
    materialConfirmText: String = "OK",
    materialDismissText: String = "Cancel",
    materialTitle: (@Composable () -> Unit)? = null,
    materialHeadline: (@Composable () -> Unit)? = null,
)
