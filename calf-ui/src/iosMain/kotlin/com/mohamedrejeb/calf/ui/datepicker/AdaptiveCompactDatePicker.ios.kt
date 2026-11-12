package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.DialogProperties
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.utils.SafeAreaIsolatingView

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
    materialShowModeToggle: Boolean,
    materialFocusRequester: FocusRequester?,
    materialDialogShape: Shape,
    materialDialogTonalElevation: Dp,
    materialDialogProperties: DialogProperties,
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
                SafeAreaIsolatingView(datePickerManager.view)
            },
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
            ),
            modifier = modifier.size(datePickerManager.viewSize),
        )
    }
}
