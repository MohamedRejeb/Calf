@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.datepicker

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
import platform.UIKit.UICalendarSelectionSingleDate
import platform.UIKit.UICalendarSelectionSingleDateDelegateProtocol
import platform.UIKit.UICalendarView
import platform.UIKit.UICalendarViewDecoration
import platform.UIKit.UICalendarViewDelegateProtocol
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.NSObject

private const val MAX_SUPPORTED_YEAR = 9999L
private const val MAX_MONTH = 12L

/**
 * Inline calendar backed by `UICalendarView` (iOS 16+). Days rejected by [isDaySelectable]
 * are greyed out and cannot be tapped, matching the Material picker.
 */
internal class CalendarDatePickerBackend(
    initialSelectedDateMillis: Long?,
    private val onSelectionChanged: (utcTimeMillis: Long?) -> Unit,
    private val isDaySelectable: (utcTimeMillis: Long) -> Boolean,
) : IosDatePickerBackend {

    private val calendarView = UICalendarView()

    private val selectionDelegate = object : NSObject(), UICalendarSelectionSingleDateDelegateProtocol {
        @ObjCSignatureOverride
        override fun dateSelection(
            selection: UICalendarSelectionSingleDate,
            didSelectDate: NSDateComponents?,
        ) {
            onSelectionChanged(didSelectDate?.toUtcDayMillis())
        }

        @ObjCSignatureOverride
        override fun dateSelection(
            selection: UICalendarSelectionSingleDate,
            canSelectDate: NSDateComponents?,
        ): Boolean {
            val day = canSelectDate?.toUtcDayMillis() ?: return true
            return isDaySelectable(day)
        }
    }

    private val selection = UICalendarSelectionSingleDate(delegate = selectionDelegate)

    /** Draws no decorations, but lets [refresh] ask the calendar to rebuild its day cells. */
    private val decorationDelegate = object : NSObject(), UICalendarViewDelegateProtocol {
        @ObjCSignatureOverride
        override fun calendarView(
            calendarView: UICalendarView,
            decorationForDateComponents: NSDateComponents,
        ): UICalendarViewDecoration? = null
    }

    override val view: UIView
        get() = calendarView

    init {
        calendarView.calendar = NSCalendar.currentCalendar
        calendarView.locale = getCalendarLocalDefault()
        calendarView.timeZone = NSTimeZone.localTimeZone
        calendarView.delegate = decorationDelegate
        calendarView.selectionBehavior = selection
        initialSelectedDateMillis?.let { millis ->
            selection.setSelectedDate(utcDayToDateComponents(millis), animated = false)
            calendarView.setVisibleDateComponents(utcDayToDateComponents(millis), animated = false)
        }
        calendarView.sizeToFit()
    }

    override fun setSelectedDate(utcTimeMillis: Long?) {
        val current = selection.selectedDate?.toUtcDayMillis()
        if (current == utcTimeMillis) return

        selection.setSelectedDate(utcTimeMillis?.let(::utcDayToDateComponents), animated = true)
        utcTimeMillis?.let { millis ->
            calendarView.setVisibleDateComponents(utcDayToDateComponents(millis), animated = true)
        }
    }

    override fun applyDateBounds(minDateMillis: Long?, maxDateMillis: Long?) {
        val start = minDateMillis?.let(::localStartOfDay) ?: NSDate.distantPast
        val end = maxDateMillis?.let(::localEndOfDay) ?: NSDate.distantFuture
        calendarView.availableDateRange = NSDateInterval(startDate = start, endDate = end)
    }

    override fun updateSelectableDates() {
        selection.updateSelectableDates()
        refresh()
    }

    override fun setEnabled(enabled: Boolean) {
        calendarView.userInteractionEnabled = enabled
    }

    /**
     * Rebuilds the days around the visible month, so bounds and rule changes show right away
     * instead of on the next scroll or tap.
     */
    private fun refresh() {
        val days = daysAroundVisibleMonth()
        if (days.isNotEmpty()) {
            calendarView.reloadDecorationsForDateComponents(days, animated = false)
        }
        calendarView.setNeedsLayout()
        calendarView.layoutIfNeeded()
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

    override fun applyColors(containerColor: UIColor, selectedDayContainerColor: UIColor) {
        calendarView.tintColor = selectedDayContainerColor
        calendarView.backgroundColor = containerColor
    }
}
