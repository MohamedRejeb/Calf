package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun AdaptiveTimePicker(
    state: AdaptiveTimePickerState,
    modifier: Modifier,
    colors: TimePickerColors,
    layoutType: TimePickerLayoutType,
) {
    TimePicker(
        state = state.timePickerState,
        modifier = modifier,
        colors = colors,
        layoutType = layoutType,
    )
}