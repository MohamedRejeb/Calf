package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberAdaptiveTimePickerState(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    is24Hour: Boolean = false,
): AdaptiveTimePickerState = rememberSaveable(
    saver = AdaptiveTimePickerState.Saver()
) {
    AdaptiveTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour,
    )
}

/**
 * A class to handle state changes in a [TimePicker]
 *
 * @sample androidx.compose.material3.samples.TimePickerSample
 *
 * @param initialHour
 *  starting hour for this state, will be displayed in the time picker when launched
 *  Ranges from 0 to 23
 * @param initialMinute
 *  starting minute for this state, will be displayed in the time picker when launched.
 *  Ranges from 0 to 59
 * @param is24Hour The format for this time picker `false` for 12 hour format with an AM/PM toggle
 *  or `true` for 24 hour format without toggle.
 */
@Stable
expect class AdaptiveTimePickerState(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
) {
    val minute: Int
    val hour: Int
    val is24hour: Boolean

    companion object {
        /**
         * The default [Saver] implementation for [TimePickerState].
         */
        fun Saver(): Saver<AdaptiveTimePickerState, *>
    }
}