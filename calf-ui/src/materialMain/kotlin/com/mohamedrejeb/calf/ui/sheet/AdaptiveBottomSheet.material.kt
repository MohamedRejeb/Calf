package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
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
    dragHandle: @Composable (() -> Unit)?,
    windowInsets: WindowInsets,
    content: @Composable (ColumnScope.() -> Unit),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = adaptiveSheetState.materialSheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        windowInsets = windowInsets,
        content = content,
    )
}
