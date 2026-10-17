package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.window.FrameWindowScope
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.platform.PlatformFilePicker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    settings: FilePickerSettings,
    onError: (FilePickerException) -> Unit,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    // Resolve parent window: settings.parentWindow > LocalFilePickerParentWindow > null
    val localWindow = LocalFilePickerParentWindow.current
    val resolvedSettings = remember(
        settings.title,
        settings.initialDirectory,
        settings.parentWindow,
        localWindow,
    ) {
        if (settings.parentWindow == null && localWindow != null) {
            FilePickerSettings(
                title = settings.title,
                initialDirectory = settings.initialDirectory,
                parentWindow = localWindow,
            )
        } else {
            settings
        }
    }

    return rememberFilePickerLauncherInternal(type, selectionMode, resolvedSettings, onError, onResult)
}

/**
 * Desktop-only extension that automatically captures the [FrameWindowScope.window]
 * as the parent window for the file picker dialog. Failures are not reported; use the overload
 * with `onError` to observe them.
 */
@Composable
fun FrameWindowScope.rememberFilePickerLauncher(
    type: FilePickerFileType = FilePickerFileType.All,
    selectionMode: FilePickerSelectionMode = FilePickerSelectionMode.Single,
    settings: FilePickerSettings = defaultFilePickerSettings(),
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher = rememberFilePickerLauncher(
    type = type,
    selectionMode = selectionMode,
    settings = settings,
    onError = {},
    onResult = onResult,
)

/**
 * Desktop-only extension that automatically captures the [FrameWindowScope.window]
 * as the parent window for the file picker dialog.
 *
 * @param onError Called when the dialog cannot be created or shown, for example when the native
 * library cannot be loaded. Cancelling the dialog is not a failure.
 */
@Composable
fun FrameWindowScope.rememberFilePickerLauncher(
    type: FilePickerFileType = FilePickerFileType.All,
    selectionMode: FilePickerSelectionMode = FilePickerSelectionMode.Single,
    settings: FilePickerSettings = defaultFilePickerSettings(),
    onError: (FilePickerException) -> Unit,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val resolvedSettings = remember(
        settings.title,
        settings.initialDirectory,
        settings.parentWindow,
        window,
    ) {
        if (settings.parentWindow == null) {
            FilePickerSettings(
                title = settings.title,
                initialDirectory = settings.initialDirectory,
                parentWindow = window,
            )
        } else {
            settings
        }
    }

    return rememberFilePickerLauncherInternal(type, selectionMode, resolvedSettings, onError, onResult)
}

@Composable
private fun rememberFilePickerLauncherInternal(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    settings: FilePickerSettings,
    onError: (FilePickerException) -> Unit,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnError by rememberUpdatedState(onError)
    val currentType by rememberUpdatedState(type)

    val dialogHandle = remember { mutableLongStateOf(0L) }
    // Why the native dialog could not be created, reported when the user tries to open it.
    val dialogFailure = remember { mutableStateOf<Throwable?>(null) }
    // Mutex ensures launch() and settings updates never overlap.
    // Settings update acquires the lock to safely destroy/recreate the handle.
    // launch() acquires the lock to show the dialog with a valid handle.
    val dialogMutex = remember { Mutex() }

    // Create the native dialog handle off the main thread
    LaunchedEffect(
        type,
        selectionMode,
        settings.title,
        settings.initialDirectory,
        settings.parentWindow,
    ) {
        dialogMutex.withLock {
            val oldHandle = dialogHandle.longValue
            if (oldHandle != 0L) {
                PlatformFilePicker.destroyDialog(oldHandle)
                dialogHandle.longValue = 0L
            }

            val title = settings.title
                ?: if (type == FilePickerFileType.Folder) "Select a folder" else "Select a file"

            val handle = createDialogOrNull(dialogFailure) {
                if (type == FilePickerFileType.Folder) {
                    PlatformFilePicker.createDirectoryPickerHandle(
                        initialDirectory = settings.initialDirectory,
                        title = title,
                        parentWindow = settings.parentWindow,
                    )
                } else {
                    PlatformFilePicker.createFilePickerHandle(
                        initialDirectory = settings.initialDirectory,
                        type = type,
                        selectionMode = selectionMode,
                        title = title,
                        parentWindow = settings.parentWindow,
                    )
                }
            }
            dialogHandle.longValue = handle
        }
    }

    // Free native memory when leaving composition
    DisposableEffect(Unit) {
        onDispose {
            val handle = dialogHandle.longValue
            if (handle != 0L) {
                PlatformFilePicker.destroyDialog(handle)
            }
        }
    }

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                scope.launch {
                    dialogMutex.withLock {
                        val handle = dialogHandle.longValue
                        if (handle == 0L) {
                            dialogFailure.value?.let { failure ->
                                currentOnError(FilePickerException("Could not create the file dialog", failure))
                            }
                            return@launch
                        }

                        val picked = try {
                            if (currentType == FilePickerFileType.Folder) {
                                listOfNotNull(PlatformFilePicker.showDirectoryPicker(handle))
                            } else {
                                val maxItems = (selectionMode as? FilePickerSelectionMode.Multiple)?.maxItems
                                val files = PlatformFilePicker.showFilePicker(handle)
                                if (maxItems != null) files.take(maxItems) else files
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Throwable) {
                            currentOnError(FilePickerException("Could not show the file dialog", e))
                            return@launch
                        }
                        currentOnResult(picked.map { KmpFile(it) })
                    }
                }
            },
        )
    }
}

/**
 * Creates a native dialog off the main thread. A failure, such as the native library failing to
 * load, is kept in [failure] and the handle is 0.
 */
private suspend fun createDialogOrNull(failure: MutableState<Throwable?>, create: () -> Long): Long =
    withContext(Dispatchers.IO) {
        try {
            create().also { failure.value = null }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            failure.value = e
            0L
        }
    }

@Stable
actual class FilePickerLauncher actual constructor(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    private val onLaunch: () -> Unit,
) {
    actual fun launch() {
        onLaunch()
    }
}
