package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.browser.document
import org.w3c.files.File

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
                val fileInputElement = document.createElement("input")
                fileInputElement.setAttribute("style", "display='none'")
                fileInputElement.setAttribute("type", "file")
                fileInputElement.setAttribute("name", "file")

                fileInputElement.setAttribute("accept", type.value.joinToString(", "))

                if (selectionMode == FilePickerSelectionMode.Multiple)
                    fileInputElement.setAttribute("multiple", "true")
                else
                    fileInputElement.removeAttribute("multiple")

                fileInputElement.addEventListener("change", {
                    val files: Array<File> = fileInputElement.asDynamic().files
                    onResult(files.map { KmpFile(it) })
                    fileDialogVisible = false
                })

                js("fileInputElement.click();")

                Unit
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
