package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3Api::class)
class DateBoundsSelectableDatesTest {

    @Test
    fun `date bounds accept the days inside and reject the days outside`() {
        val march = DateBounds(minDateMillis = MAR_1_2026, maxDateMillis = MAR_31_2026)

        assertFalse(march.isSelectableDate(FEB_28_2026))
        assertTrue(march.isSelectableDate(MAR_1_2026))
        assertTrue(march.isSelectableDate(MAR_31_2026 + NOON_OFFSET_MILLIS))
        assertFalse(march.isSelectableDate(APR_1_2026))
    }

    @Test
    fun `date bounds with one side open accept everything on that side`() {
        assertTrue(DateBounds(minDateMillis = MAR_1_2026).isSelectableDate(APR_1_2026))
        assertTrue(DateBounds(maxDateMillis = MAR_31_2026).isSelectableDate(FEB_28_2026))
        assertTrue(DateBounds().isSelectableDate(FEB_29_2000))
    }

    @Test
    fun `date bounds reject the years outside`() {
        val march = DateBounds(minDateMillis = MAR_1_2026, maxDateMillis = MAR_31_2026)

        assertFalse(march.isSelectableYear(2025))
        assertTrue(march.isSelectableYear(2026))
        assertFalse(march.isSelectableYear(2027))
    }

    @Test
    fun `and accepts a day only when both rules accept it`() {
        val marchWeekdays = DateBounds(minDateMillis = MAR_1_2026, maxDateMillis = MAR_31_2026) and WeekdaysOnly

        assertTrue(marchWeekdays.isSelectableDate(MAR_13_2026))
        assertFalse(marchWeekdays.isSelectableDate(MAR_14_2026))
        assertFalse(marchWeekdays.isSelectableDate(APR_1_2026))
        assertFalse(marchWeekdays.isSelectableYear(2025))
    }

    @Test
    fun `bounds are extracted from date bounds and intersected through and`() {
        val march = DateBounds(minDateMillis = MAR_1_2026, maxDateMillis = MAR_31_2026)
        val fromMid = DateBounds(minDateMillis = MAR_15_2026)

        assertEquals(march, march.dateBoundsOrNull())
        assertEquals(
            DateBounds(minDateMillis = MAR_15_2026, maxDateMillis = MAR_31_2026),
            (march and fromMid).dateBoundsOrNull(),
        )
        assertEquals(march, (march and WeekdaysOnly).dateBoundsOrNull())
        assertEquals(march, (WeekdaysOnly and march).dateBoundsOrNull())
    }

    @Test
    fun `other rules expose no bounds`() {
        assertNull(WeekdaysOnly.dateBoundsOrNull())
        assertNull(DatePickerDefaults.AllDates.dateBoundsOrNull())
        assertNull((WeekdaysOnly and NoDates).dateBoundsOrNull())
    }
}
