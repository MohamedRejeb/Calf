package com.mohamedrejeb.calf.ui.timepicker

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SystemTimeFormatDesktopTest {

    @Test
    fun `US English shows a 12-hour clock`() {
        assertFalse(isTwentyFourHourLocale(Locale.US))
    }

    @Test
    fun `German shows a 24-hour clock`() {
        assertTrue(isTwentyFourHourLocale(Locale.GERMANY))
    }

    @Test
    fun `British English shows a 24-hour clock`() {
        assertTrue(isTwentyFourHourLocale(Locale.UK))
    }
}
