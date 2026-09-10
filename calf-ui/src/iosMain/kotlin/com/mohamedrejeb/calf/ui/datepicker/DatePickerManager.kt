@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.utils.applyTheme
import com.mohamedrejeb.calf.ui.utils.isDark
import com.mohamedrejeb.calf.ui.utils.isIOSVersionAtLeast
import com.mohamedrejeb.calf.ui.utils.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.UIKit.UIView

private const val FIRST_IOS_WITH_CALENDAR_VIEW = 16

/**
 * Owns the native date picker view and keeps it in sync with [AdaptiveDatePickerState].
 *
 * The inline style uses `UICalendarView` on iOS 16+, which can grey out days the
 * [isDaySelectable] rule rejects. The wheels style, and the inline style on older systems,
 * use `UIDatePicker` and resolve rejected picks through [resolveSelection].
 */
@InternalCalfApi
class DatePickerManager internal constructor(
    initialSelectedDateMillis: Long?,
    displayMode: UIKitDisplayMode,
    onSelectionChanged: (utcTimeMillis: Long?) -> Unit,
    isDaySelectable: (utcTimeMillis: Long) -> Boolean,
    resolveSelection: (pickedUtcTimeMillis: Long) -> Long,
) {
    private val backend: IosDatePickerBackend =
        if (displayMode == UIKitDisplayMode.Picker && isIOSVersionAtLeast(FIRST_IOS_WITH_CALENDAR_VIEW)) {
            CalendarDatePickerBackend(
                initialSelectedDateMillis = initialSelectedDateMillis,
                onSelectionChanged = onSelectionChanged,
                isDaySelectable = isDaySelectable,
            )
        } else {
            WheelsDatePickerBackend(
                initialSelectedDateMillis = initialSelectedDateMillis,
                style = when (displayMode) {
                    UIKitDisplayMode.Picker -> UIDatePickerStyle.UIDatePickerStyleInline
                    else -> UIDatePickerStyle.UIDatePickerStyleWheels
                },
                onSelectionChanged = onSelectionChanged,
                resolveSelection = resolveSelection,
            )
        }

    /** The native view to embed. */
    val view: UIView
        get() = backend.view

    /** Width divided by height of the native view, or 0 when it has no intrinsic size yet. */
    internal var aspectRatio by mutableFloatStateOf(0f)
        private set

    init {
        aspectRatio = backend.view.aspectRatioOrZero()
            .takeIf { it > 0f }
            ?: inlineDatePickerAspectRatio()
    }

    internal fun applyColors(
        containerColor: Color,
        dayContentColor: Color,
        selectedDayContainerColor: Color,
    ) {
        applyTheme(isDark = !isDark(dayContentColor))
        backend.applyColors(
            containerColor = containerColor.toUIColor(),
            selectedDayContainerColor = selectedDayContainerColor.toUIColor(),
        )
    }

    internal fun applyTheme(isDark: Boolean) {
        backend.view.applyTheme(isDark)
    }

    internal fun applyDateBounds(minDateMillis: Long?, maxDateMillis: Long?) {
        backend.applyDateBounds(minDateMillis, maxDateMillis)
        backend.updateSelectableDates()
    }

    internal fun updateSelectableDates() {
        backend.updateSelectableDates()
    }

    internal fun setSelectedDate(utcTimeMillis: Long?) {
        backend.setSelectedDate(utcTimeMillis)
    }
}

private fun UIView.aspectRatioOrZero(): Float =
    frame.useContents {
        if (size.height > 0.0) (size.width / size.height).toFloat() else 0f
    }

/** Size of a stock inline `UIDatePicker`, used when the calendar view reports no size yet. */
private fun inlineDatePickerAspectRatio(): Float =
    UIDatePicker().apply {
        datePickerMode = UIDatePickerMode.UIDatePickerModeDate
        preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleInline
    }.aspectRatioOrZero()
