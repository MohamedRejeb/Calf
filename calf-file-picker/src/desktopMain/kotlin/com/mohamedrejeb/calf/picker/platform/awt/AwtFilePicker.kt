package com.mohamedrejeb.calf.picker.platform.awt

import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import java.awt.Dialog
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Window
import java.io.File
import java.io.FilenameFilter
import java.net.URLConnection

internal class AwtFilePicker {
    fun launchFilePicker(
        initialDirectory: String?,
        type: FilePickerFileType,
        selectionMode: FilePickerSelectionMode,
        title: String?,
        parentWindow: Window?,
        onResult: (List<File>) -> Unit,
    ): FileDialog = callAwtFilePicker(
        title = title,
        initialDirectory = initialDirectory,
        type = type,
        selectionMode = selectionMode,
        parentWindow = parentWindow,
        onResult = onResult,
    )

    fun pickDirectory(
        initialDirectory: String?,
        title: String?,
        parentWindow: Window?,
        onResult: (File?) -> Unit,
    ): FileDialog = callAwtDirectoryPicker(
        title = title,
        initialDirectory = initialDirectory,
        parentWindow = parentWindow,
        onResult = onResult,
    )

    private fun callAwtFilePicker(
        title: String?,
        initialDirectory: String?,
        type: FilePickerFileType,
        selectionMode: FilePickerSelectionMode,
        parentWindow: Window?,
        onResult: (List<File>) -> Unit,
    ): FileDialog {
        fun handleResult(value: Boolean, files: Array<File>?) {
            if (value) {
                val result = files?.toList().orEmpty()
                onResult(result)
            }
        }

        // Handle parentWindow: Dialog, Frame, or null
        val dialog = when (parentWindow) {
            is Dialog -> object : FileDialog(parentWindow, title, LOAD) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }

            else -> object : FileDialog(parentWindow as? Frame, title, LOAD) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }
        }

        // Set multiple mode
        dialog.isMultipleMode = selectionMode == FilePickerSelectionMode.Multiple

        // Set mime types / extensions
        val mimeType =
            when (type) {
                is FilePickerFileType.All, is FilePickerFileType.Extension -> emptyList()
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

        dialog.filenameFilter = FilenameFilter { _, name ->
            when (type) {
                is FilePickerFileType.All ->
                    true

                is FilePickerFileType.Extension ->
                    type.extensions.any { name.endsWith(it) }

                else -> {
                    val contentType = URLConnection.guessContentTypeFromName(name) ?: ""
                    mimeType.any { contentType.startsWith(it, true) }
                }
            }
        }

        // Set initial directory
        dialog.directory = initialDirectory

        // Show the dialog
        dialog.isVisible = true

        return dialog
    }

    private fun callAwtDirectoryPicker(
        title: String?,
        initialDirectory: String?,
        parentWindow: Window?,
        onResult: (File?) -> Unit,
    ): FileDialog {
        fun handleResult(value: Boolean, files: Array<File>?) {
            if (value) {
                val result = files?.firstOrNull()
                onResult(result)
            }
        }

        // Handle parentWindow: Dialog, Frame, or null
        val dialog = when (parentWindow) {
            is Dialog -> object : FileDialog(parentWindow, title, SAVE) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }

            else -> object : FileDialog(parentWindow as? Frame, title, SAVE) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }
        }

        // Set multiple mode
        dialog.isMultipleMode = false

        // Set mime types
        dialog.filenameFilter = FilenameFilter { file, _ ->
            file.isDirectory
        }

        // Set initial directory
        dialog.directory = initialDirectory

        // Show the dialog
        dialog.isVisible = true

        return dialog
    }

    companion object {
        val current: AwtFilePicker by lazy { AwtFilePicker() }
    }
}
