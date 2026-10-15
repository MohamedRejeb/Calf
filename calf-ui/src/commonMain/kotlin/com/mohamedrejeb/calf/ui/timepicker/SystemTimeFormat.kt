package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.Composable

/**
 * Whether the device is set to the 24-hour time format: the system setting on Android, the
 * default locale's short time pattern on Desktop, the locale's preferred hour format on iOS and
 * the browser's resolved `hour12` option on Web, where an undetectable setting counts as 12-hour.
 */
@Composable
internal expect fun systemIs24HourFormat(): Boolean

private const val PATTERN_QUOTE = '\''
private val TWENTY_FOUR_HOUR_FIELDS = setOf('H', 'k')

/** The browser resolved `hour12` to `true`. */
internal const val HOUR12_TRUE = 1

/** The browser resolved `hour12` to `false`. */
internal const val HOUR12_FALSE = 0

/** The browser could not resolve `hour12`. */
internal const val HOUR12_UNKNOWN = -1

/**
 * Whether a date pattern in the Unicode / `SimpleDateFormat` syntax shows the hour on a 24-hour
 * clock: it contains an `H` (0 to 23) or `k` (1 to 24) field outside quoted literals. Patterns
 * built on `h` or `K` carry an `a` (AM/PM) field instead and are 12-hour.
 */
internal fun isTwentyFourHourTimePattern(pattern: String): Boolean =
    pattern.withoutQuotedLiterals().any { it in TWENTY_FOUR_HOUR_FIELDS }

/** Maps the browser's resolved `hour12` option to the 24-hour format; only an explicit `false` is 24-hour. */
internal fun isTwentyFourHourFromHour12Flag(hour12Flag: Int): Boolean = hour12Flag == HOUR12_FALSE

/** Drops the `'...'` literals of a pattern: the even chunks between quotes are the fields. */
private fun String.withoutQuotedLiterals(): String =
    split(PATTERN_QUOTE)
        .filterIndexed { index, _ -> index % 2 == 0 }
        .joinToString(separator = "")
