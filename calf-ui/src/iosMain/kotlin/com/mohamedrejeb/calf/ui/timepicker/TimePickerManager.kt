package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.mutableStateOf
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.Foundation.*
import platform.UIKit.*
import platform.objc.sel_registerName

@OptIn(ExperimentalForeignApi::class)
class TimePickerManager internal constructor(
    private val datePicker: UIDatePicker,
    initialMinute: Int,
    initialHour: Int,
    is24Hour: Boolean,
    private val onHourChanged: (hour: Int) -> Unit,
    private val onMinuteChanged: (minute: Int) -> Unit,
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
        val components = NSCalendar.currentCalendar.components(
            NSCalendarUnitHour or NSCalendarUnitMinute,
            datePicker.date
        )

        onHourChanged(components.hour.toInt())
        onMinuteChanged(components.minute.toInt())
    }

    val datePickerWidth = mutableStateOf(0f)
    val datePickerHeight = mutableStateOf(0f)

    init {
        val dateComponents = NSDateComponents()
        dateComponents.minute = initialMinute.toLong()
        dateComponents.hour = initialHour.toLong()
        val date = NSCalendar.currentCalendar.dateFromComponents(dateComponents) ?: NSDate()
        datePicker.date = date
        datePicker.locale = NSLocale.currentLocale
        datePicker.datePickerMode = UIDatePickerMode.UIDatePickerModeTime
        datePicker.preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels
        datePicker.addTarget(
            target = this,
            action = dateSelectionPointer,
            forControlEvents = UIControlEventValueChanged
        )
        datePicker.frame.useContents {
            datePickerWidth.value = this.size.width.toFloat()
            datePickerHeight.value = this.size.height.toFloat()
        }
    }
}