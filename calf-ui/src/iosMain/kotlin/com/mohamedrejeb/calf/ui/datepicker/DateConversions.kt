@file:OptIn(ExperimentalTime::class, ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.datepicker

import com.mohamedrejeb.calf.ui.utils.datetime.KotlinxDatetimeCalendarModel
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.plus
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDayCalendarUnit
import platform.Foundation.NSMonthCalendarUnit
import platform.Foundation.NSYearCalendarUnit
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.time.ExperimentalTime

private val calendarModel = KotlinxDatetimeCalendarModel()

private const val MILLIS_PER_SECOND = 1000.0

/** The calendar day of a UTC day timestamp, as a plain date. */
internal fun utcDayToLocalDate(utcTimeMillis: Long): LocalDate {
    val canonicalDate = calendarModel.getCanonicalDate(utcTimeMillis)
    return LocalDate(
        year = canonicalDate.year,
        month = canonicalDate.month,
        day = canonicalDate.dayOfMonth,
    )
}

/** Start of the same calendar day in the device time zone. */
internal fun localStartOfDay(utcTimeMillis: Long): NSDate {
    val startMillis = utcDayToLocalDate(utcTimeMillis)
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
    return NSDate.dateWithTimeIntervalSince1970(startMillis / MILLIS_PER_SECOND)
}

/** Last millisecond of the same calendar day in the device time zone. */
internal fun localEndOfDay(utcTimeMillis: Long): NSDate {
    val nextDayStartMillis = utcDayToLocalDate(utcTimeMillis)
        .plus(1, DateTimeUnit.DAY)
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
    return NSDate.dateWithTimeIntervalSince1970((nextDayStartMillis - 1) / MILLIS_PER_SECOND)
}

/** The UTC start of the calendar day this date falls on in the device time zone. */
internal fun NSDate.toUtcDayMillis(): Long {
    val components = NSCalendar.currentCalendar.components(
        NSYearCalendarUnit or NSMonthCalendarUnit or NSDayCalendarUnit,
        this,
    )
    return components.toUtcDayMillis()
}

/** The UTC start of the day described by year, month and day components. */
internal fun NSDateComponents.toUtcDayMillis(): Long =
    LocalDate(year = year.toInt(), month = month.toInt(), day = day.toInt())
        .atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds()

/** Year, month and day components for a UTC day timestamp. */
internal fun utcDayToDateComponents(utcTimeMillis: Long): NSDateComponents {
    val date = utcDayToLocalDate(utcTimeMillis)
    return NSDateComponents().apply {
        year = date.year.toLong()
        month = date.month.number.toLong()
        day = date.day.toLong()
    }
}
