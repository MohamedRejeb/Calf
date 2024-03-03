package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.runtime.mutableStateOf
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.Foundation.*
import platform.UIKit.*
import platform.objc.sel_registerName

@OptIn(ExperimentalForeignApi::class)
class DatePickerManager internal constructor(
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
            stripTimeFromDate(datePicker.date).timeIntervalSince1970.toLong() * 1000
        )
    }

    val datePickerWidth = mutableStateOf(0f)
    val datePickerHeight = mutableStateOf(0f)

    init {
        datePicker.date = NSDate()
        datePicker.locale = NSLocale.currentLocale
        datePicker.datePickerMode = UIDatePickerMode.UIDatePickerModeDate
        datePicker.preferredDatePickerStyle = when(displayMode) {
            UIKitDisplayMode.Picker -> UIDatePickerStyle.UIDatePickerStyleInline
            else -> UIDatePickerStyle.UIDatePickerStyleWheels
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
    }

    private fun stripTimeFromDate(originalDate: NSDate): NSDate {
        val components = NSCalendar.currentCalendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            originalDate
        )
        return NSCalendar.currentCalendar.dateFromComponents(components) ?: originalDate
    }
}