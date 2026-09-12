package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun AdaptiveCompactDatePicker(
    state: AdaptiveDatePickerState,
    modifier: Modifier,
    enabled: Boolean,
    dateFormatter: DatePickerFormatter,
    colors: DatePickerColors,
    materialPlaceholder: String,
    materialConfirmText: String,
    materialDismissText: String,
    materialTitle: (@Composable () -> Unit)?,
    materialHeadline: (@Composable () -> Unit)?,
) {
    var isDialogOpen by rememberSaveable { mutableStateOf(false) }

    val label = state.selectedDateMillis
        ?.let { dateFormatter.formatDate(it, getCalendarLocalDefault()) }
        ?: materialPlaceholder

    OutlinedButton(
        onClick = { isDialogOpen = true },
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(label)
    }

    if (!isDialogOpen) return

    // The dialog edits a scratch copy so the shared state only changes on confirm.
    // It leaves composition when the dialog closes, so every opening starts fresh.
    val dialogState = remember { state.newDialogState() }

    val keepSelection = {
        state.selectedDateMillis = dialogState.selectedDateMillis
        isDialogOpen = false
    }
    val discardSelection = { isDialogOpen = false }

    DatePickerDialog(
        onDismissRequest = discardSelection,
        confirmButton = {
            TextButton(onClick = keepSelection) {
                Text(materialConfirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = discardSelection) {
                Text(materialDismissText)
            }
        },
        colors = colors,
    ) {
        DatePicker(
            state = dialogState,
            dateFormatter = dateFormatter,
            title = materialTitle ?: {
                DatePickerDefaults.DatePickerTitle(
                    displayMode = dialogState.displayMode,
                    modifier = Modifier.padding(DatePickerTitlePadding),
                )
            },
            headline = materialHeadline ?: {
                DatePickerDefaults.DatePickerHeadline(
                    selectedDateMillis = dialogState.selectedDateMillis,
                    displayMode = dialogState.displayMode,
                    dateFormatter = dateFormatter,
                    modifier = Modifier.padding(DatePickerHeadlinePadding),
                )
            },
            colors = colors,
        )
    }
}

/** A Material3 state seeded from this state that keeps following its rule. */
@OptIn(ExperimentalMaterial3Api::class)
private fun AdaptiveDatePickerState.newDialogState(): DatePickerState {
    val scratch = DatePickerState(
        locale = getCalendarLocalDefault(),
        initialSelectedDateMillis = selectedDateMillis,
        initialDisplayedMonthMillis = selectedDateMillis,
        yearRange = yearRange,
        initialDisplayMode = displayMode,
    )
    return RuleTrackingDatePickerState(scratch) { selectableDates }
}
