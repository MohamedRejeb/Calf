package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

internal const val MAX_HOUR = 23
internal const val MAX_MINUTE = 59
private const val FIRST_PM_HOUR = 12

/**
 * Creates and remembers an [AdaptiveTimePickerState].
 *
 * @param initialHour the hour shown first, 0 to 23.
 * @param initialMinute the minute shown first, 0 to 59.
 * @param is24Hour `true` for the 24-hour format, `false` for the 12-hour format with an AM/PM
 * toggle. Defaults to the device's time format setting, read once when the state is created;
 * earlier versions defaulted to `false`. See [AdaptiveTimePickerState.is24hour] for what the
 * value controls on each platform.
 */
@Composable
fun rememberAdaptiveTimePickerState(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    is24Hour: Boolean = systemIs24HourFormat(),
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
 * State of an [AdaptiveTimePicker]: the selected time and the hour format. It wraps a Material3
 * `TimePickerState` on Material platforms and drives a `UIDatePicker` in time mode on iOS.
 *
 * @param initialHour the hour shown first, 0 to 23.
 * @param initialMinute the minute shown first, 0 to 59.
 * @param is24Hour initial value of [is24hour].
 * @throws IllegalArgumentException if [initialHour] or [initialMinute] is out of range.
 */
@Stable
expect class AdaptiveTimePickerState(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
) {
    /**
     * The selected minute, 0 to 59. Observable. Setting it moves the picker on every platform.
     *
     * @throws IllegalArgumentException if the value is out of range.
     */
    var minute: Int

    /**
     * The selected hour, 0 to 23, whatever the format. Observable. Setting it moves the picker
     * on every platform; see [isPm] for the half of the day it falls in.
     *
     * @throws IllegalArgumentException if the value is out of range.
     */
    var hour: Int

    /**
     * Whether the picker uses the 24-hour format. Observable: on Material platforms, changing
     * it re-lays out the picker in the other format. The iOS wheels always follow the device's
     * time format setting, so there the value is only stored, and it matches the wheels when
     * the state was created with the default of [rememberAdaptiveTimePickerState].
     */
    var is24hour: Boolean

    companion object {
        /**
         * The default [Saver] implementation for [AdaptiveTimePickerState].
         */
        fun Saver(): Saver<AdaptiveTimePickerState, *>
    }
}

/** Whether [AdaptiveTimePickerState.hour] is in the afternoon or the evening, that is 12 or later. */
val AdaptiveTimePickerState.isPm: Boolean
    get() = hour >= FIRST_PM_HOUR

/** Fails with a message naming [name] when [hour] is not within 0 to 23. */
internal fun requireValidHour(hour: Int, name: String) {
    require(hour in 0..MAX_HOUR) { "$name should be in [0..$MAX_HOUR] range" }
}

/** Fails with a message naming [name] when [minute] is not within 0 to 59. */
internal fun requireValidMinute(minute: Int, name: String) {
    require(minute in 0..MAX_MINUTE) { "$name should be in [0..$MAX_MINUTE] range" }
}
