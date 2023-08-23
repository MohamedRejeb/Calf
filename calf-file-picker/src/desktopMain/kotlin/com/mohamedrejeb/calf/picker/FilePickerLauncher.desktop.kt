package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.AwtWindow
import com.mohamedrejeb.calf.io.KmpFile
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

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
                val fileDialog = object : FileDialog(
                    frame,
                    "Select File",
                    LOAD
                ) {
                    override fun setVisible(value: Boolean) {
                        super.setVisible(value)
                        if (value) {
                            onResult(files?.toList() ?: emptyList())
                            fileDialogVisible = false
                        }
                    }
                }

                fileDialog.isMultipleMode = selectionMode == FilePickerSelectionMode.Multiple

                val mimeType = when (type) {
                    FilePickerFileType.Image -> listOf("image")
                    FilePickerFileType.Video -> listOf("video")
                    FilePickerFileType.ImageVideo -> listOf("image", "video")
                    FilePickerFileType.All -> emptyList()
                }
                fileDialog.setFilenameFilter { file, name ->
                    if (mimeType.isEmpty()) {
                        return@setFilenameFilter true
                    }

                    val contentType = file.toURI().toURL().openConnection().contentType
                    println("contentType $contentType")
                    contentType.startsWith(mimeType.first(), true)
                }

                fileDialog
            },
            dispose = {
                it.dispose()
            }
        )
    }

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