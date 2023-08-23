package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.io.KmpFile

@Composable
expect fun rememberFilePickerLauncher(
    type: FilePickerFileType = FilePickerFileType.All,
    selectionMode: FilePickerSelectionMode = FilePickerSelectionMode.Single,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher

sealed class FilePickerFileType(vararg val value: String) {
    data object Image: FilePickerFileType("image/*")
    data object Video: FilePickerFileType("video/*")
    data object ImageVideo: FilePickerFileType("image/*", "video/*")
    data object All: FilePickerFileType("*/*")
}

sealed class FilePickerSelectionMode {
    data object Single: FilePickerSelectionMode()
    data object Multiple: FilePickerSelectionMode()
}

expect class FilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onLaunch: () -> Unit,
) {
    fun launch()
}