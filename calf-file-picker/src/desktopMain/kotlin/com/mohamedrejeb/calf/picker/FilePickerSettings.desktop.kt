package com.mohamedrejeb.calf.picker

import androidx.compose.ui.awt.ComposeWindow

actual class FilePickerSettings(
    actual val title: String? = null,
    actual val initialDirectory: String? = null,
    /**
     * The parent window for the file picker dialog.
     * On Windows this ensures the dialog appears in front of the app window.
     * On macOS/Linux this is optional.
     */
    val parentWindow: ComposeWindow? = null,
)

actual fun defaultFilePickerSettings(): FilePickerSettings = FilePickerSettings()
