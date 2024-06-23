package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.AwtWindow
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.platform.awt.AwtFilePicker
import com.mohamedrejeb.calf.picker.platform.util.Platform
import com.mohamedrejeb.calf.picker.platform.util.PlatformUtil
import com.mohamedrejeb.calf.picker.platform.windows.WindowsFilePicker
import jodd.net.MimeTypes
import java.io.File

@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    return when(PlatformUtil.current) {
        Platform.Windows ->
            rememberWindowsFilePickerLauncher(type, selectionMode, onResult)

        else ->
            rememberAwtFilePickerLauncher(type, selectionMode, onResult)
    }
}

@Composable
private fun rememberAwtFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    var fileDialogVisible by rememberSaveable { mutableStateOf(false) }

    if (fileDialogVisible) {
        AwtWindow(
            create = {
                if (type !is FilePickerFileType.Extension) {
                    type.value.forEach { mimetype ->
                        MimeTypes.findExtensionsByMimeTypes(mimetype, true)
                    }
                }

                if (type == FilePickerFileType.Folder)
                    AwtFilePicker.current.launchDirectoryPicker(
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
                            fileDialogVisible = false
                        }
                    )
                else
                    AwtFilePicker.current.launchFilePicker(
                        initialDirectory = null,
                        type = type,
                        selectionMode = selectionMode,
                        title = "Select a file",
                        parentWindow = null,
                        onResult = { files ->
                            onResult(files.map { KmpFile(it) })
                            fileDialogVisible = false
                        }
                    )
            },
            dispose = {
                it.dispose()
            },
        )
    }

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                fileDialogVisible = true
            },
        )
    }
}

@Composable
private fun rememberWindowsFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                if (type == FilePickerFileType.Folder)
                    WindowsFilePicker.current.launchDirectoryPicker(
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
                    WindowsFilePicker.current.launchFilePicker(
                        initialDirectory = null,
                        type = type,
                        selectionMode = selectionMode,
                        title = "Select a file",
                        parentWindow = null,
                        onResult = { files ->
                            onResult(files.map { KmpFile(it) })
                        }
                    )
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

fun main() {
    MimeTypes.findExtensionsByMimeTypes("video/*", true).also {
        println(it)
    }
}