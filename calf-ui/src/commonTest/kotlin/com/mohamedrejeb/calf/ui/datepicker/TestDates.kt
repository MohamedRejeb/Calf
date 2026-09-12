package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates

/** UTC start-of-day timestamp for the given number of days since 1970-01-01. */
internal fun utcDay(daysSinceEpoch: Long): Long = daysSinceEpoch * MILLIS_PER_DAY

internal const val NOON_OFFSET_MILLIS = 12L * 60 * 60 * 1000

// Day counts since 1970-01-01 (UTC) for the dates used in the date picker tests.
// 2026-03-01 is a Sunday, so 2026-03-14 is a Saturday and 2026-03-15 a Sunday.
internal val FEB_29_2000 = utcDay(11016)
internal val JAN_1_2025 = utcDay(20089)
internal val FEB_28_2026 = utcDay(20512)
internal val MAR_1_2026 = utcDay(20513)
internal val MAR_13_2026 = utcDay(20525)
internal val MAR_14_2026 = utcDay(20526)
internal val MAR_15_2026 = utcDay(20527)
internal val MAR_16_2026 = utcDay(20528)
internal val MAR_31_2026 = utcDay(20543)
internal val APR_1_2026 = utcDay(20544)

private const val DAYS_PER_WEEK = 7L
private const val EPOCH_DAY_OFFSET_FROM_MONDAY = 3L // 1970-01-01 was a Thursday
private const val SATURDAY_INDEX = 5L

/** Accepts Monday to Friday only, computed from the epoch day without a calendar library. */
@OptIn(ExperimentalMaterial3Api::class)
internal object WeekdaysOnly : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val dayOfWeekIndex = (utcTimeMillis.floorDiv(MILLIS_PER_DAY) + EPOCH_DAY_OFFSET_FROM_MONDAY)
            .mod(DAYS_PER_WEEK)
        return dayOfWeekIndex < SATURDAY_INDEX
    }
}

/** Rejects every day. */
@OptIn(ExperimentalMaterial3Api::class)
internal object NoDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = false
}
