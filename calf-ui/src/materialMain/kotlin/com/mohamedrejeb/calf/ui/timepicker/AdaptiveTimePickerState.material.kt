package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver


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
@OptIn(ExperimentalMaterial3Api::class)
actual class AdaptiveTimePickerState actual constructor(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
) {
    init {
        require(initialHour in 0..23) { "initialHour should in [0..23] range" }
        require(initialHour in 0..59) { "initialMinute should be in [0..59] range" }
    }

    val timePickerState = TimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    actual val minute: Int get() = timePickerState.minute
    actual val hour: Int get() = timePickerState.hour
    actual val is24hour: Boolean get() = timePickerState.is24hour

    actual companion object {
        /**
         * The default [Saver] implementation for [TimePickerState].
         */
        actual fun Saver(): Saver<AdaptiveTimePickerState, *> = Saver(
            save = {
                listOf(
                    it.hour,
                    it.minute,
                    it.is24hour
                )
            },
            restore = { value ->
                AdaptiveTimePickerState(
                    initialHour = value[0] as Int,
                    initialMinute = value[1] as Int,
                    is24Hour = value[2] as Boolean
                )
            }
        )
    }
}