@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.utils.isDark
import com.mohamedrejeb.calf.ui.utils.isIOSVersionAtLeast
import com.mohamedrejeb.calf.ui.utils.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.UIKit.UIView

internal const val FIRST_IOS_WITH_CALENDAR_VIEW = 16

/** How the native date picker is presented. */
internal enum class IosDatePickerPresentation {
    /** A full calendar; `UICalendarView` on iOS 16+, an inline `UIDatePicker` before that. */
    Inline,

    /** Spinning wheels. */
    Wheels,

    /** A small button showing the date that pops the calendar over the content. */
    Compact,
}

/**
 * Owns the native date picker view and keeps it in sync with the selection it is given.
 *
 * The inline presentation uses `UICalendarView` on iOS 16+, which greys out days the
 * [isDaySelectable] rule rejects. Every other presentation, including the system compact
 * control, uses `UIDatePicker`, which only enforces bounds, and resolves rejected picks
 * through [resolveSelection].
 */
@InternalCalfApi
class DatePickerManager internal constructor(
    initialSelectedDateMillis: Long?,
    private val presentation: IosDatePickerPresentation,
    onSelectionChanged: (utcTimeMillis: Long?) -> Unit,
    isDaySelectable: (utcTimeMillis: Long) -> Boolean,
    resolveSelection: (pickedUtcTimeMillis: Long) -> Long,
) {
    private val onNativeSelectionChanged: (Long?) -> Unit = { utcTimeMillis ->
        remeasureCompactView()
        onSelectionChanged(utcTimeMillis)
    }

    private val backend: IosDatePickerBackend =
        if (presentation == IosDatePickerPresentation.Inline && isIOSVersionAtLeast(FIRST_IOS_WITH_CALENDAR_VIEW)) {
            CalendarDatePickerBackend(
                initialSelectedDateMillis = initialSelectedDateMillis,
                onSelectionChanged = onNativeSelectionChanged,
                isDaySelectable = isDaySelectable,
            )
        } else {
            UIDatePickerBackend(
                initialSelectedDateMillis = initialSelectedDateMillis,
                style = presentation.toUIDatePickerStyle(),
                onSelectionChanged = onNativeSelectionChanged,
                resolveSelection = resolveSelection,
            )
        }

    /** The native view to embed. */
    val view: UIView
        get() = backend.view

    /** Width divided by height of the native view, or 0 when it has no intrinsic size yet. */
    internal var aspectRatio by mutableFloatStateOf(0f)
        private set

    /**
     * Intrinsic size of the native view in points, which equal dp on iOS. The compact picker
     * resizes with its date label, so it is measured again after every selection change.
     */
    internal var viewSize: DpSize by mutableStateOf(backend.view.currentSize())
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
        backend.applyTheme(isDark)
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
        remeasureCompactView()
    }

    private fun remeasureCompactView() {
        if (presentation != IosDatePickerPresentation.Compact) return
        backend.view.sizeToFit()
        viewSize = backend.view.currentSize()
    }

    internal fun setEnabled(enabled: Boolean) {
        backend.setEnabled(enabled)
    }
}

private fun IosDatePickerPresentation.toUIDatePickerStyle(): UIDatePickerStyle =
    when (this) {
        IosDatePickerPresentation.Inline -> UIDatePickerStyle.UIDatePickerStyleInline
        IosDatePickerPresentation.Wheels -> UIDatePickerStyle.UIDatePickerStyleWheels
        IosDatePickerPresentation.Compact -> UIDatePickerStyle.UIDatePickerStyleCompact
    }

private fun UIView.currentSize(): DpSize =
    frame.useContents { DpSize(size.width.dp, size.height.dp) }

internal fun UIView.aspectRatioOrZero(): Float =
    frame.useContents {
        if (size.height > 0.0) (size.width / size.height).toFloat() else 0f
    }

/** Size of a stock inline `UIDatePicker`, used when the calendar view reports no size yet. */
internal fun inlineDatePickerAspectRatio(): Float =
    UIDatePicker().apply {
        datePickerMode = UIDatePickerMode.UIDatePickerModeDate
        preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleInline
    }.aspectRatioOrZero()
