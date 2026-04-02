package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile

/**
 * Remembers a [FileSaverLauncher] that can save a file to a user-chosen location.
 *
 * @param settings Dialog settings (title, initial directory, etc.).
 * @param onResult Called with the saved [KmpFile], or `null` if the user cancelled.
 */
@ExperimentalCalfApi
@Composable
expect fun rememberFileSaverLauncher(
    settings: FilePickerSettings = defaultFilePickerSettings(),
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher

/**
 * Launcher for saving a file. Call [launch] with the bytes and file name to trigger
 * the platform save dialog.
 *
 * @see rememberFileSaverLauncher
 */
@ExperimentalCalfApi
@Stable
expect class FileSaverLauncher {
    /**
     * Launches the platform save dialog.
     *
     * @param bytes The file content to save, or `null` to create an empty file.
     * @param baseName The suggested file name without extension (e.g. "document").
     * @param extension The file extension without dot (e.g. "pdf").
     * @param initialDirectory Optional initial directory for the save dialog.
     */
    fun launch(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String? = null,
    )
}
