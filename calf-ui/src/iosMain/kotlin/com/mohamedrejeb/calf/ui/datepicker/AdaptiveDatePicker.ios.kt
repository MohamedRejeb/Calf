package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.mohamedrejeb.calf.core.InternalCalfApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIDatePicker

@OptIn(ExperimentalForeignApi::class, ExperimentalMaterial3Api::class, InternalCalfApi::class)
@Composable
actual fun AdaptiveDatePicker(
    state: AdaptiveDatePickerState,
    modifier: Modifier,
    dateFormatter: DatePickerFormatter,
    title: @Composable (() -> Unit)?,
    headline: @Composable (() -> Unit)?,
    showModeToggle: Boolean,
    colors: DatePickerColors
) {
    val datePicker = remember {
        UIDatePicker()
    }

    val datePickerManager = remember {
        DatePickerManager(
            initialSelectedDateMillis = state.selectedDateMillis,
            colors = colors,
            datePicker = datePicker,
            displayMode = state.initialUIKitDisplayMode,
            onSelectionChanged = { dateMillis ->
                state.selectedDateMillis = dateMillis
            }
        )
    }

    LaunchedEffect(colors) {
        datePickerManager.applyColors(colors)
    }

    Box(
        modifier = modifier
    ) {
        UIKitView(
            factory = {
                datePicker
            },
            onResize = { _, size ->
                datePicker.setFrame(size)
            },
            background = colors.containerColor,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (datePickerManager.aspectRatio.isFinite() && datePickerManager.aspectRatio > 0f)
                        Modifier
                            .aspectRatio(datePickerManager.aspectRatio)
                    else
                        Modifier
                )
        )
    }
}
