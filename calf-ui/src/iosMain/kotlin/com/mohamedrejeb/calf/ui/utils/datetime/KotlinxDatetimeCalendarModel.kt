package com.mohamedrejeb.calf.ui.utils.datetime

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

internal class KotlinxDatetimeCalendarModel(
    val locale: CalendarLocale
) {
    // A map for caching formatter related results for better performance
    internal val formatterCache = mutableMapOf<String, Any>()

    fun getCanonicalDate(timeInMillis: Long): CalendarDate {
        return Instant
            .fromEpochMilliseconds(timeInMillis)
            .toLocalDateTime(TimeZone.UTC)
            .date
            .atStartOfDayIn(TimeZone.UTC)
            .toCalendarDate(TimeZone.UTC)
    }

    /**
     * Formats a [CalendarMonth] into a string with a given date format skeleton.
     *
     * @param month a [CalendarMonth] to format
     * @param skeleton a date format skeleton
     * @param locale the [CalendarLocale] to use when formatting the given month
     */
    @OptIn(ExperimentalMaterial3Api::class)
    fun formatWithSkeleton(
        month: CalendarMonth,
        skeleton: String,
        locale: CalendarLocale = this.locale
    ): String =
        androidx.compose.material3.formatWithSkeleton(month.startUtcTimeMillis, skeleton, locale, formatterCache)

    /**
     * Formats a [CalendarDate] into a string with a given date format skeleton.
     *
     * @param date a [CalendarDate] to format
     * @param skeleton a date format skeleton
     * @param locale the [CalendarLocale] to use when formatting the given date
     */
    @OptIn(ExperimentalMaterial3Api::class)
    fun formatWithSkeleton(
        date: CalendarDate,
        skeleton: String,
        locale: CalendarLocale = this.locale
    ): String = androidx.compose.material3.formatWithSkeleton(date.utcTimeMillis, skeleton, locale, formatterCache)
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

    /**
     * Formats the date into a string with the given skeleton format and a [CalendarLocale].
     */
    fun format(
        calendarModel: KotlinxDatetimeCalendarModel,
        skeleton: String,
    ): String =
        calendarModel.formatWithSkeleton(this, skeleton, calendarModel.locale)
}

/**
 * Represents a calendar month.
 *
 * @param year the month's year
 * @param month the calendar month as an integer (e.g. JANUARY as 1, December as 12)
 * @param numberOfDays the number of days in the month
 * @param daysFromStartOfWeekToFirstOfMonth the number of days from the start of the week to the
 * first day of the month
 * @param startUtcTimeMillis the first day of the month in _UTC_ milliseconds from the epoch
 */
internal data class CalendarMonth(
    val year: Int,
    val month: Int,
    val numberOfDays: Int,
    val daysFromStartOfWeekToFirstOfMonth: Int,
    val startUtcTimeMillis: Long
) {
    /**
     * Formats the month into a string with the given skeleton format and a [CalendarLocale].
     */
    fun format(
        calendarModel: KotlinxDatetimeCalendarModel,
        skeleton: String
    ): String =
        calendarModel.formatWithSkeleton(this, skeleton, calendarModel.locale)
}

