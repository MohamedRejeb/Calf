package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver

/**
 * State of an [AdaptiveTimePicker]: the selected time and the hour format, delegated to a
 * Material3 [TimePickerState].
 *
 * @param initialHour the hour shown first, 0 to 23.
 * @param initialMinute the minute shown first, 0 to 59.
 * @param is24Hour initial value of [is24hour].
 * @throws IllegalArgumentException if [initialHour] or [initialMinute] is out of range.
 */
@Stable
@OptIn(ExperimentalMaterial3Api::class)
actual class AdaptiveTimePickerState actual constructor(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
) {
    init {
        requireValidHour(initialHour, name = "initialHour")
        requireValidMinute(initialMinute, name = "initialMinute")
    }

    /**
     * The Material3 state this state delegates to. Its [TimePickerState.is24hour] is observable,
     * unlike the Material3 default's, so the picker re-lays out when the format changes.
     */
    val timePickerState: TimePickerState = FormatTrackingTimePickerState(
        TimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = is24Hour,
        ),
    )

    actual var minute: Int
        get() = timePickerState.minute
        set(value) {
            requireValidMinute(value, name = "minute")
            timePickerState.minute = value
        }

    actual var hour: Int
        get() = timePickerState.hour
        set(value) {
            requireValidHour(value, name = "hour")
            timePickerState.hour = value
        }

    actual var is24hour: Boolean
        get() = timePickerState.is24hour
        set(value) {
            timePickerState.is24hour = value
        }

    actual companion object {
        /**
         * The default [Saver] implementation for [AdaptiveTimePickerState].
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
