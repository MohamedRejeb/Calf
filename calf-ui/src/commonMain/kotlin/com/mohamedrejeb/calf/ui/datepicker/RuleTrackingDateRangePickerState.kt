package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates

/**
 * A [DateRangePickerState] that reports the rule returned by [currentRule] instead of the one
 * it was created with, for the same reason as [RuleTrackingDatePickerState].
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class RuleTrackingDateRangePickerState(
    delegate: DateRangePickerState,
    private val currentRule: () -> SelectableDates,
) : DateRangePickerState by delegate {
    override val selectableDates: SelectableDates
        get() = currentRule()
}
