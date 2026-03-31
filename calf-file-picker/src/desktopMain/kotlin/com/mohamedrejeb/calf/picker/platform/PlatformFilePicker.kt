package com.mohamedrejeb.calf.picker.platform

import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import jodd.net.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal object PlatformFilePicker {

    fun warmup() {
        NativeFilePickerBridge.init()
    }

    suspend fun launchFilePicker(
        initialDirectory: String?,
        type: FilePickerFileType,
        selectionMode: FilePickerSelectionMode,
        title: String?,
    ): List<File> {
        val extensions = resolveExtensions(type)

        val paths = withContext(Dispatchers.IO) {
            NativeFilePickerBridge.pickFiles(
                title = title,
                initialDirectory = initialDirectory,
                extensions = extensions?.toTypedArray(),
                multiple = selectionMode == FilePickerSelectionMode.Multiple,
            )
        }

        return paths.map { File(it) }
    }

    suspend fun launchDirectoryPicker(
        initialDirectory: String?,
        title: String?,
    ): File? {
        val path = withContext(Dispatchers.IO) {
            NativeFilePickerBridge.pickDirectory(
                title = title,
                initialDirectory = initialDirectory,
            )
        }

        return path?.let { File(it) }
    }

    suspend fun saveFile(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ): File? {
        val path = withContext(Dispatchers.IO) {
            NativeFilePickerBridge.saveFileDialog(
                title = "Save file",
                initialDirectory = initialDirectory,
                defaultName = "$baseName.$extension",
                extension = extension,
            )
        }

        return path?.let { pathStr ->
            val file = File(pathStr)
            if (bytes != null) {
                file.writeBytes(bytes)
            } else {
                file.createNewFile()
            }
            file
        }
    }
}

/**
 * Resolves [FilePickerFileType] to a list of file extensions for the native dialog filter.
 * Returns null when no filter should be applied (All, Folder).
 */
private fun resolveExtensions(type: FilePickerFileType): List<String>? {
    return when (type) {
        is FilePickerFileType.All,
        is FilePickerFileType.Folder -> null

        is FilePickerFileType.Extension -> type.extensions

        else -> {
            type.value
                .flatMap { mimeType ->
                    MimeTypes.findExtensionsByMimeTypes(mimeType, mimeType.contains('*'))
                }
                .distinct()
                .ifEmpty { null }
        }
    }
}
