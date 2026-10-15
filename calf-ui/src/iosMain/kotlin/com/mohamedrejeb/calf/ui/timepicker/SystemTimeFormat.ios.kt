package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.Composable
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

/** The hour skeleton: the formatter resolves it to the locale's preferred hour field, `H` or `h a`. */
private const val HOUR_SKELETON = "j"

@Composable
internal actual fun systemIs24HourFormat(): Boolean = isTwentyFourHourLocale(NSLocale.currentLocale)

/**
 * Whether [locale], which carries the device's time format setting, prefers a 24-hour clock.
 * `false` when the formatter gives no pattern.
 */
internal fun isTwentyFourHourLocale(locale: NSLocale): Boolean {
    val hourPattern = NSDateFormatter.dateFormatFromTemplate(HOUR_SKELETON, 0UL, locale) ?: return false
    return isTwentyFourHourTimePattern(hourPattern)
}
