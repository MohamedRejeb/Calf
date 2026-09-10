package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
class AdaptiveDatePickerStateMaterialTest {

    private val march = DateBounds(minDateMillis = MAR_1_2026, maxDateMillis = MAR_31_2026)

    private fun state(
        selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    ) = AdaptiveDatePickerState(
        initialSelectedDateMillis = null,
        initialDisplayedMonthMillis = null,
        yearRange = DatePickerDefaults.YearRange,
        initialMaterialDisplayMode = DisplayMode.Picker,
        initialUIKitDisplayMode = UIKitDisplayMode.Picker,
        selectableDates = selectableDates,
    )

    @Test
    fun `material selectable dates follow the rule`() {
        val selectable = state(selectableDates = march).datePickerState.selectableDates

        assertFalse(selectable.isSelectableDate(FEB_28_2026))
        assertTrue(selectable.isSelectableDate(MAR_1_2026))
        assertTrue(selectable.isSelectableDate(MAR_31_2026))
        assertFalse(selectable.isSelectableDate(APR_1_2026))
        assertFalse(selectable.isSelectableYear(2025))
        assertTrue(selectable.isSelectableYear(2026))
        assertFalse(selectable.isSelectableYear(2027))
    }

    @Test
    fun `the material state exposes the current rule as a new object after a change`() {
        val state = state()
        val before = state.datePickerState.selectableDates
        assertTrue(before.isSelectableDate(FEB_28_2026))

        state.selectableDates = DateBounds(minDateMillis = MAR_1_2026)

        val after = state.datePickerState.selectableDates
        assertFalse(after.isSelectableDate(FEB_28_2026))
        assertTrue(before !== after)
    }

    @Test
    fun `changing the rule disables the rejected days in the calendar`() = runComposeUiTest {
        var rule: SelectableDates by mutableStateOf(DatePickerDefaults.AllDates)
        setContent {
            val state = rememberAdaptiveDatePickerState(
                initialSelectedDateMillis = MAR_15_2026,
                selectableDates = rule,
            )
            AdaptiveDatePicker(state = state)
        }
        waitForIdle()
        dayCell(MAR_14_2026).assertIsEnabled()

        rule = WeekdaysOnly
        waitForIdle()

        dayCell(MAR_14_2026).assertIsNotEnabled()
    }

    @Test
    fun `selection is clamped to the new maximum when the bounds shrink`() {
        val state = state()
        state.selectedDateMillis = MAR_15_2026

        state.selectableDates = DateBounds(maxDateMillis = MAR_1_2026)

        assertEquals(MAR_1_2026, state.selectedDateMillis)
    }

    @Test
    fun `selection is clamped to the new minimum when the bounds shrink`() {
        val state = state()
        state.selectedDateMillis = FEB_28_2026

        state.selectableDates = DateBounds(minDateMillis = MAR_1_2026)

        assertEquals(MAR_1_2026, state.selectedDateMillis)
    }

    @Test
    fun `selection inside the bounds is left untouched`() {
        val state = state()
        state.selectedDateMillis = MAR_15_2026

        state.selectableDates = march

        assertEquals(MAR_15_2026, state.selectedDateMillis)
    }

    @Test
    fun `selection snaps to the nearest selectable day when the rule changes`() {
        val state = state()
        state.selectedDateMillis = MAR_14_2026

        state.selectableDates = WeekdaysOnly

        assertEquals(MAR_13_2026, state.selectedDateMillis)
    }

    @Test
    fun `selection is kept when no selectable day is within reach`() {
        val state = state()
        state.selectedDateMillis = MAR_14_2026

        state.selectableDates = NoDates

        assertEquals(MAR_14_2026, state.selectedDateMillis)
    }

    @Test
    fun `saver round-trips the selection and re-attaches the rule`() {
        val saver = AdaptiveDatePickerState.Saver(march)
        val original = state(selectableDates = march)
        original.selectedDateMillis = MAR_15_2026

        val saved = requireNotNull(with(saver) { SaverScope { true }.save(original) })
        val restored = requireNotNull(saver.restore(saved))

        assertEquals(MAR_15_2026, restored.selectedDateMillis)
        assertEquals(march, restored.selectableDates)
    }

    @Test
    fun `saver restores a list saved by the previous format`() {
        val savedPreviously = listOf<Any?>(MAR_15_2026, 2020, 2030, 0, 0)

        val restored = requireNotNull(AdaptiveDatePickerState.Saver().restore(savedPreviously))

        assertEquals(MAR_15_2026, restored.selectedDateMillis)
        assertEquals(DatePickerDefaults.AllDates, restored.selectableDates)
    }

    @Test
    fun `remembered state follows a new rule passed on recomposition`() = runComposeUiTest {
        var rule: SelectableDates by mutableStateOf(DatePickerDefaults.AllDates)
        lateinit var state: AdaptiveDatePickerState

        setContent {
            state = rememberAdaptiveDatePickerState(selectableDates = rule)
        }
        waitForIdle()
        assertTrue(state.selectableDates.isSelectableDate(FEB_28_2026))

        rule = march
        waitForIdle()

        assertEquals(march, state.selectableDates)
        assertNull(state.selectedDateMillis)
    }
}
