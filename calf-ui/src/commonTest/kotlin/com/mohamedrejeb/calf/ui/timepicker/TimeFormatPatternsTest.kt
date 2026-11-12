package com.mohamedrejeb.calf.ui.timepicker

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimeFormatPatternsTest {

    @Test
    fun `a pattern with an H field is 24-hour`() {
        assertTrue(isTwentyFourHourTimePattern("HH:mm"))
        assertTrue(isTwentyFourHourTimePattern("H:mm"))
        assertTrue(isTwentyFourHourTimePattern("H")) // the iOS "j" skeleton result for a 24-hour locale
    }

    @Test
    fun `a pattern with a k field is 24-hour`() {
        assertTrue(isTwentyFourHourTimePattern("kk:mm"))
    }

    @Test
    fun `a pattern with an h or K field and an AM-PM marker is 12-hour`() {
        assertFalse(isTwentyFourHourTimePattern("h:mm a"))
        assertFalse(isTwentyFourHourTimePattern("h a")) // the iOS "j" skeleton result for a 12-hour locale
        assertFalse(isTwentyFourHourTimePattern("K:mm a"))
    }

    @Test
    fun `quoted literals do not count as fields`() {
        assertFalse(isTwentyFourHourTimePattern("'H' h:mm a"))
        assertTrue(isTwentyFourHourTimePattern("'at' HH:mm"))
        assertTrue(isTwentyFourHourTimePattern("HH 'o''clock'"))
    }

    @Test
    fun `an empty pattern is 12-hour`() {
        assertFalse(isTwentyFourHourTimePattern(""))
    }

    @Test
    fun `the browser hour12 flag maps to the 24-hour format only when it is false`() {
        assertTrue(isTwentyFourHourFromHour12Flag(HOUR12_FALSE))
        assertFalse(isTwentyFourHourFromHour12Flag(HOUR12_TRUE))
        assertFalse(isTwentyFourHourFromHour12Flag(HOUR12_UNKNOWN))
    }
}
