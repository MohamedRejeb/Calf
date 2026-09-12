@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.ui.graphics.Color
import com.mohamedrejeb.calf.ui.utils.applyTheme
import com.mohamedrejeb.calf.ui.utils.isDark
import com.mohamedrejeb.calf.ui.utils.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateInterval
import platform.Foundation.NSTimeZone
import platform.Foundation.distantFuture
import platform.Foundation.distantPast
import platform.Foundation.localTimeZone
import platform.UIKit.UICalendarView
import platform.UIKit.UICalendarViewDecoration
import platform.UIKit.UICalendarViewDelegateProtocol
import platform.darwin.NSObject

private const val MAX_SUPPORTED_YEAR = 9999L
private const val MAX_MONTH = 12L

/** A `UICalendarView` (iOS 16+) configured for the device calendar, shared by the calendar backends. */
internal class CalendarViewHost {

    val calendarView: UICalendarView = UICalendarView().apply {
        calendar = NSCalendar.currentCalendar
        locale = getCalendarLocalDefault()
        timeZone = NSTimeZone.localTimeZone
    }

    /** Draws no decorations, but lets [refresh] ask the calendar to rebuild its day cells. */
    private val decorationDelegate = object : NSObject(), UICalendarViewDelegateProtocol {
        @ObjCSignatureOverride
        override fun calendarView(
            calendarView: UICalendarView,
            decorationForDateComponents: NSDateComponents,
        ): UICalendarViewDecoration? = null
    }

    init {
        calendarView.delegate = decorationDelegate
    }

    fun showMonth(utcTimeMillis: Long, animated: Boolean) {
        calendarView.setVisibleDateComponents(utcDayToDateComponents(utcTimeMillis), animated = animated)
    }

    fun applyDateBounds(minDateMillis: Long?, maxDateMillis: Long?) {
        val start = minDateMillis?.let(::localStartOfDay) ?: NSDate.distantPast
        val end = maxDateMillis?.let(::localEndOfDay) ?: NSDate.distantFuture
        calendarView.availableDateRange = NSDateInterval(startDate = start, endDate = end)
    }

    /**
     * Rebuilds the days around the visible month, so bounds and rule changes show right away
     * instead of on the next scroll or tap.
     */
    fun refresh() {
        val days = daysAroundVisibleMonth()
        if (days.isNotEmpty()) {
            calendarView.reloadDecorationsForDateComponents(days, animated = false)
        }
        calendarView.setNeedsLayout()
        calendarView.layoutIfNeeded()
    }

    fun applyColors(containerColor: Color, dayContentColor: Color, selectedDayContainerColor: Color) {
        calendarView.applyTheme(dark = !isDark(dayContentColor))
        calendarView.tintColor = selectedDayContainerColor.toUIColor()
        calendarView.backgroundColor = containerColor.toUIColor()
    }

    fun setEnabled(enabled: Boolean) {
        calendarView.userInteractionEnabled = enabled
    }

    /** Every day of the visible month and its two neighbours, which may be partly on screen. */
    private fun daysAroundVisibleMonth(): List<NSDateComponents> {
        val visible = calendarView.visibleDateComponents
        val year = visible.year
        val month = visible.month
        if (year !in 1..MAX_SUPPORTED_YEAR || month !in 1..MAX_MONTH) return emptyList()

        val firstOfMonth = LocalDate(year.toInt(), month.toInt(), 1)
        val start = firstOfMonth.minus(1, DateTimeUnit.MONTH)
        val end = firstOfMonth.plus(2, DateTimeUnit.MONTH)
        return generateSequence(start) { it.plus(1, DateTimeUnit.DAY) }
            .takeWhile { it < end }
            .map { date ->
                NSDateComponents().apply {
                    this.year = date.year.toLong()
                    this.month = date.month.number.toLong()
                    this.day = date.day.toLong()
                }
            }
            .toList()
    }
}
