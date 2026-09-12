package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Creates and remembers an [AdaptiveDatePickerState].
 *
 * @param initialSelectedDateMillis timestamp in _UTC_ milliseconds from the epoch that
 * represents an initial selection of a date. Provide a `null` to indicate no selection.
 * @param initialDisplayedMonthMillis timestamp in _UTC_ milliseconds from the epoch that
 * represents an initial selection of a month to be displayed to the user. In case `null` is
 * provided, the displayed month would be the current one.
 * @param yearRange an [IntRange] that holds the year range that the date picker will be limited
 * to
 * @param initialMaterialDisplayMode an initial [DisplayMode] that this state will hold
 * @param initialUIKitDisplayMode an initial [UIKitDisplayMode] used by the iOS picker
 * @param selectableDates the rule deciding which days can be picked, see
 * [AdaptiveDatePickerState.selectableDates]. Use [DateBounds] for a minimum and maximum day and
 * [and] to combine rules. Pass a stable instance (for example an `object`, a `data class` or a
 * remembered value) so the picker does not re-evaluate on every recomposition.
 */
@Composable
@ExperimentalMaterial3Api
fun rememberAdaptiveDatePickerState(
    initialSelectedDateMillis: Long? = null,
    initialDisplayedMonthMillis: Long? = initialSelectedDateMillis,
    yearRange: IntRange = DatePickerDefaults.YearRange,
    initialMaterialDisplayMode: DisplayMode = DisplayMode.Picker,
    initialUIKitDisplayMode: UIKitDisplayMode = UIKitDisplayMode.Picker,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
): AdaptiveDatePickerState =
    rememberSaveable(
        saver = AdaptiveDatePickerState.Saver(selectableDates),
    ) {
        AdaptiveDatePickerState(
            initialSelectedDateMillis = initialSelectedDateMillis,
            initialDisplayedMonthMillis = initialDisplayedMonthMillis,
            yearRange = yearRange,
            initialMaterialDisplayMode = initialMaterialDisplayMode,
            initialUIKitDisplayMode = initialUIKitDisplayMode,
            selectableDates = selectableDates,
        )
    }.apply {
        // Keep the rule in sync when the caller passes a new one on recomposition.
        this.selectableDates = selectableDates
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
 * @param initialUIKitDisplayMode an initial [UIKitDisplayMode] used by the iOS picker
 * @param selectableDates initial value of [AdaptiveDatePickerState.selectableDates]
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
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
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

    /**
     * The [SelectableDates] rule deciding which days can be picked. Use [DateBounds] for a
     * minimum and maximum day, and [and] to combine rules.
     *
     * Honoured by the Material picker and by the iOS 16+ calendars, where rejected days are
     * greyed out; bounds coming from a [DateBounds] are also applied natively, so the iOS wheels
     * stop at the range. The iOS wheels picker, and the inline picker below iOS 16, cannot grey
     * out days: a rejected day is snapped to the nearest selectable one instead. Observable:
     * changing it updates the displayed picker, and a selection the new rule rejects is moved
     * to the nearest selectable day. [SelectableDates.isSelectableYear] only affects Material.
     */
    var selectableDates: SelectableDates

    companion object {
        /**
         * The default [Saver] implementation for [AdaptiveDatePickerState].
         *
         * @param selectableDates the rule to re-attach on restore, since it cannot be saved.
         */
        fun Saver(selectableDates: SelectableDates = DatePickerDefaults.AllDates): Saver<AdaptiveDatePickerState, Any>
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
