package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
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
import platform.Foundation.*
import platform.UIKit.*
import platform.objc.sel_registerName

@OptIn(
    ExperimentalForeignApi::class,
    ExperimentalMaterial3Api::class
)
@InternalCalfApi
class DatePickerManager @OptIn(ExperimentalMaterial3Api::class) internal constructor(
    initialSelectedDateMillis: Long?,
    colors: DatePickerColors,
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

    val datePickerWidth = mutableStateOf(0f)
    val datePickerHeight = mutableStateOf(0f)

    init {
        val date =
            if (initialSelectedDateMillis != null) {
                val calendarModel = KotlinxDatetimeCalendarModel(getCalendarLocalDefault())
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
            datePickerWidth.value = this.size.width.toFloat()
            datePickerHeight.value = this.size.height.toFloat()
        }
        applyColors(colors)
    }

    fun applyColors(colors: DatePickerColors) {
        applyTheme(isDark = !isDark(colors.dayContentColor))
        datePicker.tintColor = colors.selectedDayContainerColor.toUIColor()
        datePicker.backgroundColor = colors.containerColor.toUIColor()
    }

    fun applyTheme(isDark: Boolean) {
        datePicker.applyTheme(isDark)
    }

}