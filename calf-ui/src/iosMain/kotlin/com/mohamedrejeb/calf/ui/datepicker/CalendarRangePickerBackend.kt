@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSDateComponents
import platform.UIKit.UICalendarSelectionMultiDate
import platform.UIKit.UICalendarSelectionMultiDateDelegateProtocol
import platform.UIKit.UIView
import platform.darwin.NSObject

/**
 * Range calendar backed by `UICalendarView` (iOS 16+) with multi-date selection. Taps are
 * folded into a [DateRangeSelection], and every day of the range is highlighted. Days rejected
 * by [isDaySelectable] are greyed out.
 */
internal class CalendarRangePickerBackend(
    initialSelection: DateRangeSelection,
    private val onSelectionChanged: (selection: DateRangeSelection) -> Unit,
    private val isDaySelectable: (utcTimeMillis: Long) -> Boolean,
) {
    private val host = CalendarViewHost()

    private var selection = initialSelection

    private val selectionDelegate = object : NSObject(), UICalendarSelectionMultiDateDelegateProtocol {
        @ObjCSignatureOverride
        override fun multiDateSelection(
            selection: UICalendarSelectionMultiDate,
            didSelectDate: NSDateComponents,
        ) {
            onTap(didSelectDate.toUtcDayMillis())
        }

        @ObjCSignatureOverride
        override fun multiDateSelection(
            selection: UICalendarSelectionMultiDate,
            didDeselectDate: NSDateComponents,
        ) {
            onTap(didDeselectDate.toUtcDayMillis())
        }

        @ObjCSignatureOverride
        override fun multiDateSelection(
            selection: UICalendarSelectionMultiDate,
            canSelectDate: NSDateComponents,
        ): Boolean = isDaySelectable(canSelectDate.toUtcDayMillis())

        @ObjCSignatureOverride
        override fun multiDateSelection(
            selection: UICalendarSelectionMultiDate,
            canDeselectDate: NSDateComponents,
        ): Boolean = true
    }

    private val multiSelection = UICalendarSelectionMultiDate(delegate = selectionDelegate)

    val view: UIView
        get() = host.calendarView

    /** Width divided by height of the calendar, or 0 when it has no intrinsic size yet. */
    val aspectRatio: Float

    init {
        host.calendarView.selectionBehavior = multiSelection
        render(animated = false)
        selection.start?.let { host.showMonth(it, animated = false) }
        host.calendarView.sizeToFit()
        aspectRatio = host.calendarView.aspectRatioOrZero()
    }

    private fun onTap(utcTimeMillis: Long) {
        selection = selection.afterTap(utcTimeMillis)
        render(animated = true)
        onSelectionChanged(selection)
    }

    private fun render(animated: Boolean) {
        val highlightedDays = selection.days()
            .filter(isDaySelectable)
            .map(::utcDayToDateComponents)
        multiSelection.setSelectedDates(highlightedDays, animated = animated)
    }

    fun setSelectedRange(newSelection: DateRangeSelection) {
        if (newSelection == selection) return
        selection = newSelection
        render(animated = true)
        newSelection.start?.let { host.showMonth(it, animated = true) }
    }

    fun applyDateBounds(minDateMillis: Long?, maxDateMillis: Long?) {
        host.applyDateBounds(minDateMillis, maxDateMillis)
        updateSelectableDates()
    }

    fun updateSelectableDates() {
        multiSelection.updateSelectableDates()
        host.refresh()
    }

    fun applyColors(containerColor: Color, dayContentColor: Color, selectedDayContainerColor: Color) {
        host.applyColors(containerColor, dayContentColor, selectedDayContainerColor)
    }
}
