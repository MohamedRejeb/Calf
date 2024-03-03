package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIDatePicker

@OptIn(ExperimentalMaterial3Api::class, ExperimentalForeignApi::class)
@Composable
actual fun AdaptiveTimePicker(
    state: AdaptiveTimePickerState,
    modifier: Modifier,
    colors: TimePickerColors,
    layoutType: TimePickerLayoutType,
) {
    val datePicker = remember {
        UIDatePicker()
    }
    val datePickerManager = remember {
        TimePickerManager(
            datePicker = datePicker,
            initialMinute = state.minute,
            initialHour = state.hour,
            is24Hour = state.is24hour,
            onHourChanged = { hour ->
                state.hourState = hour
            },
            onMinuteChanged = { minute ->
                state.minuteState = minute
            }
        )
    }

    UIKitView(
        factory = {
            datePicker
        },
        modifier = modifier
            .then(
                if (datePickerManager.datePickerWidth.value > 0f)
                    Modifier.width(datePickerManager.datePickerWidth.value.dp)
                else
                    Modifier
            )
            .then(
                if (datePickerManager.datePickerHeight.value > 0f)
                    Modifier.height(datePickerManager.datePickerHeight.value.dp)
                else
                    Modifier
            )
    )
}