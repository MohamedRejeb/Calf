package com.mohamedrejeb.calf.ui.utils.datetime

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

internal class KotlinxDatetimeCalendarModel {
    fun getCanonicalDate(timeInMillis: Long): CalendarDate {
        return Instant
            .fromEpochMilliseconds(timeInMillis)
            .toLocalDateTime(TimeZone.UTC)
            .date
            .atStartOfDayIn(TimeZone.UTC)
            .toCalendarDate(TimeZone.UTC)
    }
}

internal fun Instant.toCalendarDate(
    timeZone : TimeZone
) : CalendarDate {

    val dateTime = toLocalDateTime(timeZone)

    return CalendarDate(
        year = dateTime.year,
        month = dateTime.monthNumber,
        dayOfMonth = dateTime.dayOfMonth,
        utcTimeMillis = toEpochMilliseconds()
    )
}

/**
 * Represents a calendar date.
 *
 * @param year the date's year
 * @param month the date's month
 * @param dayOfMonth the date's day of month
 * @param utcTimeMillis the date representation in _UTC_ milliseconds from the epoch
 */
internal data class CalendarDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val utcTimeMillis: Long
) : Comparable<CalendarDate> {
    override operator fun compareTo(other: CalendarDate): Int =
        this.utcTimeMillis.compareTo(other.utcTimeMillis)
}
