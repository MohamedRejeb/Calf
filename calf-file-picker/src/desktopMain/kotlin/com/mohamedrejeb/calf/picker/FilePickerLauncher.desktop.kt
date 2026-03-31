package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.window.FrameWindowScope
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.platform.PlatformFilePicker
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

    return rememberFilePickerLauncherInternal(type, selectionMode, resolvedSettings, onResult)
}

/**
 * Desktop-only extension that automatically captures the [FrameWindowScope.window]
 * as the parent window for the file picker dialog.
 */
@Composable
fun FrameWindowScope.rememberFilePickerLauncher(
    type: FilePickerFileType = FilePickerFileType.All,
    selectionMode: FilePickerSelectionMode = FilePickerSelectionMode.Single,
    settings: FilePickerSettings = defaultFilePickerSettings(),
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

    return rememberFilePickerLauncherInternal(type, selectionMode, resolvedSettings, onResult)
}

@Composable
private fun rememberFilePickerLauncherInternal(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    settings: FilePickerSettings,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    val currentType by rememberUpdatedState(type)

    val dialogHandle = remember { mutableLongStateOf(0L) }
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

            val handle = withContext(Dispatchers.IO) {
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
                        if (handle == 0L) return@launch

                        if (currentType == FilePickerFileType.Folder) {
                            val file = PlatformFilePicker.showDirectoryPicker(handle)
                            currentOnResult(
                                if (file == null)
                                    emptyList()
                                else
                                    listOf(KmpFile(file))
                            )
                        } else {
                            val files = PlatformFilePicker.showFilePicker(handle)
                            val maxItems = (selectionMode as? FilePickerSelectionMode.Multiple)?.maxItems
                            val result = files.map { KmpFile(it) }
                            currentOnResult(if (maxItems != null) result.take(maxItems) else result)
                        }
                    }
                }
            },
        )
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
