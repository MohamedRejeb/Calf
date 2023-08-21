package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.mohamedrejeb.calf.io.KmpFile
@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    var fileDialogVisible by rememberSaveable { mutableStateOf(false) }


    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                fileDialogVisible = true
            }
        )
    }
}

actual class FilePickerLauncher actual constructor(
    type: FilePickerType,
    selectionMode: FilePickerSelectionMode,
    private val onLaunch: () -> Unit,
) {
    actual fun launch() {
        onLaunch()
    }
}