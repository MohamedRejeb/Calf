package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.utils.applyTheme
import com.mohamedrejeb.calf.ui.utils.datetime.KotlinxDatetimeCalendarModel
import com.mohamedrejeb.calf.ui.utils.isDark
import com.mohamedrejeb.calf.ui.utils.toUIColor
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.objc.sel_registerName

@OptIn(
    ExperimentalForeignApi::class,
)
@InternalCalfApi
class DatePickerManager internal constructor(
    initialSelectedDateMillis: Long?,
    private val datePicker: UIDatePicker,
    displayMode: UIKitDisplayMode,
    private val onSelectionChanged: (dateMillis: Long?) -> Unit,
) {
    /**
     * Pointer to the [dateSelection] method.
     */
    @OptIn(ExperimentalForeignApi::class)
    private val dateSelectionPointer get() = sel_registerName("dateSelection")

    /**
     * Dismisses the dialog.
     */
    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun dateSelection() {
        onSelectionChanged(
            datePicker.date.timeIntervalSince1970.toLong() * 1000
        )
    }

    internal var aspectRatio by mutableFloatStateOf(0f)
        private set

    init {
        val date =
            if (initialSelectedDateMillis != null) {
                val calendarModel = KotlinxDatetimeCalendarModel()
                val canonicalDate = calendarModel.getCanonicalDate(initialSelectedDateMillis)
                NSDate.dateWithTimeIntervalSince1970(canonicalDate.utcTimeMillis / 1000.0)
            } else {
                NSDate()
            }

        datePicker.setDate(date, animated = false)

        datePicker.locale = getCalendarLocalDefault()
        datePicker.timeZone = TimeZone.UTC.toNSTimeZone()

        datePicker.datePickerMode = UIDatePickerMode.UIDatePickerModeDate
        datePicker.preferredDatePickerStyle = when(displayMode) {
            UIKitDisplayMode.Picker ->
                UIDatePickerStyle.UIDatePickerStyleInline

            else ->
                UIDatePickerStyle.UIDatePickerStyleWheels
        }
        datePicker.addTarget(
            target = this,
            action = dateSelectionPointer,
            forControlEvents = UIControlEventValueChanged
        )
        datePicker.frame.useContents {
            aspectRatio = this.size.width.toFloat() / this.size.height.toFloat()
        }
    }

    internal fun applyColors(
        containerColor: Color,
        dayContentColor: Color,
        selectedDayContainerColor: Color,
    ) {
        applyTheme(isDark = !isDark(dayContentColor))
        datePicker.tintColor = selectedDayContainerColor.toUIColor()
        datePicker.backgroundColor = containerColor.toUIColor()
    }

    internal fun applyTheme(isDark: Boolean) {
        datePicker.applyTheme(isDark)
    }

}