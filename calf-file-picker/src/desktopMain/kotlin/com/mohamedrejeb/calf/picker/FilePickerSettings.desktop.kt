package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposeWindow

internal data class DesktopFilePickerSettings(
    override val title: String? = null,
    override val initialDirectory: String? = null,
    override val imageRepresentationMode: ImageRepresentationMode = ImageRepresentationMode.Compatible,
    /**
     * The parent window for the file picker dialog.
     * On Windows this ensures the dialog appears in front of the app window.
     * On macOS/Linux this is optional.
     */
    val parentWindow: ComposeWindow? = null,
) : FilePickerSettings

/**
 * Creates a [FilePickerSettings] with desktop-specific [parentWindow] support.
 */
fun FilePickerSettings(
    title: String? = null,
    initialDirectory: String? = null,
    imageRepresentationMode: ImageRepresentationMode = ImageRepresentationMode.Compatible,
    parentWindow: ComposeWindow? = null,
): FilePickerSettings = DesktopFilePickerSettings(
    title = title,
    initialDirectory = initialDirectory,
    imageRepresentationMode = imageRepresentationMode,
    parentWindow = parentWindow,
)

/**
 * Accesses the parent window from a [FilePickerSettings] on Desktop.
 * Returns `null` if the settings were not created with a parent window.
 */
val FilePickerSettings.parentWindow: ComposeWindow?
    get() = (this as? DesktopFilePickerSettings)?.parentWindow

/**
 * Remembers a [FilePickerSettings] instance with desktop-specific [parentWindow] support.
 */
@Composable
fun rememberFilePickerSettings(
    title: String? = null,
    initialDirectory: String? = null,
    imageRepresentationMode: ImageRepresentationMode = ImageRepresentationMode.Compatible,
    parentWindow: ComposeWindow? = null,
): FilePickerSettings = remember(title, initialDirectory, imageRepresentationMode, parentWindow) {
    FilePickerSettings(
        title = title,
        initialDirectory = initialDirectory,
        imageRepresentationMode = imageRepresentationMode,
        parentWindow = parentWindow,
    )
}
