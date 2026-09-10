package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3Api::class)
class DateBoundsTest {

    @Test
    fun `every day is within bounds when no bounds are set`() {
        assertTrue(isDayWithinBounds(MAR_15_2026 + NOON_OFFSET_MILLIS, minDateMillis = null, maxDateMillis = null))
    }

    @Test
    fun `days before the minimum are rejected and the minimum day itself is accepted`() {
        assertFalse(isDayWithinBounds(FEB_28_2026, minDateMillis = MAR_1_2026, maxDateMillis = null))
        assertTrue(isDayWithinBounds(MAR_1_2026, minDateMillis = MAR_1_2026, maxDateMillis = null))
    }

    @Test
    fun `days after the maximum are rejected and the maximum day itself is accepted`() {
        assertTrue(isDayWithinBounds(MAR_31_2026, minDateMillis = null, maxDateMillis = MAR_31_2026))
        assertFalse(isDayWithinBounds(APR_1_2026, minDateMillis = null, maxDateMillis = MAR_31_2026))
    }

    @Test
    fun `bounds are compared at day granularity`() {
        assertTrue(isDayWithinBounds(MAR_1_2026, minDateMillis = MAR_1_2026 + NOON_OFFSET_MILLIS, maxDateMillis = null))
        assertTrue(isDayWithinBounds(MAR_31_2026 + NOON_OFFSET_MILLIS, minDateMillis = null, maxDateMillis = MAR_31_2026))
    }

    @Test
    fun `years outside the bounds are rejected`() {
        assertFalse(isYearWithinBounds(2025, minDateMillis = MAR_1_2026, maxDateMillis = MAR_31_2026))
        assertTrue(isYearWithinBounds(2026, minDateMillis = MAR_1_2026, maxDateMillis = MAR_31_2026))
        assertFalse(isYearWithinBounds(2027, minDateMillis = MAR_1_2026, maxDateMillis = MAR_31_2026))
        assertTrue(isYearWithinBounds(1999, minDateMillis = null, maxDateMillis = null))
    }

    @Test
    fun `utc year is derived from epoch millis across year and leap boundaries`() {
        assertEquals(1970, utcYearOf(0))
        assertEquals(1969, utcYearOf(-1))
        assertEquals(2000, utcYearOf(FEB_29_2000))
        assertEquals(2024, utcYearOf(JAN_1_2025 - 1))
        assertEquals(2025, utcYearOf(JAN_1_2025))
        assertEquals(2026, utcYearOf(MAR_1_2026))
    }

    @Test
    fun `clamping keeps a day inside the bounds unchanged`() {
        assertEquals(MAR_15_2026, clampDayIntoBounds(MAR_15_2026, minDateMillis = MAR_1_2026, maxDateMillis = MAR_31_2026))
    }

    @Test
    fun `clamping moves a day before the minimum to the start of the minimum day`() {
        assertEquals(MAR_1_2026, clampDayIntoBounds(FEB_28_2026, minDateMillis = MAR_1_2026 + NOON_OFFSET_MILLIS, maxDateMillis = null))
    }

    @Test
    fun `clamping moves a day after the maximum to the start of the maximum day`() {
        assertEquals(MAR_31_2026, clampDayIntoBounds(APR_1_2026, minDateMillis = null, maxDateMillis = MAR_31_2026 + NOON_OFFSET_MILLIS))
    }

    @Test
    fun `nearest selectable day is the start of the day itself when it is selectable`() {
        assertEquals(
            MAR_15_2026,
            nearestSelectableDay(MAR_15_2026 + NOON_OFFSET_MILLIS, null, null, DatePickerDefaults.AllDates),
        )
    }

    @Test
    fun `nearest selectable day skips the days the rule rejects`() {
        assertEquals(MAR_13_2026, nearestSelectableDay(MAR_14_2026, null, null, WeekdaysOnly))
        assertEquals(MAR_16_2026, nearestSelectableDay(MAR_15_2026, null, null, WeekdaysOnly))
    }

    @Test
    fun `nearest selectable day stays inside the bounds`() {
        assertEquals(MAR_31_2026, nearestSelectableDay(APR_1_2026, null, MAR_31_2026, DatePickerDefaults.AllDates))
        assertEquals(MAR_1_2026, nearestSelectableDay(FEB_28_2026, MAR_1_2026, null, DatePickerDefaults.AllDates))
    }

    @Test
    fun `nearest selectable day is null when nothing is selectable within the search window`() {
        assertNull(nearestSelectableDay(MAR_15_2026, null, null, NoDates, maxDistanceDays = 3))
    }

    @Test
    fun `a picked day is kept when the bounds and the rule accept it`() {
        assertEquals(MAR_13_2026, resolvePickedDay(MAR_13_2026, DateBounds(MAR_1_2026, MAR_31_2026), WeekdaysOnly))
    }

    @Test
    fun `a rejected pick resolves to the nearest selectable day`() {
        assertEquals(MAR_13_2026, resolvePickedDay(MAR_14_2026, DateBounds(), WeekdaysOnly))
        assertEquals(MAR_31_2026, resolvePickedDay(APR_1_2026, DateBounds(maxDateMillis = MAR_31_2026), DatePickerDefaults.AllDates))
    }

    @Test
    fun `a rejected pick is kept when nothing selectable is within reach`() {
        assertEquals(MAR_14_2026, resolvePickedDay(MAR_14_2026, DateBounds(), NoDates))
    }
}
