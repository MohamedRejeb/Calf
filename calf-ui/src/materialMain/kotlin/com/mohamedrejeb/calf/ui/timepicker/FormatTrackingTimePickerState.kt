package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * A [TimePickerState] whose [is24hour] is a snapshot state.
 *
 * The Material3 default keeps `is24hour` in a plain field, so the picker never notices a change
 * made after creation. Reading it from a snapshot state lets the Material3 picker re-lay out in
 * the new format.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class FormatTrackingTimePickerState(
    delegate: TimePickerState,
) : TimePickerState by delegate {
    override var is24hour: Boolean by mutableStateOf(delegate.is24hour)
}
