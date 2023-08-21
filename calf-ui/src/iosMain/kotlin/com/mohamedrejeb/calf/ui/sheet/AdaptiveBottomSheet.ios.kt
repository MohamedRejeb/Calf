package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun AdaptiveBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    adaptiveSheetState: AdaptiveSheetState,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    tonalElevation: Dp,
    scrimColor: Color,
    dragHandle: @Composable() (() -> Unit)?,
    windowInsets: WindowInsets,
    content: @Composable() (ColumnScope.() -> Unit)
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes

    val sheetHelper = remember {
        BottomSheetManager(
            onDismiss = {
                onDismissRequest()
            },
            content = {
                MaterialTheme(
                    colorScheme = colorScheme,
                    typography = typography,
                    shapes = shapes,
                ) {
                    Column {
                        content()
                    }
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        adaptiveSheetState.show()
    }

    LaunchedEffect(adaptiveSheetState.sheetValue) {
        println(adaptiveSheetState.sheetValue)
        if (adaptiveSheetState.sheetValue == SheetValue.Hidden) {
            sheetHelper.hide(
                completion = {
                    adaptiveSheetState.deferredUntilHidden.complete(Unit)
                }
            )
        } else {
            sheetHelper.show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sheetHelper.hide()
            adaptiveSheetState.sheetValue = SheetValue.Hidden
        }
    }
}