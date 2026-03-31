package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

    val currentType by rememberUpdatedState(type)
    val currentSelectionMode by rememberUpdatedState(selectionMode)
    val currentOnResult by rememberUpdatedState(onResult)

    // Eagerly load native library on first composition to avoid first-launch delay
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            PlatformFilePicker.warmup()
        }
    }

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                scope.launch {
                    if (currentType == FilePickerFileType.Folder) {
                        val file = PlatformFilePicker.launchDirectoryPicker(
                            initialDirectory = null,
                            title = "Select a folder",
                        )
                        currentOnResult(
                            if (file == null)
                                emptyList()
                            else
                                listOf(KmpFile(file))
                        )
                    } else {
                        val files = PlatformFilePicker.launchFilePicker(
                            initialDirectory = null,
                            type = currentType,
                            selectionMode = currentSelectionMode,
                            title = "Select a file",
                        )
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
