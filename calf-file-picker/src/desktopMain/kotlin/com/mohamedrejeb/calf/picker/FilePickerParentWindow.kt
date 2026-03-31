package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.FrameWindowScope

/**
 * CompositionLocal for providing a parent [ComposeWindow] to all file pickers
 * in the composition tree.
 *
 * When provided, all [rememberFilePickerLauncher] calls will use this window
 * as the parent for native dialogs, unless overridden via [FilePickerSettings.parentWindow].
 *
 * Use [ProvideFilePickerParentWindow] for convenient setup.
 */
val LocalFilePickerParentWindow = staticCompositionLocalOf<ComposeWindow?> { null }

/**
 * Provides the current [FrameWindowScope.window] to all file pickers in [content].
 *
 * Usage:
 * ```
 * Window(title = "My App", onCloseRequest = ::exitApplication) {
 *     ProvideFilePickerParentWindow {
 *         App()
 *     }
 * }
 * ```
 */
@Composable
fun FrameWindowScope.ProvideFilePickerParentWindow(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalFilePickerParentWindow provides window) {
        content()
    }
}
