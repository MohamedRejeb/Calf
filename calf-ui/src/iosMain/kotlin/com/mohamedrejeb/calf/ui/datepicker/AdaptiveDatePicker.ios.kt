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
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.utils.applyLayoutDirection
import com.mohamedrejeb.calf.ui.utils.surfaceColorAtElevation
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(
    ExperimentalForeignApi::class, ExperimentalMaterial3Api::class, InternalCalfApi::class,
    ExperimentalComposeUiApi::class,
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
    val datePickerManager = rememberDatePickerManager(
        state = state,
        presentation = when (state.initialUIKitDisplayMode) {
            UIKitDisplayMode.Picker -> IosDatePickerPresentation.Inline
            else -> IosDatePickerPresentation.Wheels
        },
    )

    SyncNativeDatePicker(
        state = state,
        manager = datePickerManager,
        colors = colors,
        enabled = true,
    )

    Box(
        modifier = modifier
    ) {
        key(datePickerManager) {
            UIKitView(
                factory = {
                    datePickerManager.view
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
}

/** Creates the native picker for [state] and recreates it if a different state is passed. */
@OptIn(ExperimentalMaterial3Api::class, InternalCalfApi::class)
@Composable
internal fun rememberDatePickerManager(
    state: AdaptiveDatePickerState,
    presentation: IosDatePickerPresentation,
): DatePickerManager = remember(state) {
    DatePickerManager(
        initialSelectedDateMillis = state.selectedDateMillis,
        presentation = presentation,
        onSelectionChanged = { dateMillis ->
            state.selectedDateMillis = dateMillis
        },
        isDaySelectable = { dateMillis ->
            state.isDaySelectable(dateMillis)
        },
        resolveSelection = { pickedMillis ->
            resolvePickedDay(pickedMillis, state.dateBounds, state.selectableDates)
        },
    )
}

/** Pushes [state], colors, layout direction and the enabled flag into the native picker. */
@OptIn(ExperimentalMaterial3Api::class, InternalCalfApi::class)
@Composable
internal fun SyncNativeDatePicker(
    state: AdaptiveDatePickerState,
    manager: DatePickerManager,
    colors: DatePickerColors,
    enabled: Boolean,
) {
    val layoutDirection = LocalLayoutDirection.current
    val absoluteElevation = LocalAbsoluteTonalElevation.current
    val containerColorAtElevation = surfaceColorAtElevation(
        color = colors.containerColor,
        elevation = absoluteElevation,
    )

    LaunchedEffect(layoutDirection) {
        manager.view.applyLayoutDirection(layoutDirection)
    }

    LaunchedEffect(state.selectableDates) {
        val bounds = state.dateBounds
        manager.applyDateBounds(
            minDateMillis = bounds.minDateMillis,
            maxDateMillis = bounds.maxDateMillis,
        )
        manager.updateSelectableDates()
    }

    LaunchedEffect(state.selectedDateMillis) {
        manager.setSelectedDate(state.selectedDateMillis)
    }

    LaunchedEffect(enabled) {
        manager.setEnabled(enabled)
    }

    LaunchedEffect(colors, containerColorAtElevation) {
        manager.applyColors(
            containerColor = containerColorAtElevation,
            dayContentColor = colors.dayContentColor,
            selectedDayContainerColor = colors.selectedDayContainerColor,
        )
    }
}
