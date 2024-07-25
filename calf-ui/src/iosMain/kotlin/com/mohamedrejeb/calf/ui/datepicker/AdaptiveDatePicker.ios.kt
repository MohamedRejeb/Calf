package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.core.InternalCalfApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.setValue
import platform.UIKit.UIColor
import platform.UIKit.UIDatePicker
import platform.UIKit.UILabel
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class, ExperimentalMaterial3Api::class, InternalCalfApi::class, BetaInteropApi::class)
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

    UIKitView(
        factory = {
            datePicker
        },
        background = colors.containerColor,
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

fun UIView.lookForLabels(color: UIColor, level: Int = 1) {
    subviews.forEach {
        val uiView = (it as? UIView) ?: return@forEach
//        println("${"-".repeat(level)} -> $uiView")
        uiView.lookForLabels(color, level = level + 1)

//        if (uiView is UIButton) {
//            println("${"-".repeat(level)} -> btn title: ${uiView.currentTitle}")
//        }

        if (uiView !is UILabel)
            return@forEach

//        println("${"-".repeat(level)} -> text: ${uiView.text}")

        uiView.setValue(color, forKey = "textColor")
    }
}