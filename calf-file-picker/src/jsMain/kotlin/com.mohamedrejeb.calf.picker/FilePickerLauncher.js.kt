package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import org.w3c.files.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    settings: FilePickerSettings,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    var fileDialogVisible by rememberSaveable { mutableStateOf(false) }

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                if (type == FilePickerFileType.Folder) {
                    // Folder picking is not supported on Web
                    onResult(emptyList())
                    return@FilePickerLauncher
                }

                val fileInputElement = document.createElement("input") as HTMLInputElement

                fun cleanup() {
                    fileInputElement.remove()
                }

                with(fileInputElement) {
                    style.display = "none"
                    this.type = "file"
                    name = "file"

                    accept =
                        if (type is FilePickerFileType.Extension)
                            type.value.joinToString(", ") { ".$it" }
                        else
                            type.value.joinToString(", ")

                    multiple = selectionMode is FilePickerSelectionMode.Multiple

                    onchange = { event ->
                        try {
                            // Get the selected files
                            val files = event.target
                                ?.unsafeCast<HTMLInputElement>()
                                ?.files
                                ?.asList()
                                .orEmpty()

                            // Apply maxItems limit if set
                            val maxItems = (selectionMode as? FilePickerSelectionMode.Multiple)?.maxItems
                            val limitedFiles = if (maxItems != null) files.take(maxItems) else files

                            // Return the result
                            onResult(limitedFiles.map { KmpFile(it) })
                            fileDialogVisible = false
                        } catch (_: Throwable) {
                            onResult(emptyList())
                        } finally {
                            cleanup()
                        }
                    }

                    oncancel = {
                        onResult(emptyList())
                        cleanup()
                    }

                    // Append to DOM so events fire reliably, then trigger
                    document.body?.appendChild(this)
                    click()
                }
            },
        )
    }
}

@Stable
actual class FilePickerLauncher actual constructor(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    private val onLaunch: () -> Unit,
) {
    actual fun launch() {
        onLaunch()
    }
}
