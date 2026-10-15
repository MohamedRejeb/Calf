package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.platform.PlatformFilePicker
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@ExperimentalCalfApi
@Composable
actual fun rememberFileSaverLauncher(
    settings: FilePickerSettings,
    onError: (FilePickerException) -> Unit,
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnError by rememberUpdatedState(onError)
    val currentSettings by rememberUpdatedState(settings)

    // Resolve parent window from local if not set explicitly
    val localWindow = LocalFilePickerParentWindow.current
    val resolvedParentWindow = settings.parentWindow ?: localWindow

    // Shows the save dialog and writes the chosen file with [save]; null means the user cancelled.
    val launchSave: (String, String, String?, suspend (Long) -> File?) -> Unit =
        { baseName, extension, initialDirectory, save ->
            scope.launch {
                val saved = try {
                    val handle = PlatformFilePicker.createSaveDialogHandle(
                        initialDirectory = initialDirectory ?: currentSettings.initialDirectory,
                        baseName = baseName,
                        extension = extension,
                        title = currentSettings.title,
                        parentWindow = resolvedParentWindow,
                    )
                    try {
                        save(handle)
                    } finally {
                        PlatformFilePicker.destroyDialog(handle)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    currentOnError(FilePickerException("Could not save the file", e))
                    return@launch
                }
                currentOnResult(saved?.let { KmpFile(it) })
            }
        }

    return remember {
        FileSaverLauncher(
            onLaunch = { bytes, baseName, extension, initialDirectory ->
                launchSave(baseName, extension, initialDirectory) { handle ->
                    PlatformFilePicker.showSaveDialog(handle, bytes)
                }
            },
            onLaunchFile = { kmpFile, baseName, extension, initialDirectory ->
                launchSave(baseName, extension, initialDirectory) { handle ->
                    PlatformFilePicker.showSaveDialogWithFile(handle, kmpFile.file)
                }
            },
        )
    }
}

@ExperimentalCalfApi
@Stable
actual class FileSaverLauncher(
    private val onLaunch: (
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) -> Unit,
    private val onLaunchFile: (
        file: KmpFile,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) -> Unit,
) {
    actual fun launch(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) {
        onLaunch(bytes, baseName, extension, initialDirectory)
    }

    actual fun launch(
        file: KmpFile,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) {
        onLaunchFile(file, baseName, extension, initialDirectory)
    }

    /**
     * Launches the platform save dialog using a [java.io.File] as the source.
     *
     * @param file The source file to save.
     * @param baseName The suggested file name without extension (e.g. "document").
     * @param extension The file extension without dot (e.g. "pdf").
     * @param initialDirectory Optional initial directory for the save dialog.
     */
    fun launch(
        file: File,
        baseName: String,
        extension: String,
        initialDirectory: String? = null,
    ) {
        launch(KmpFile(file), baseName, extension, initialDirectory)
    }
}
