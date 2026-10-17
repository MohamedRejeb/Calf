package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile

/**
 * Remembers a [FileSaverLauncher] that can save a file to a user-chosen location.
 *
 * Failures are reported through [onError], and [onResult] is not called for them: no app on the
 * device can save files, the source could not be read, or the chosen file could not be written.
 *
 * @param settings Dialog settings (title, initial directory, etc.).
 * @param onError Called when saving fails. Cancelling the dialog is not a failure.
 * @param onResult Called with the saved [KmpFile], or `null` if the user cancelled.
 */
@ExperimentalCalfApi
@Composable
expect fun rememberFileSaverLauncher(
    settings: FilePickerSettings = defaultFilePickerSettings(),
    onError: (FilePickerException) -> Unit,
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher

/**
 * Remembers a [FileSaverLauncher] that can save a file to a user-chosen location. A failure is
 * reported as `null`, like a cancellation; use the overload with `onError` to tell them apart.
 *
 * @param settings Dialog settings (title, initial directory, etc.).
 * @param onResult Called with the saved [KmpFile], or `null` if the user cancelled or saving failed.
 */
@ExperimentalCalfApi
@Composable
fun rememberFileSaverLauncher(
    settings: FilePickerSettings = defaultFilePickerSettings(),
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher = rememberFileSaverLauncher(
    settings = settings,
    onError = { onResult(null) },
    onResult = onResult,
)

/**
 * Launcher for saving a file. Call [launch] with the bytes or a [KmpFile] and file name
 * to trigger the platform save dialog.
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

    /**
     * Launches the platform save dialog using a [KmpFile] as the source.
     *
     * This avoids loading the entire file content into memory. On each platform,
     * the file is copied or streamed directly to the destination chosen by the user.
     *
     * @param file The source file to save.
     * @param baseName The suggested file name without extension (e.g. "document").
     * @param extension The file extension without dot (e.g. "pdf").
     * @param initialDirectory Optional initial directory for the save dialog.
     */
    fun launch(
        file: KmpFile,
        baseName: String,
        extension: String,
        initialDirectory: String? = null,
    )
}
