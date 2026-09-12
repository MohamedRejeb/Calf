package com.mohamedrejeb.calf.ui.datepicker

import kotlin.test.Test
import kotlin.test.assertEquals

class DateRangeSelectionTest {

    @Test
    fun `first tap starts a new range`() {
        assertEquals(
            DateRangeSelection(start = MAR_14_2026, end = null),
            DateRangeSelection(start = null, end = null).afterTap(MAR_14_2026),
        )
    }

    @Test
    fun `tapping after the start completes the range`() {
        assertEquals(
            DateRangeSelection(start = MAR_14_2026, end = MAR_16_2026),
            DateRangeSelection(start = MAR_14_2026, end = null).afterTap(MAR_16_2026),
        )
    }

    @Test
    fun `tapping the start day again makes a single-day range`() {
        assertEquals(
            DateRangeSelection(start = MAR_14_2026, end = MAR_14_2026),
            DateRangeSelection(start = MAR_14_2026, end = null).afterTap(MAR_14_2026),
        )
    }

    @Test
    fun `tapping before the start moves the start`() {
        assertEquals(
            DateRangeSelection(start = MAR_13_2026, end = null),
            DateRangeSelection(start = MAR_14_2026, end = null).afterTap(MAR_13_2026),
        )
    }

    @Test
    fun `tapping while a range is complete starts over`() {
        assertEquals(
            DateRangeSelection(start = MAR_1_2026, end = null),
            DateRangeSelection(start = MAR_14_2026, end = MAR_16_2026).afterTap(MAR_1_2026),
        )
    }

    @Test
    fun `days in range lists every day start from start to end inclusive`() {
        assertEquals(
            listOf(MAR_13_2026, MAR_14_2026, MAR_15_2026, MAR_16_2026),
            DateRangeSelection(start = MAR_13_2026, end = MAR_16_2026).days(),
        )
        assertEquals(listOf(MAR_14_2026), DateRangeSelection(start = MAR_14_2026, end = null).days())
        assertEquals(emptyList(), DateRangeSelection(start = null, end = null).days())
    }
}
