package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.interop.LocalUIViewController
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
    val compositionLocalContext = rememberUpdatedState(currentCompositionLocalContext)
    val currentUIViewController = LocalUIViewController.current

    val isDark = isSystemInDarkTheme()

    val sheetManager = remember(currentUIViewController) {
        BottomSheetManager(
            parentUIViewController = currentUIViewController,
            dark = isDark,
            onDismiss = {
                onDismissRequest()
            },
            content = {
                val sheetCompositionLocalContext = currentCompositionLocalContext

                CompositionLocalProvider(compositionLocalContext.value) {
                    CompositionLocalProvider(sheetCompositionLocalContext) {
                        // TODO: To remove on 1.7.0
                        if (!adaptiveSheetState.skipPartiallyExpanded) {
                            var update by remember { mutableIntStateOf(0) }

                            LaunchedEffect(Unit) {
                                while (true) {
                                    withFrameMillis {
                                        update++
                                    }
                                }
                            }

                            @Suppress("UNUSED_EXPRESSION")
                            update
                        }

                        Column(
                            modifier = modifier,
                            content = content,
                        )
                    }
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        adaptiveSheetState.show()
    }

    LaunchedEffect(isDark) {
        sheetManager.applyTheme(isDark)
    }

    LaunchedEffect(adaptiveSheetState.sheetValue) {
        if (adaptiveSheetState.sheetValue == SheetValue.Hidden) {
            sheetManager.hide(
                completion = {
                    adaptiveSheetState.deferredUntilHidden.complete(Unit)
                }
            )
        } else {
            sheetManager.show(adaptiveSheetState.skipPartiallyExpanded)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sheetManager.hide()
            adaptiveSheetState.sheetValue = SheetValue.Hidden
        }
    }
}