package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.mutableStateOf
import com.mohamedrejeb.calf.core.InternalCalfApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
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
    private val datePickerDelegate = object : NSObject() {
        @ObjCAction
        fun onTimeChanged(sender: UIDatePicker) {
            val components = NSCalendar.currentCalendar.components(
                NSCalendarUnitHour or NSCalendarUnitMinute,
                sender.date
            )

            onHourChanged(components.hour.toInt())
            onMinuteChanged(components.minute.toInt())
        }
    }

    val datePickerWidth = mutableStateOf(0f)
    val datePickerHeight = mutableStateOf(0f)

    init {
        val dateComponents = NSDateComponents().apply {
            minute = initialMinute.toLong()
            hour = initialHour.toLong()
        }

        val date = NSCalendar.currentCalendar.dateFromComponents(dateComponents) ?: NSDate()

        datePicker.apply {
            this.date = date
            locale = NSLocale.currentLocale
            datePickerMode = UIDatePickerMode.UIDatePickerModeTime
            preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels

            // Add target using NSObject delegate
            addTarget(
                target = datePickerDelegate,
                action = sel_registerName("onTimeChanged:"),
                forControlEvents = UIControlEventValueChanged
            )
        }

        datePicker.frame.useContents {
            datePickerWidth.value = this.size.width.toFloat()
            datePickerHeight.value = this.size.height.toFloat()
        }
    }


}