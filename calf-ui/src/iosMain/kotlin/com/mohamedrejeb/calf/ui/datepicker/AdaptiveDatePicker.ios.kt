package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.utils.surfaceColorAtElevation
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIDatePicker

@OptIn(ExperimentalForeignApi::class, ExperimentalMaterial3Api::class, InternalCalfApi::class,
    ExperimentalComposeUiApi::class
)
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
            datePicker = datePicker,
            displayMode = state.initialUIKitDisplayMode,
            onSelectionChanged = { dateMillis ->
                state.selectedDateMillis = dateMillis
            }
        )
    }

    val absoluteElevation = LocalAbsoluteTonalElevation.current

    val containerColorAtElevation =
        surfaceColorAtElevation(
            color = colors.containerColor,
            elevation = absoluteElevation
        )

    LaunchedEffect(colors, containerColorAtElevation) {
        datePickerManager.applyColors(
            containerColor = containerColorAtElevation,
            dayContentColor = colors.dayContentColor,
            selectedDayContainerColor = colors.selectedDayContainerColor,
        )
    }

    Box(
        modifier = modifier
    ) {
        UIKitView(
            factory = {
                datePicker
            },
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
            ),
            modifier = Modifier
                .background(colors.containerColor)
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
