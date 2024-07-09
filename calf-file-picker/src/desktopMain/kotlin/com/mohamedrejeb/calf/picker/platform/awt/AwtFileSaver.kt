package com.mohamedrejeb.calf.picker.platform.awt

import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Dialog
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Window
import java.io.File
import kotlin.coroutines.resume

internal object AwtFileSaver {
    suspend fun saveFile(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
        parentWindow: Window?,
    ): KmpFile? = suspendCancellableCoroutine { continuation ->
        fun handleResult(value: Boolean, files: Array<File>?) {
            if (value) {
                val file = files?.firstOrNull()?.let { file ->
                    // Write bytes to file, or create a new file
                    bytes?.let { file.writeBytes(bytes) } ?: file.createNewFile()
                    KmpFile(file)
                }
                continuation.resume(file)
            }
        }

        // Handle parentWindow: Dialog, Frame, or null
        val dialog = when (parentWindow) {
            is Dialog -> object : FileDialog(parentWindow, "Save dialog", SAVE) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }

            else -> object : FileDialog(parentWindow as? Frame, "Save dialog", SAVE) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }
        }

        // Set initial directory
        dialog.directory = initialDirectory

        // Set file name
        dialog.file = "$baseName.$extension"

        // Show the dialog
        dialog.isVisible = true

        // Dispose the dialog when the continuation is cancelled
        continuation.invokeOnCancellation { dialog.dispose() }
    }
}