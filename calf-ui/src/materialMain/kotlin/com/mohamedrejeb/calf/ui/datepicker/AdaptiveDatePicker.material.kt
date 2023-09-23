package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun AdaptiveDatePicker(
    state: AdaptiveDatePickerState,
    modifier: Modifier,
    dateFormatter: DatePickerFormatter,
    title: @Composable() (() -> Unit)?,
    headline: @Composable() (() -> Unit)?,
    showModeToggle: Boolean,
    colors: DatePickerColors
) {
    DatePicker(
        state = state.datePickerState,
        modifier = modifier,
        dateFormatter = dateFormatter,
        title = title ?: {
            DatePickerDefaults.DatePickerTitle(
                state = state.datePickerState,
                modifier = Modifier.padding(DatePickerTitlePadding)
            )
        },
        headline = headline ?: {
            DatePickerDefaults.DatePickerHeadline(
                state = state.datePickerState,
                dateFormatter = dateFormatter,
                modifier = Modifier.padding(DatePickerHeadlinePadding)
            )
        },
        showModeToggle = showModeToggle,
        colors = colors,
    )
}