package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Creates and remembers an [AdaptiveDateRangePickerState].
 *
 * @param initialSelectedStartDateMillis timestamp in _UTC_ milliseconds from the epoch of the
 * initial start date, or `null` for no selection.
 * @param initialSelectedEndDateMillis timestamp in _UTC_ milliseconds from the epoch of the
 * initial end date, or `null` for no end date. Requires a start date.
 * @param initialDisplayedMonthMillis timestamp in _UTC_ milliseconds from the epoch of the month
 * to display first; the current month when `null`.
 * @param yearRange the years the picker is limited to.
 * @param initialMaterialDisplayMode the initial [DisplayMode] on Material platforms.
 * @param selectableDates the rule deciding which days can be picked, see
 * [AdaptiveDateRangePickerState.selectableDates]. Use [DateBounds] for a minimum and maximum
 * day and [and] to combine rules. Pass a stable instance.
 */
@Composable
@ExperimentalMaterial3Api
fun rememberAdaptiveDateRangePickerState(
    initialSelectedStartDateMillis: Long? = null,
    initialSelectedEndDateMillis: Long? = null,
    initialDisplayedMonthMillis: Long? = initialSelectedStartDateMillis,
    yearRange: IntRange = DatePickerDefaults.YearRange,
    initialMaterialDisplayMode: DisplayMode = DisplayMode.Picker,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
): AdaptiveDateRangePickerState =
    rememberSaveable(
        saver = AdaptiveDateRangePickerState.Saver(selectableDates),
    ) {
        AdaptiveDateRangePickerState(
            initialSelectedStartDateMillis = initialSelectedStartDateMillis,
            initialSelectedEndDateMillis = initialSelectedEndDateMillis,
            initialDisplayedMonthMillis = initialDisplayedMonthMillis,
            yearRange = yearRange,
            initialMaterialDisplayMode = initialMaterialDisplayMode,
            selectableDates = selectableDates,
        )
    }.apply {
        // Keep the rule in sync when the caller passes a new one on recomposition.
        this.selectableDates = selectableDates
    }

/**
 * State of an [AdaptiveDateRangePicker]: the selected start and end days plus the selectable
 * range. It wraps a Material3 [DateRangePickerState] on every platform.
 *
 * Unlike [AdaptiveDatePickerState], a range is not adjusted automatically when the rule
 * changes; it is validated when set, see [setSelection].
 *
 * @param initialSelectedStartDateMillis see [rememberAdaptiveDateRangePickerState].
 * @param initialSelectedEndDateMillis see [rememberAdaptiveDateRangePickerState].
 * @param initialDisplayedMonthMillis see [rememberAdaptiveDateRangePickerState].
 * @param yearRange the years the picker is limited to.
 * @param initialMaterialDisplayMode the initial [DisplayMode] on Material platforms.
 * @param selectableDates initial value of [selectableDates].
 * Initial dates are validated like [setSelection]: a missing or out-of-range start leaves the
 * selection empty, and an end before the start does too.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class AdaptiveDateRangePickerState(
    initialSelectedStartDateMillis: Long?,
    initialSelectedEndDateMillis: Long?,
    initialDisplayedMonthMillis: Long?,
    val yearRange: IntRange,
    initialMaterialDisplayMode: DisplayMode,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
) {
    /**
     * The [SelectableDates] rule deciding which days can be picked. Use [DateBounds] for a
     * minimum and maximum day, and [and] to combine rules. Honoured by the Material picker and
     * by the iOS 16+ calendar, where rejected days are greyed out; bounds coming from a
     * [DateBounds] are also applied natively. [SelectableDates.isSelectableYear] only affects
     * Material. Observable.
     */
    var selectableDates: SelectableDates by mutableStateOf(selectableDates)

    /** The bounds the rule enforces natively; open on both sides for rules without bounds. */
    internal val dateBounds: DateBounds
        get() = selectableDates.dateBoundsOrNull() ?: DateBounds()

    private val materialState: DateRangePickerState =
        DateRangePickerState(
            locale = getCalendarLocalDefault(),
            initialSelectedStartDateMillis = initialSelectedStartDateMillis,
            initialSelectedEndDateMillis = initialSelectedEndDateMillis,
            initialDisplayedMonthMillis = initialDisplayedMonthMillis,
            yearRange = yearRange,
            initialDisplayMode = initialMaterialDisplayMode,
        )

    /**
     * The Material3 state this state delegates to. It reports [selectableDates] as its rule,
     * so the Material3 picker follows every change.
     */
    // `this.` is required: the constructor parameter of the same name would be captured otherwise.
    val dateRangePickerState: DateRangePickerState =
        RuleTrackingDateRangePickerState(materialState) { this.selectableDates }

    /** The _start_ of the selected start day in _UTC_ milliseconds, or `null`. */
    val selectedStartDateMillis: Long?
        get() = dateRangePickerState.selectedStartDateMillis

    /** The _start_ of the selected end day in _UTC_ milliseconds, or `null`. */
    val selectedEndDateMillis: Long?
        get() = dateRangePickerState.selectedEndDateMillis

    /** The display mode on Material platforms (calendar or text input). */
    var displayMode: DisplayMode
        get() = dateRangePickerState.displayMode
        set(value) {
            dateRangePickerState.displayMode = value
        }

    /**
     * Sets the selected range.
     *
     * @param startDateMillis the start day in _UTC_ milliseconds, or `null` to clear the range.
     * @param endDateMillis the end day in _UTC_ milliseconds, or `null` for an open range.
     *
     * A `null` or out-of-[yearRange] start clears the whole selection, and so does an end
     * before the start. A `null` or out-of-range end only drops the end and keeps the start.
     */
    fun setSelection(startDateMillis: Long?, endDateMillis: Long?) {
        dateRangePickerState.setSelection(startDateMillis, endDateMillis)
    }

    internal fun isDaySelectable(utcTimeMillis: Long): Boolean =
        selectableDates.isSelectableDate(utcTimeMillis)

    companion object {
        /**
         * The default [Saver] implementation for [AdaptiveDateRangePickerState].
         *
         * @param selectableDates the rule to re-attach on restore, since it cannot be saved.
         */
        fun Saver(
            selectableDates: SelectableDates = DatePickerDefaults.AllDates,
        ): Saver<AdaptiveDateRangePickerState, Any> =
            listSaver(
                save = {
                    listOf(
                        it.selectedStartDateMillis,
                        it.selectedEndDateMillis,
                        it.dateRangePickerState.displayedMonthMillis,
                        it.yearRange.first,
                        it.yearRange.last,
                        it.displayMode.value,
                    )
                },
                restore = { value ->
                    AdaptiveDateRangePickerState(
                        initialSelectedStartDateMillis = value[0] as Long?,
                        initialSelectedEndDateMillis = value[1] as Long?,
                        initialDisplayedMonthMillis = value[2] as Long?,
                        yearRange = IntRange(value[3] as Int, value[4] as Int),
                        initialMaterialDisplayMode = displayModeFromValue(value[5] as Int),
                        selectableDates = selectableDates,
                    )
                },
            )
    }
}
