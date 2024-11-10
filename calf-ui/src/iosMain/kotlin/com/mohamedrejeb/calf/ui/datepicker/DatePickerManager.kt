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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDayCalendarUnit
import platform.Foundation.NSMonthCalendarUnit
import platform.Foundation.NSYearCalendarUnit
import platform.Foundation.dateWithTimeIntervalSince1970
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
    private val calendarModel = KotlinxDatetimeCalendarModel()

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
        val components = NSCalendar.currentCalendar.components(
            NSYearCalendarUnit or NSMonthCalendarUnit or NSDayCalendarUnit,
            datePicker.date
        )

        val utcTimeMillis =
            LocalDate(
                year = components.year.toInt(),
                monthNumber = components.month.toInt(),
                dayOfMonth = components.day.toInt(),
            )
                .atStartOfDayIn(TimeZone.UTC)
                .toEpochMilliseconds()

        onSelectionChanged(utcTimeMillis)
    }

    internal var aspectRatio by mutableFloatStateOf(0f)
        private set

    init {
        val date =
            if (initialSelectedDateMillis != null) {
                val canonicalDate = calendarModel.getCanonicalDate(initialSelectedDateMillis)
                val currentTimeZoneTimeMillis =
                    LocalDate(
                        year = canonicalDate.year,
                        monthNumber = canonicalDate.month,
                        dayOfMonth = canonicalDate.dayOfMonth,
                    )
                        .atStartOfDayIn(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds()

                NSDate.dateWithTimeIntervalSince1970(currentTimeZoneTimeMillis / 1000.0)
            } else {
                NSDate()
            }

        datePicker.setDate(date, animated = false)

        datePicker.locale = getCalendarLocalDefault()
        datePicker.timeZone = TimeZone.currentSystemDefault().toNSTimeZone()

        datePicker.datePickerMode = UIDatePickerMode.UIDatePickerModeDate
        datePicker.preferredDatePickerStyle = when (displayMode) {
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