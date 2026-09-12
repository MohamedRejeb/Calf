package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
class AdaptiveDateRangePickerStateTest {

    private val march = DateBounds(minDateMillis = MAR_1_2026, maxDateMillis = MAR_31_2026)

    private fun state(
        selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    ) = AdaptiveDateRangePickerState(
        initialSelectedStartDateMillis = null,
        initialSelectedEndDateMillis = null,
        initialDisplayedMonthMillis = null,
        yearRange = DatePickerDefaults.YearRange,
        initialMaterialDisplayMode = DisplayMode.Picker,
        selectableDates = selectableDates,
    )

    @Test
    fun `selection exposes start and end`() {
        val state = state()

        state.setSelection(MAR_13_2026, MAR_16_2026)

        assertEquals(MAR_13_2026, state.selectedStartDateMillis)
        assertEquals(MAR_16_2026, state.selectedEndDateMillis)
    }

    @Test
    fun `an end before the start clears the selection`() {
        val state = state()
        state.setSelection(MAR_13_2026, MAR_16_2026)

        state.setSelection(MAR_16_2026, MAR_13_2026)

        assertNull(state.selectedStartDateMillis)
        assertNull(state.selectedEndDateMillis)
    }

    @Test
    fun `selectable dates follow the bounds and the rule`() {
        val selectable = state(selectableDates = march and WeekdaysOnly).dateRangePickerState.selectableDates

        assertFalse(selectable.isSelectableDate(FEB_28_2026))
        assertTrue(selectable.isSelectableDate(MAR_13_2026))
        assertFalse(selectable.isSelectableDate(MAR_14_2026))
        assertFalse(selectable.isSelectableDate(APR_1_2026))
        assertFalse(selectable.isSelectableYear(2025))
        assertTrue(selectable.isSelectableYear(2026))
    }

    @Test
    fun `the material state exposes the current rule as a new object after a change`() {
        val state = state()
        val before = state.dateRangePickerState.selectableDates
        assertTrue(before.isSelectableDate(FEB_28_2026))

        state.selectableDates = DateBounds(minDateMillis = MAR_1_2026)

        val after = state.dateRangePickerState.selectableDates
        assertFalse(after.isSelectableDate(FEB_28_2026))
        assertTrue(before !== after)
    }

    @Test
    fun `changing the rule disables the rejected days in the range calendar`() = runComposeUiTest {
        var rule: SelectableDates by mutableStateOf(DatePickerDefaults.AllDates)
        setContent {
            val state = rememberAdaptiveDateRangePickerState(
                initialSelectedStartDateMillis = MAR_15_2026,
                selectableDates = rule,
            )
            AdaptiveDateRangePicker(state = state)
        }
        waitForIdle()
        dayCell(MAR_14_2026).assertIsEnabled()

        rule = WeekdaysOnly
        waitForIdle()

        dayCell(MAR_14_2026).assertIsNotEnabled()
    }

    @Test
    fun `saver round-trips the selection and re-attaches the rule`() {
        val saver = AdaptiveDateRangePickerState.Saver(march)
        val original = state(selectableDates = march)
        original.setSelection(MAR_13_2026, MAR_16_2026)

        val saved = requireNotNull(with(saver) { SaverScope { true }.save(original) })
        val restored = requireNotNull(saver.restore(saved))

        assertEquals(MAR_13_2026, restored.selectedStartDateMillis)
        assertEquals(MAR_16_2026, restored.selectedEndDateMillis)
        assertEquals(march, restored.selectableDates)
        assertNull(state().selectedStartDateMillis)
    }

    @Test
    fun `a rule can reject a year inside the bounds`() {
        val noYear2026 = object : SelectableDates {
            override fun isSelectableYear(year: Int): Boolean = year != 2026
        }
        val selectable = state(selectableDates = noYear2026).dateRangePickerState.selectableDates

        assertFalse(selectable.isSelectableYear(2026))
        assertTrue(selectable.isSelectableYear(2027))
    }

    @Test
    fun `range picker renders the month of the selection`() = runComposeUiTest {
        val state = AdaptiveDateRangePickerState(
            initialSelectedStartDateMillis = MAR_13_2026,
            initialSelectedEndDateMillis = MAR_16_2026,
            initialDisplayedMonthMillis = MAR_13_2026,
            yearRange = DatePickerDefaults.YearRange,
            initialMaterialDisplayMode = DisplayMode.Picker,
        )
        val startDayDescription = requireNotNull(
            DatePickerDefaults.dateFormatter()
                .formatDate(MAR_13_2026, getCalendarLocalDefault(), forContentDescription = true),
        )

        setContent {
            AdaptiveDateRangePicker(state = state)
        }

        val describesStartDay = hasText(startDayDescription, substring = true) or
            hasContentDescription(startDayDescription, substring = true)
        assertTrue(onAllNodes(describesStartDay).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun `range picker can be placed in a vertically scrolling column`() = runComposeUiTest {
        val state = state()

        setContent {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AdaptiveDateRangePicker(state = state)
            }
        }
        waitForIdle()
    }
}
