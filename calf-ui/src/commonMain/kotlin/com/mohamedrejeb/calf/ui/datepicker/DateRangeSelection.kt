package com.mohamedrejeb.calf.ui.datepicker

/**
 * The start and end of a date range as UTC start-of-day timestamps, and how taps on a
 * calendar change it: the first tap sets the start, a tap on or after the start sets the end,
 * a tap before the start moves the start, and any tap on a completed range starts over.
 */
internal data class DateRangeSelection(
    val start: Long?,
    val end: Long?,
) {
    fun afterTap(utcTimeMillis: Long): DateRangeSelection {
        val currentStart = start
        return when {
            currentStart == null || end != null -> DateRangeSelection(start = utcTimeMillis, end = null)
            utcTimeMillis < currentStart -> DateRangeSelection(start = utcTimeMillis, end = null)
            else -> DateRangeSelection(start = currentStart, end = utcTimeMillis)
        }
    }

    /** Every day of the range as UTC start-of-day timestamps; only the start while the end is unset. */
    fun days(): List<Long> {
        val firstDay = start?.floorDiv(MILLIS_PER_DAY) ?: return emptyList()
        val lastDay = end?.floorDiv(MILLIS_PER_DAY) ?: firstDay
        return (firstDay..lastDay).map { day -> day * MILLIS_PER_DAY }
    }
}
