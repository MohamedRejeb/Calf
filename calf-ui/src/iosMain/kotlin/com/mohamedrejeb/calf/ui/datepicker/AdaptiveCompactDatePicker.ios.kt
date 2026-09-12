package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.mohamedrejeb.calf.core.InternalCalfApi

@OptIn(ExperimentalMaterial3Api::class, InternalCalfApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun AdaptiveCompactDatePicker(
    state: AdaptiveDatePickerState,
    modifier: Modifier,
    enabled: Boolean,
    dateFormatter: DatePickerFormatter,
    colors: DatePickerColors,
    materialPlaceholder: String,
    materialConfirmText: String,
    materialDismissText: String,
    materialTitle: (@Composable () -> Unit)?,
    materialHeadline: (@Composable () -> Unit)?,
) {
    val datePickerManager = rememberDatePickerManager(
        state = state,
        presentation = IosDatePickerPresentation.Compact,
    )

    SyncNativeDatePicker(
        state = state,
        manager = datePickerManager,
        colors = colors,
        enabled = enabled,
    )

    key(datePickerManager) {
        UIKitView(
            factory = {
                datePickerManager.view
            },
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
            ),
            modifier = modifier.size(datePickerManager.viewSize),
        )
    }
}
