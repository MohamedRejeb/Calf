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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.mohamedrejeb.calf.ui.utils.applyLayoutDirection
import com.mohamedrejeb.calf.ui.utils.isIOSVersionAtLeast
import com.mohamedrejeb.calf.ui.utils.surfaceColorAtElevation

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
actual fun AdaptiveDateRangePicker(
    state: AdaptiveDateRangePickerState,
    modifier: Modifier,
    dateFormatter: DatePickerFormatter,
    title: (@Composable () -> Unit)?,
    headline: (@Composable () -> Unit)?,
    showModeToggle: Boolean,
    colors: DatePickerColors,
) {
    if (!isIOSVersionAtLeast(FIRST_IOS_WITH_CALENDAR_VIEW)) {
        MaterialDateRangePicker(
            state = state,
            modifier = modifier,
            dateFormatter = dateFormatter,
            title = title,
            headline = headline,
            showModeToggle = showModeToggle,
            colors = colors,
        )
        return
    }

    // Recreated if a different state is passed, so taps never reach a stale state.
    val backend = remember(state) {
        CalendarRangePickerBackend(
            initialSelection = DateRangeSelection(
                start = state.selectedStartDateMillis,
                end = state.selectedEndDateMillis,
            ),
            onSelectionChanged = { selection ->
                state.setSelection(selection.start, selection.end)
            },
            isDaySelectable = { dateMillis ->
                state.isDaySelectable(dateMillis)
            },
        )
    }
    val aspectRatio = remember(backend) {
        backend.aspectRatio.takeIf { it > 0f } ?: inlineDatePickerAspectRatio()
    }

    val layoutDirection = LocalLayoutDirection.current
    val absoluteElevation = LocalAbsoluteTonalElevation.current
    val containerColorAtElevation = surfaceColorAtElevation(
        color = colors.containerColor,
        elevation = absoluteElevation,
    )

    LaunchedEffect(layoutDirection) {
        backend.view.applyLayoutDirection(layoutDirection)
    }

    LaunchedEffect(state.selectableDates) {
        val bounds = state.dateBounds
        backend.applyDateBounds(bounds.minDateMillis, bounds.maxDateMillis)
    }

    LaunchedEffect(state.selectedStartDateMillis, state.selectedEndDateMillis) {
        backend.setSelectedRange(
            DateRangeSelection(
                start = state.selectedStartDateMillis,
                end = state.selectedEndDateMillis,
            ),
        )
    }

    LaunchedEffect(colors, containerColorAtElevation) {
        backend.applyColors(
            containerColor = containerColorAtElevation,
            dayContentColor = colors.dayContentColor,
            selectedDayContainerColor = colors.selectedDayContainerColor,
        )
    }

    Box(modifier = modifier) {
        key(backend) {
            UIKitView(
                factory = { backend.view },
                properties = UIKitInteropProperties(
                    interactionMode = UIKitInteropInteractionMode.NonCooperative,
                ),
                modifier = Modifier
                    .background(colors.containerColor)
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio),
            )
        }
    }
}
