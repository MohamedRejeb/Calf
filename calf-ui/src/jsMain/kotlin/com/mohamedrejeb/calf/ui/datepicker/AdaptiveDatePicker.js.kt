package com.mohamedrejeb.calf.ui.datepicker

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

}