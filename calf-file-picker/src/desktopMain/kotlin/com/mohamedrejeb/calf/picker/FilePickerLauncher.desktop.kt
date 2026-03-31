package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.platform.PlatformFilePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)

    val dialogHandle = remember { mutableLongStateOf(0L) }

    // Create the native dialog handle off the main thread
    LaunchedEffect(type, selectionMode) {
        // Destroy previous handle if type/selectionMode changed
        val oldHandle = dialogHandle.longValue
        if (oldHandle != 0L) {
            PlatformFilePicker.destroyDialog(oldHandle)
            dialogHandle.longValue = 0L
        }

        val handle = withContext(Dispatchers.IO) {
            if (type == FilePickerFileType.Folder) {
                PlatformFilePicker.createDirectoryPickerHandle(
                    initialDirectory = null,
                    title = "Select a folder",
                )
            } else {
                PlatformFilePicker.createFilePickerHandle(
                    initialDirectory = null,
                    type = type,
                    selectionMode = selectionMode,
                    title = "Select a file",
                )
            }
        }
        dialogHandle.longValue = handle
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
                    val handle = dialogHandle.longValue
                    if (handle == 0L) return@launch

                    if (type == FilePickerFileType.Folder) {
                        val file = PlatformFilePicker.showDirectoryPicker(handle)
                        currentOnResult(
                            if (file == null)
                                emptyList()
                            else
                                listOf(KmpFile(file))
                        )
                    } else {
                        val files = PlatformFilePicker.showFilePicker(handle)
                        currentOnResult(files.map { KmpFile(it) })
                    }
                }
            },
        )
    }
}

actual class FilePickerLauncher actual constructor(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    private val onLaunch: () -> Unit,
) {
    actual fun launch() {
        onLaunch()
    }
}
