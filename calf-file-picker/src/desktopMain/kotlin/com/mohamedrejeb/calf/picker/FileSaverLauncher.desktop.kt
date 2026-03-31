package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.platform.PlatformFilePicker
import kotlinx.coroutines.launch

@ExperimentalCalfApi
@Composable
fun rememberFileSaverLauncher(
    settings: FilePickerSettings = defaultFilePickerSettings(),
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()

    val currentOnResult by rememberUpdatedState(onResult)

    // Resolve parent window from local if not set explicitly
    val localWindow = LocalFilePickerParentWindow.current
    val resolvedParentWindow = settings.parentWindow ?: localWindow

    return remember {
        FileSaverLauncher(
            onLaunch = { bytes, baseName, extension, initialDirectory ->
                scope.launch {
                    val handle = PlatformFilePicker.createSaveDialogHandle(
                        initialDirectory = initialDirectory ?: settings.initialDirectory,
                        baseName = baseName,
                        extension = extension,
                        title = settings.title ?: "Save file",
                        parentWindow = resolvedParentWindow,
                    )
                    try {
                        val file = PlatformFilePicker.showSaveDialog(handle, bytes)
                        currentOnResult(file?.let { KmpFile(it) })
                    } finally {
                        PlatformFilePicker.destroyDialog(handle)
                    }
                }
            }
        )
    }
}

@ExperimentalCalfApi
class FileSaverLauncher(
    private val onLaunch: (
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) -> Unit,
) {
    fun launch(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) {
        onLaunch(
            bytes,
            baseName,
            extension,
            initialDirectory,
        )
    }
}
