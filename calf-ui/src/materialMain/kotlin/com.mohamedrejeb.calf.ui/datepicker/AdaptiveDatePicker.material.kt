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
        title = title,
        headline = headline,
        showModeToggle = showModeToggle,
        colors = colors,
    )
}
