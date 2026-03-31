package com.mohamedrejeb.calf.picker.platform

import androidx.compose.ui.awt.ComposeWindow
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

    /**
     * Create a native file picker dialog handle.
     * The handle stores all configuration (title, filters, selection mode)
     * so that [showFilePicker] only needs to display it.
     *
     * Must be paired with [destroyDialog] when no longer needed.
     */
    fun createFilePickerHandle(
        initialDirectory: String?,
        type: FilePickerFileType,
        selectionMode: FilePickerSelectionMode,
        title: String?,
        parentWindow: ComposeWindow? = null,
    ): Long {
        val extensions = resolveExtensions(type)

        return NativeFilePickerBridge.createFileDialog(
            title = title,
            initialDirectory = initialDirectory,
            extensions = extensions?.toTypedArray(),
            multiple = selectionMode == FilePickerSelectionMode.Multiple,
            parentWindow = parentWindow?.windowHandle ?: 0L,
        )
    }

    /**
     * Create a native directory picker dialog handle.
     * Must be paired with [destroyDialog] when no longer needed.
     */
    fun createDirectoryPickerHandle(
        initialDirectory: String?,
        title: String?,
        parentWindow: ComposeWindow? = null,
    ): Long {
        return NativeFilePickerBridge.createDirectoryDialog(
            title = title,
            initialDirectory = initialDirectory,
            parentWindow = parentWindow?.windowHandle ?: 0L,
        )
    }

    /**
     * Create a native save-file dialog handle.
     * Must be paired with [destroyDialog] when no longer needed.
     */
    fun createSaveDialogHandle(
        initialDirectory: String?,
        baseName: String,
        extension: String,
        title: String = "Save file",
        parentWindow: ComposeWindow? = null,
    ): Long {
        return NativeFilePickerBridge.createSaveDialog(
            title = title,
            initialDirectory = initialDirectory,
            defaultName = "$baseName.$extension",
            extension = extension,
            parentWindow = parentWindow?.windowHandle ?: 0L,
        )
    }

    /** Show a previously created file picker dialog. */
    suspend fun showFilePicker(handle: Long): List<File> {
        val paths = withContext(Dispatchers.IO) {
            NativeFilePickerBridge.showFileDialog(handle)
        }
        return paths.map { File(it) }
    }

    /** Show a previously created directory picker dialog. */
    suspend fun showDirectoryPicker(handle: Long): File? {
        val path = withContext(Dispatchers.IO) {
            NativeFilePickerBridge.showDirectoryDialog(handle)
        }
        return path?.let { File(it) }
    }

    /** Show a previously created save dialog and write bytes to the selected file. */
    suspend fun showSaveDialog(handle: Long, bytes: ByteArray?): File? {
        return withContext(Dispatchers.IO) {
            val path = NativeFilePickerBridge.showSaveDialog(handle) ?: return@withContext null

            val file = File(path)
            if (bytes != null) {
                file.writeBytes(bytes)
            } else {
                file.createNewFile()
            }
            file
        }
    }

    /** Free the native memory for a dialog handle. */
    fun destroyDialog(handle: Long) {
        NativeFilePickerBridge.destroyDialog(handle)
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
