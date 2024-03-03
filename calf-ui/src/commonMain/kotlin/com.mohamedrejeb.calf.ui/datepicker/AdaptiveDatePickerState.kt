package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
@ExperimentalMaterial3Api
fun rememberAdaptiveDatePickerState(
    initialSelectedDateMillis: Long? = null,
    initialDisplayedMonthMillis: Long? = initialSelectedDateMillis,
    yearRange: IntRange = DatePickerDefaults.YearRange,
    initialMaterialDisplayMode: DisplayMode = DisplayMode.Picker,
    initialUIKitDisplayMode: UIKitDisplayMode = UIKitDisplayMode.Picker,
): AdaptiveDatePickerState =
    rememberSaveable(
        saver = AdaptiveDatePickerState.Saver(),
    ) {
        AdaptiveDatePickerState(
            initialSelectedDateMillis = initialSelectedDateMillis,
            initialDisplayedMonthMillis = initialDisplayedMonthMillis,
            yearRange = yearRange,
            initialMaterialDisplayMode = initialMaterialDisplayMode,
            initialUIKitDisplayMode = initialUIKitDisplayMode,
        )
    }

/**
 * A state object that can be hoisted to observe the date picker state. See
 * [rememberAdaptiveDatePickerState].
 *
 * The state's [selectedDateMillis] will provide a timestamp that represents the _start_ of the day.
 *
 * @param initialSelectedDateMillis timestamp in _UTC_ milliseconds from the epoch that
 * represents an initial selection of a date. Provide a `null` to indicate no selection. Note
 * that the state's
 * [selectedDateMillis] will provide a timestamp that represents the _start_ of the day, which
 * may be different than the provided initialSelectedDateMillis.
 * @param initialDisplayedMonthMillis timestamp in _UTC_ milliseconds from the epoch that
 * represents an initial selection of a month to be displayed to the user. In case `null` is
 * provided, the displayed month would be the current one.
 * @param yearRange an [IntRange] that holds the year range that the date picker will be limited
 * to
 * @param initialMaterialDisplayMode an initial [DisplayMode] that this state will hold
 * @see rememberAdaptiveDatePickerState
 * @throws [IllegalArgumentException] if the initial selected date or displayed month represent
 * a year that is out of the year range.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
expect class AdaptiveDatePickerState(
    initialSelectedDateMillis: Long?,
    initialDisplayedMonthMillis: Long?,
    yearRange: IntRange,
    initialMaterialDisplayMode: DisplayMode,
    initialUIKitDisplayMode: UIKitDisplayMode,
) {
    /**
     * A timestamp that represents the _start_ of the day of the selected date in _UTC_ milliseconds
     * from the epoch.
     *
     * In case no date was selected or provided, the state will hold a `null` value.
     *
     * @see [setSelection]
     */
    var selectedDateMillis: Long?

    /**
     * Sets the selected date.
     *
     * @param dateMillis timestamp in _UTC_ milliseconds from the epoch that represents the date
     * selection, or `null` to indicate no selection.
     *
     * @throws IllegalArgumentException in case the given timestamps do not fall within the year
     * range this state was created with.
     */
    @Deprecated(
        message = "Use selectedDateMillis property instead",
        replaceWith = ReplaceWith("selectedDateMillis = dateMillis"),
    )
    fun setSelection(dateMillis: Long?)

    /**
     * A mutable state of [DisplayMode] that represents the current display mode of the UI
     * (i.e. picker or input).
     */
    var displayMode: DisplayMode

    companion object {
        /**
         * The default [Saver] implementation for [AdaptiveDatePickerState].
         */
        fun Saver(): Saver<AdaptiveDatePickerState, *>
    }
}

@OptIn(ExperimentalMaterial3Api::class)
internal val DisplayMode.value: Int
    get() =
        when (this) {
            DisplayMode.Picker -> 0
            DisplayMode.Input -> 1
            else -> -1
        }

@OptIn(ExperimentalMaterial3Api::class)
internal fun displayModeFromValue(value: Int) =
    when (value) {
        0 -> DisplayMode.Picker
        else -> DisplayMode.Input
    }

internal fun uiKitDisplayModeFromValue(value: Int) =
    when (value) {
        0 -> UIKitDisplayMode.Picker
        else -> UIKitDisplayMode.Wheels
    }

@OptIn(ExperimentalMaterial3Api::class)
internal expect fun getCalendarLocalDefault(): CalendarLocale
