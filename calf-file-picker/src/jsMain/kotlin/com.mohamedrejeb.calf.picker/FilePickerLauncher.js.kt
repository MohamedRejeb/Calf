package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
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
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    var fileDialogVisible by rememberSaveable { mutableStateOf(false) }

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                if (type == FilePickerFileType.Folder)
                    return@FilePickerLauncher

                val fileInputElement = document.createElement("input") as HTMLInputElement

                with(fileInputElement) {
                    style.display = "none"
                    this.type = "file"
                    name = "file"

                    accept =
                        if (type is FilePickerFileType.Extension)
                            type.value.joinToString(", ") { ".$it" }
                        else
                            type.value.joinToString(", ")

                    multiple = selectionMode == FilePickerSelectionMode.Multiple

                    onchange = { event ->
                        try {
                            // Get the selected files
                            val files = event.target
                                ?.unsafeCast<HTMLInputElement>()
                                ?.files
                                ?.asList()
                                .orEmpty()

                            // Return the result
                            onResult(files.map { KmpFile(it) })
                            fileDialogVisible = false
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }

                    click()
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
