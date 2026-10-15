package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.mutableStateOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.darwin.NSObject
import platform.objc.sel_registerName

/**
 * Configures a `UIDatePicker` as time wheels and keeps it in sync with the state: user changes
 * reach [onHourChanged] and [onMinuteChanged], programmatic ones arrive through [setTime].
 */
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
            val components = sender.date.hourAndMinute()

            onHourChanged(components.hour.toInt())
            onMinuteChanged(components.minute.toInt())
        }
    }

    val datePickerWidth = mutableStateOf(0f)
    val datePickerHeight = mutableStateOf(0f)

    init {
        datePicker.apply {
            date = dateAt(hour = initialHour, minute = initialMinute)
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

    /**
     * Moves the wheels to [hour]:[minute]. A programmatic `setDate` fires no value-changed
     * event, so the callbacks stay quiet; a time the wheels already show is left alone.
     */
    fun setTime(hour: Int, minute: Int) {
        val shown = datePicker.date.hourAndMinute()
        if (shown.hour.toInt() == hour && shown.minute.toInt() == minute) return
        datePicker.setDate(dateAt(hour = hour, minute = minute), animated = true)
    }

    private fun NSDate.hourAndMinute(): NSDateComponents =
        NSCalendar.currentCalendar.components(NSCalendarUnitHour or NSCalendarUnitMinute, this)

    private fun dateAt(hour: Int, minute: Int): NSDate {
        val components = NSDateComponents().apply {
            this.hour = hour.toLong()
            this.minute = minute.toLong()
        }
        return NSCalendar.currentCalendar.dateFromComponents(components) ?: NSDate()
    }
}
