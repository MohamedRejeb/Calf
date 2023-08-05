package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun BottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    sheetState: SheetState,
    content: @Composable() (ColumnScope.() -> Unit)
) {

}