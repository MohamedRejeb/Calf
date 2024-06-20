package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.AwtWindow
import com.mohamedrejeb.calf.io.KmpFile
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.net.URLConnection

@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    var fileDialogVisible by rememberSaveable { mutableStateOf(false) }

    if (fileDialogVisible) {
        AwtWindow(
            create = {
                val frame: Frame? = null
                val fileDialog =
                    object : FileDialog(
                        frame,
                        "Select ${if (type == FilePickerFileType.Folder) "Folder" else "File"}",
                        if (type == FilePickerFileType.Folder) SAVE else LOAD,
                    ) {
                        override fun setVisible(value: Boolean) {
                            super.setVisible(value)
                            if (value) {
                                onResult(files.orEmpty().map { KmpFile(it) })
                                fileDialogVisible = false
                            }
                        }
                    }

                fileDialog.isMultipleMode = selectionMode == FilePickerSelectionMode.Multiple

                val mimeType =
                    when (type) {
                        FilePickerFileType.Folder -> listOf("folder")
                        FilePickerFileType.All -> emptyList()
                        else ->
                            type.value
                                .map {
                                    it
                                        .removeSuffix("/*")
                                        .removeSuffix("/")
                                        .removeSuffix("*")
                                }
                                .filter {
                                    it.isNotEmpty()
                                }
                    }
                fileDialog.setFilenameFilter { file, name ->
                    if (mimeType.isEmpty()) {
                        true
                    } else if (mimeType.first().contains("folder", true)) {
                        file.isDirectory
                    } else {
                        val contentType = URLConnection.guessContentTypeFromName(name) ?: ""
                        mimeType.any {
                            contentType.startsWith(it, true)
                        }
                    }
                }

                fileDialog
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
