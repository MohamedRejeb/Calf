package com.mohamedrejeb.calf.ui.datepicker

import com.mohamedrejeb.calf.ui.utils.applyTheme
import platform.UIKit.UIColor
import platform.UIKit.UIView

/**
 * A native view that lets the user pick a day, driven by [DatePickerManager].
 */
internal interface IosDatePickerBackend {
    /** The view to embed in the Compose hierarchy. */
    val view: UIView

    /** Shows [utcTimeMillis] as the selection without notifying the selection callback. */
    fun setSelectedDate(utcTimeMillis: Long?)

    /** Restricts the pickable range; a null bound is open on that side. */
    fun applyDateBounds(minDateMillis: Long?, maxDateMillis: Long?)

    /** Re-evaluates which days can be picked after the rule or the bounds changed. */
    fun updateSelectableDates()

    fun setEnabled(enabled: Boolean)

    fun applyColors(containerColor: UIColor, selectedDayContainerColor: UIColor)

    fun applyTheme(isDark: Boolean) {
        view.applyTheme(isDark)
    }
}
