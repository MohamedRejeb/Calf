package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Immutable

/**
 * A [SelectableDates] rule that accepts the days from [minDateMillis] to [maxDateMillis], both
 * inclusive and compared per UTC day, so any timestamp inside the first or last day keeps that
 * whole day selectable. A `null` bound is open on that side.
 *
 * Besides greying out days, the pickers apply these bounds natively: the iOS wheels stop at the
 * range and the calendars hide the months outside it. Combine with other rules using [and].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Immutable
data class DateBounds(
    val minDateMillis: Long? = null,
    val maxDateMillis: Long? = null,
) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        isDayWithinBounds(utcTimeMillis, minDateMillis, maxDateMillis)

    override fun isSelectableYear(year: Int): Boolean =
        isYearWithinBounds(year, minDateMillis, maxDateMillis)
}

/** A rule that accepts a day, or a year, only when both this rule and [other] accept it. */
@OptIn(ExperimentalMaterial3Api::class)
infix fun SelectableDates.and(other: SelectableDates): SelectableDates =
    CombinedSelectableDates(this, other)

@OptIn(ExperimentalMaterial3Api::class)
@Immutable
internal data class CombinedSelectableDates(
    val first: SelectableDates,
    val second: SelectableDates,
) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        first.isSelectableDate(utcTimeMillis) && second.isSelectableDate(utcTimeMillis)

    override fun isSelectableYear(year: Int): Boolean =
        first.isSelectableYear(year) && second.isSelectableYear(year)
}

/**
 * The bounds this rule enforces when it is a [DateBounds], or combines one through [and];
 * `null` for any other rule.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal fun SelectableDates.dateBoundsOrNull(): DateBounds? =
    when (this) {
        is DateBounds -> this
        is CombinedSelectableDates -> intersectOrNull(first.dateBoundsOrNull(), second.dateBoundsOrNull())
        else -> null
    }

/** The days accepted by both this and [other]. */
internal fun DateBounds.intersect(other: DateBounds): DateBounds =
    DateBounds(
        minDateMillis = tighterMin(minDateMillis, other.minDateMillis),
        maxDateMillis = tighterMax(maxDateMillis, other.maxDateMillis),
    )

private fun intersectOrNull(first: DateBounds?, second: DateBounds?): DateBounds? =
    when {
        first == null -> second
        second == null -> first
        else -> first.intersect(second)
    }

private fun tighterMin(first: Long?, second: Long?): Long? =
    if (first == null || second == null) first ?: second else maxOf(first, second)

private fun tighterMax(first: Long?, second: Long?): Long? =
    if (first == null || second == null) first ?: second else minOf(first, second)
