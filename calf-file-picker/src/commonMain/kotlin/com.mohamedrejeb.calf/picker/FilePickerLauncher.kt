package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.io.KmpFile

@Composable
expect fun rememberFilePickerLauncher(
    type: FilePickerType = FilePickerType.Any,
    selectionMode: FilePickerSelectionMode = FilePickerSelectionMode.Single,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher

sealed class FilePickerType(vararg val value: String) {
    data object Image: FilePickerType("image/*")
    data object Video: FilePickerType("video/*")
    data object ImageVideo: FilePickerType("image/*", "video/*")
    data object Any: FilePickerType("*/*")
}

sealed class FilePickerSelectionMode {
    data object Single: FilePickerSelectionMode()
    data object Multiple: FilePickerSelectionMode()
}

expect class FilePickerLauncher(
    type: FilePickerType = FilePickerType.Any,
    selectionMode: FilePickerSelectionMode = FilePickerSelectionMode.Single,
    onLaunch: () -> Unit,
) {
    fun launch()
}