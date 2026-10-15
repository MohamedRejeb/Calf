package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue

/**
 * State of an [AdaptiveTimePicker]: the selected time and the hour format, mirrored into the
 * native `UIDatePicker` by [AdaptiveTimePicker].
 *
 * @param initialHour the hour shown first, 0 to 23.
 * @param initialMinute the minute shown first, 0 to 59.
 * @param is24Hour initial value of [is24hour].
 * @throws IllegalArgumentException if [initialHour] or [initialMinute] is out of range.
 */
@Stable
actual class AdaptiveTimePickerState actual constructor(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
) {
    init {
        requireValidHour(initialHour, name = "initialHour")
        requireValidMinute(initialMinute, name = "initialMinute")
    }

    private var hourState by mutableStateOf(initialHour)
    private var minuteState by mutableStateOf(initialMinute)

    actual var hour: Int
        get() = hourState
        set(value) {
            requireValidHour(value, name = "hour")
            hourState = value
        }

    actual var minute: Int
        get() = minuteState
        set(value) {
            requireValidMinute(value, name = "minute")
            minuteState = value
        }

    /** Stored only: the native wheels always follow the device's time format setting. */
    actual var is24hour: Boolean by mutableStateOf(is24Hour)

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
