package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
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
    val yearRange: IntRange,
    val initialMaterialDisplayMode: DisplayMode,
    val initialUIKitDisplayMode: UIKitDisplayMode,
    selectableDates: SelectableDates,
) {
    /**
     * A timestamp that represents the _start_ of the day of the selected date in _UTC_ milliseconds
     * from the epoch.
     *
     * In case no date was selected or provided, the state will hold a `null` value.
     *
     * @see [setSelection]
     */
    actual var selectedDateMillis by mutableStateOf(
        initialSelectedDateMillis?.let { KotlinxDatetimeCalendarModel().getCanonicalDate(it) }?.utcTimeMillis
    )

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
        level = DeprecationLevel.ERROR,
    )
    actual fun setSelection(
        @Suppress("AutoBoxing") dateMillis: Long?,
    ) {
        selectedDateMillis = dateMillis
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
            listSaver(
                save = {
                    listOf(
                        it.selectedDateMillis,
                        it.yearRange.first,
                        it.yearRange.last,
                        it.displayMode.value,
                        it.initialUIKitDisplayMode.value,
                    )
                },
                restore = { value ->
                    AdaptiveDatePickerState(
                        initialSelectedDateMillis = value[0] as Long?,
                        initialDisplayedMonthMillis = value[0] as Long?,
                        yearRange = IntRange(value[1] as Int, value[2] as Int),
                        initialMaterialDisplayMode = displayModeFromValue(value[3] as Int),
                        initialUIKitDisplayMode = uiKitDisplayModeFromValue(value[4] as Int),
                        selectableDates = selectableDates,
                    )
                },
            )
    }
}

internal actual fun getCalendarLocalDefault(): CalendarLocale = CalendarLocale.currentLocale
