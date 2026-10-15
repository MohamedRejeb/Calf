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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties

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
    materialShowModeToggle: Boolean,
    materialFocusRequester: FocusRequester?,
    materialDialogShape: Shape,
    materialDialogTonalElevation: Dp,
    materialDialogProperties: DialogProperties,
) {
    var isDialogOpen by rememberSaveable { mutableStateOf(false) }

    val label = state.selectedDateMillis
        ?.let { dateFormatter.formatDate(it, state.locale) }
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
        shape = materialDialogShape,
        tonalElevation = materialDialogTonalElevation,
        colors = colors,
        properties = materialDialogProperties,
    ) {
        DatePicker(
            state = dialogState,
            dateFormatter = dateFormatter,
            title = materialTitle ?: {
                DatePickerDefaults.DatePickerTitle(
                    displayMode = dialogState.displayMode,
                    modifier = Modifier.padding(DatePickerTitlePadding),
                    contentColor = colors.titleContentColor,
                )
            },
            headline = materialHeadline ?: {
                DatePickerDefaults.DatePickerHeadline(
                    selectedDateMillis = dialogState.selectedDateMillis,
                    displayMode = dialogState.displayMode,
                    dateFormatter = dateFormatter,
                    modifier = Modifier.padding(DatePickerHeadlinePadding),
                    contentColor = colors.headlineContentColor,
                )
            },
            showModeToggle = materialShowModeToggle,
            colors = colors,
            focusRequester = materialFocusRequester,
        )
    }
}

/**
 * A Material3 state seeded from this state that keeps following its rule. The dialog opens on the
 * selected day's month, or on [AdaptiveDatePickerState.displayedMonthMillis] while nothing is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
private fun AdaptiveDatePickerState.newDialogState(): DatePickerState {
    val scratch = DatePickerState(
        locale = locale,
        initialSelectedDateMillis = selectedDateMillis,
        initialDisplayedMonthMillis = selectedDateMillis ?: displayedMonthMillis,
        yearRange = yearRange,
        initialDisplayMode = displayMode,
    )
    return RuleTrackingDatePickerState(scratch) { selectableDates }
}
