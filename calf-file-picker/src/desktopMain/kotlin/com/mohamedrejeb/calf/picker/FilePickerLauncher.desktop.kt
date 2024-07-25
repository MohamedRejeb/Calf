package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.platform.PlatformFilePicker
import jodd.net.MimeTypes
import kotlinx.coroutines.launch
import java.io.File

@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val scope = rememberCoroutineScope()

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                scope.launch {
                    if (type == FilePickerFileType.Folder)
                        PlatformFilePicker.current.launchDirectoryPicker(
                            initialDirectory = null,
                            title = "Select a folder",
                            parentWindow = null,
                            onResult = { file ->
                                onResult(
                                    if (file == null)
                                        emptyList()
                                    else
                                        listOf(KmpFile(file))
                                )
                            }
                        )
                    else
                        PlatformFilePicker.current.launchFilePicker(
                            initialDirectory = null,
                            type = type,
                            selectionMode = selectionMode,
                            title = "Select a file",
                            parentWindow = null,
                            onResult = { files ->
                                onResult(files.map { KmpFile(it) })
                            }
                        )
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

val File.extension: String
    get() = name.substringAfterLast(".")
