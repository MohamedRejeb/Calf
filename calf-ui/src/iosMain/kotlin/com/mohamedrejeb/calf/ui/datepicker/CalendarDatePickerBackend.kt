@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.datepicker

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSDateComponents
import platform.UIKit.UICalendarSelectionSingleDate
import platform.UIKit.UICalendarSelectionSingleDateDelegateProtocol
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.NSObject

/**
 * Inline calendar backed by `UICalendarView` (iOS 16+). Days rejected by [isDaySelectable]
 * are greyed out and cannot be tapped, matching the Material picker.
 */
internal class CalendarDatePickerBackend(
    initialSelectedDateMillis: Long?,
    private val onSelectionChanged: (utcTimeMillis: Long?) -> Unit,
    private val isDaySelectable: (utcTimeMillis: Long) -> Boolean,
) : IosDatePickerBackend {

    private val host = CalendarViewHost()

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

    override val view: UIView
        get() = host.calendarView

    init {
        host.calendarView.selectionBehavior = selection
        initialSelectedDateMillis?.let { millis ->
            selection.setSelectedDate(utcDayToDateComponents(millis), animated = false)
            host.showMonth(millis, animated = false)
        }
        host.calendarView.sizeToFit()
    }

    override fun setSelectedDate(utcTimeMillis: Long?) {
        val current = selection.selectedDate?.toUtcDayMillis()
        if (current == utcTimeMillis) return

        selection.setSelectedDate(utcTimeMillis?.let(::utcDayToDateComponents), animated = true)
        utcTimeMillis?.let { millis -> host.showMonth(millis, animated = true) }
    }

    override fun applyDateBounds(minDateMillis: Long?, maxDateMillis: Long?) {
        host.applyDateBounds(minDateMillis, maxDateMillis)
    }

    override fun updateSelectableDates() {
        selection.updateSelectableDates()
        host.refresh()
    }

    override fun setEnabled(enabled: Boolean) {
        host.setEnabled(enabled)
    }

    override fun applyColors(containerColor: UIColor, selectedDayContainerColor: UIColor) {
        host.calendarView.tintColor = selectedDayContainerColor
        host.calendarView.backgroundColor = containerColor
    }
}
