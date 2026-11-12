package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import com.mohamedrejeb.calf.ui.utils.datetime.KotlinxDatetimeCalendarModel
import platform.Foundation.currentLocale

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
 * @param selectableDates initial value of [selectableDates]
 * @see rememberAdaptiveDatePickerState
 * @throws [IllegalArgumentException] if the initial selected date or displayed month represent
 * a year that is out of the year range.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
actual class AdaptiveDatePickerState actual constructor(
    val initialSelectedDateMillis: Long?,
    val initialDisplayedMonthMillis: Long?,
    actual val yearRange: IntRange,
    val initialMaterialDisplayMode: DisplayMode,
    actual val initialUIKitDisplayMode: UIKitDisplayMode,
    selectableDates: SelectableDates,
) {
    /** The system locale when the state was created; the native pickers are configured with it. */
    actual val locale: CalendarLocale = getCalendarLocalDefault()

    private val selectedDateState = mutableStateOf(
        initialSelectedDateMillis?.let { KotlinxDatetimeCalendarModel().getCanonicalDate(it) }?.utcTimeMillis,
    )

    /**
     * A timestamp that represents the _start_ of the day of the selected date in _UTC_ milliseconds
     * from the epoch.
     *
     * In case no date was selected or provided, the state will hold a `null` value. Selecting a
     * day also moves [displayedMonthMillis] to its month, which is where the native calendar
     * scrolls to.
     */
    actual var selectedDateMillis: Long?
        get() = selectedDateState.value
        set(value) {
            selectedDateState.value = value
            value?.let { displayedMonthMillis = it }
        }

    private val displayedMonthState = mutableStateOf(
        initialDisplayedMonth(initialDisplayedMonthMillis, yearRange),
    )

    /**
     * The _start_ of the displayed month in _UTC_ milliseconds. The iOS 16+ inline calendar
     * scrolls to it; the wheels and the compact picker have no month to show, so there it is only
     * stored. iOS does not report the month the user scrolls to, so the value only changes here
     * and through [selectedDateMillis]. A month outside [yearRange] is ignored, as on Material.
     */
    actual var displayedMonthMillis: Long
        get() = displayedMonthState.value
        set(value) {
            val monthStart = utcMonthStart(value)
            if (utcYearOf(monthStart) in yearRange) {
                displayedMonthState.value = monthStart
            }
        }

    /**
     * A mutable state of [DisplayMode] that represents the current display mode of the UI
     * (i.e. picker or input).
     */
    actual var displayMode: DisplayMode = initialMaterialDisplayMode

    private val selectableDatesState = mutableStateOf(selectableDates)

    actual var selectableDates: SelectableDates
        get() = selectableDatesState.value
        set(value) {
            selectableDatesState.value = value
            snapSelectionToSelectable()
        }

    actual companion object {
        /**
         * The default [Saver] implementation for [AdaptiveDatePickerState].
         *
         * @param selectableDates the rule to re-attach on restore, since it cannot be saved.
         */
        actual fun Saver(selectableDates: SelectableDates): Saver<AdaptiveDatePickerState, Any> =
            adaptiveDatePickerStateSaver(selectableDates)
    }
}

/** The requested month when its year is within [yearRange], otherwise the current month, as Material3 does. */
private fun initialDisplayedMonth(initialDisplayedMonthMillis: Long?, yearRange: IntRange): Long =
    initialDisplayedMonthMillis
        ?.let(::utcMonthStart)
        ?.takeIf { utcYearOf(it) in yearRange }
        ?: currentUtcMonthStart()

internal actual fun getCalendarLocalDefault(): CalendarLocale = CalendarLocale.currentLocale
