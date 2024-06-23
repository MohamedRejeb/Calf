package com.mohamedrejeb.calf.picker.platform.windows

import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.platform.PlatformFilePicker
import com.mohamedrejeb.calf.picker.platform.windows.api.JnaFileChooser
import jodd.net.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Window
import java.io.File

internal class WindowsFilePicker: PlatformFilePicker {
    override suspend fun launchFilePicker(
        initialDirectory: String?,
        type: FilePickerFileType,
        selectionMode: FilePickerSelectionMode,
        title: String?,
        parentWindow: Window?,
        onResult: (List<File>) -> Unit,
    ) = withContext(Dispatchers.Default) {
        val fileChooser = JnaFileChooser()

        // Setup file chooser
        fileChooser.apply {
            // Set mode
            mode =
                if (type == FilePickerFileType.Folder)
                    JnaFileChooser.Mode.Directories
                else
                    JnaFileChooser.Mode.Files

            // Set selection mode
            isMultiSelectionEnabled = selectionMode == FilePickerSelectionMode.Multiple

            // Set initial directory, title and file extensions
            val fileExtensions =
                if (type is FilePickerFileType.Extension)
                    type.extensions
                else
                    type.value
                        .map {
                            MimeTypes.findExtensionsByMimeTypes(it, it.contains('*'))
                        }
                        .flatten()
                        .distinct()
            setup(initialDirectory, fileExtensions, title)
        }

        // Show file chooser
        fileChooser.showOpenDialog(parentWindow)

        // Return selected files
		val result =
			if (selectionMode == FilePickerSelectionMode.Single)
				listOfNotNull(fileChooser.selectedFile)
			else
				fileChooser.selectedFiles.mapNotNull { it }
        onResult(result)
    }

    override suspend fun launchDirectoryPicker(
        initialDirectory: String?,
        title: String?,
        parentWindow: Window?,
        onResult: (File?) -> Unit,
    ) = withContext(Dispatchers.Default) {
        val fileChooser = JnaFileChooser()

        // Setup file chooser
        fileChooser.apply {
            // Set mode
            mode = JnaFileChooser.Mode.Directories

            // Only allow single selection
            isMultiSelectionEnabled = false

            // Set initial directory and title
            setup(initialDirectory, emptyList(), title)
        }

        // Show file chooser
        fileChooser.showOpenDialog(parentWindow)

        // Return selected directory
        onResult(fileChooser.selectedFile)
    }

    private fun JnaFileChooser.setup(
        initialDirectory: String?,
        fileExtensions: List<String>,
        title: String?
    ) {
        // Set title
        title?.let(::setTitle)

        // Set initial directory
        initialDirectory?.let(::setCurrentDirectory)

        // Set file extension
        if (fileExtensions.isNotEmpty()) {
            val filterName = fileExtensions.joinToString(", ", "Supported Files (", ")")
            addFilter(filterName, *fileExtensions.toTypedArray())
        }
    }
}
