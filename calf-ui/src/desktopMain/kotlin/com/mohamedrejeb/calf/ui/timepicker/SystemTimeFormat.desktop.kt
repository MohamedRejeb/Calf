package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.Composable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
internal actual fun systemIs24HourFormat(): Boolean =
    isTwentyFourHourLocale(Locale.getDefault(Locale.Category.FORMAT))

/**
 * Whether [locale]'s short time format shows a 24-hour clock. The JVM has no time format
 * setting of its own, so the locale is the closest to the device setting; `false` when the JVM
 * gives no pattern for it.
 */
internal fun isTwentyFourHourLocale(locale: Locale): Boolean {
    val format = DateFormat.getTimeInstance(DateFormat.SHORT, locale) as? SimpleDateFormat ?: return false
    return isTwentyFourHourTimePattern(format.toPattern())
}
