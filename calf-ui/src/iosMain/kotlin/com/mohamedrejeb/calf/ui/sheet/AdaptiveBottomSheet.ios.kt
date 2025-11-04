package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.utils.surfaceColorAtElevation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun AdaptiveBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    adaptiveSheetState: AdaptiveSheetState,
    sheetMaxWidth: Dp,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    tonalElevation: Dp,
    scrimColor: Color,
    dragHandle: @Composable (() -> Unit)?,
    contentWindowInsets: @Composable () -> WindowInsets,
    properties: ModalBottomSheetProperties,
    content: @Composable ColumnScope.() -> Unit,
) {
    val compositionLocalContext = rememberUpdatedState(currentCompositionLocalContext)
    val currentUIViewController = LocalUIViewController.current

    val modifierState = rememberUpdatedState(modifier)
    val shapeState = rememberUpdatedState(shape)
    val containerColorState = rememberUpdatedState(containerColor)
    val contentColorState = rememberUpdatedState(contentColor)
    val tonalElevationState = rememberUpdatedState(tonalElevation)
    val contentState = rememberUpdatedState(content)
    val onDismissRequestState = rememberUpdatedState(onDismissRequest)
    val confirmValueChangeState = rememberUpdatedState(adaptiveSheetState.confirmValueChange)

    val isDark = isSystemInDarkTheme()

    val absoluteElevation = LocalAbsoluteTonalElevation.current + tonalElevation

    val containerColorAtElevation =
        surfaceColorAtElevation(
            color = containerColor,
            elevation = absoluteElevation
        )

    val sheetManager = remember(currentUIViewController) {
        BottomSheetManager(
            parentUIViewController = currentUIViewController,
            isDark = isDark,
            containerColor = containerColorAtElevation,
            onDismiss = {
                onDismissRequestState.value.invoke()
            },
            confirmValueChange = {
                confirmValueChangeState.value.invoke(it)
            },
            content = {
                val sheetCompositionLocalContext = currentCompositionLocalContext

                CompositionLocalProvider(compositionLocalContext.value) {
                    CompositionLocalProvider(sheetCompositionLocalContext) {
                        Surface(
                            shape = shapeState.value,
                            color = containerColorState.value,
                            contentColor = contentColorState.value,
                            tonalElevation = tonalElevationState.value,
                            modifier = modifierState.value.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                content = contentState.value,
                            )
                        }
                    }
                }
            }
        )
    }

    /**
     * Update adaptive sheet state properties
     */

    LaunchedEffect(adaptiveSheetState, sheetManager) {
        adaptiveSheetState.iosSheetManager = sheetManager
    }

    LaunchedEffect(adaptiveSheetState, dragHandle) {
        adaptiveSheetState.showDragHandle = dragHandle != null
    }

    /**
     * Update ios sheet manager properties
     */

    LaunchedEffect(sheetManager, isDark) {
        sheetManager.applyTheme(isDark)
    }

    LaunchedEffect(sheetManager, containerColorAtElevation) {
        sheetManager.applyContainerColor(containerColorAtElevation)
    }

    /**
     * Show sheet on init
     */

    LaunchedEffect(adaptiveSheetState) {
        adaptiveSheetState.show()
    }

    /**
     * Hide sheet on dispose
     */

    DisposableEffect(Unit) {
        onDispose {
            sheetManager.hide()
            adaptiveSheetState.iosCurrentValue = SheetValue.Hidden
            adaptiveSheetState.iosTargetValue = SheetValue.Hidden
        }
    }
}