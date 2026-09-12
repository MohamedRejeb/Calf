package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates

/**
 * A [DatePickerState] that reports the rule returned by [currentRule] instead of the one it was
 * created with.
 *
 * Material3 caches each day's enabled state keyed on the rule object, so the picker only
 * notices a change when the object itself changes. Handing it the caller's rule, which is a
 * new object whenever the rule changes, keeps the calendar in sync.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class RuleTrackingDatePickerState(
    delegate: DatePickerState,
    private val currentRule: () -> SelectableDates,
) : DatePickerState by delegate {
    override val selectableDates: SelectableDates
        get() = currentRule()
}
