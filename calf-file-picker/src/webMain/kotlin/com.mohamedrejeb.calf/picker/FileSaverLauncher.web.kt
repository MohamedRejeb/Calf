package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.browser.document
import org.w3c.files.File
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob

/**
 * Platform-specific: creates a [Blob] from raw bytes with the given MIME type.
 */
internal expect fun createBlob(bytes: ByteArray, mimeType: String): Blob

/**
 * Whether the browser has the File System Access save dialog (`window.showSaveFilePicker`).
 * Chromium browsers have it; Safari and Firefox do not.
 */
internal expect fun isSaveFilePickerSupported(): Boolean

/**
 * Shows the browser's save dialog for [blob] and writes it to the file the user chooses. Exactly
 * one callback runs: [onPickerFailed] when the browser refuses to show the dialog, for example
 * without a user gesture, and [onWriteFailed] when the chosen file cannot be written.
 */
internal expect fun showSaveFilePicker(
    blob: Blob,
    suggestedName: String,
    mimeType: String,
    extension: String,
    onSaved: (File) -> Unit,
    onCancelled: () -> Unit,
    onPickerFailed: () -> Unit,
    onWriteFailed: () -> Unit,
)

/**
 * Saves through the browser's save dialog where there is one, so the user picks the location and
 * `onResult` receives the saved file, or `null` when they cancel. Elsewhere the file is downloaded:
 * the browser decides where it goes and `onResult` is not called.
 */
@ExperimentalCalfApi
@Composable
actual fun rememberFileSaverLauncher(
    settings: FilePickerSettings,
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    val currentOnResult by rememberUpdatedState(onResult)

    val save: (Blob, String, String) -> Unit = { blob, baseName, extension ->
        val fileName = "$baseName.$extension"
        if (isSaveFilePickerSupported()) {
            showSaveFilePicker(
                blob = blob,
                suggestedName = fileName,
                mimeType = extensionToMimeType(extension),
                extension = extension,
                onSaved = { file -> currentOnResult(KmpFile(file)) },
                onCancelled = { currentOnResult(null) },
                onPickerFailed = { triggerDownload(blob, fileName) },
                onWriteFailed = { currentOnResult(null) },
            )
        } else {
            triggerDownload(blob, fileName)
        }
    }

    return remember {
        FileSaverLauncher(
            onLaunch = { bytes, baseName, extension, _ ->
                save(createBlob(bytes ?: byteArrayOf(), extensionToMimeType(extension)), baseName, extension)
            },
            onLaunchFile = { file, baseName, extension, _ ->
                save(file.file, baseName, extension)
            },
            onLaunchBlob = { blob, baseName, extension, _ ->
                save(blob, baseName, extension)
            },
        )
    }
}

/**
 * Triggers a browser download for the given [Blob] with the specified file name. The browser
 * decides whether to ask where to save it, and no result is reported.
 */
private fun triggerDownload(blob: Blob, fileName: String) {
    val url = URL.createObjectURL(blob)
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = url
    anchor.download = fileName
    anchor.style.display = "none"
    document.body?.appendChild(anchor)
    anchor.click()
    anchor.remove()
    URL.revokeObjectURL(url)
}

internal fun extensionToMimeType(extension: String): String {
    return when (extension.lowercase()) {
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
        "csv" -> "text/csv"
        "json" -> "application/json"
        "xml" -> "application/xml"
        "html", "htm" -> "text/html"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "svg" -> "image/svg+xml"
        "webp" -> "image/webp"
        "mp3" -> "audio/mpeg"
        "mp4" -> "video/mp4"
        "zip" -> "application/zip"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        else -> "application/octet-stream"
    }
}

@ExperimentalCalfApi
@Stable
actual class FileSaverLauncher(
    private val onLaunch: (
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) -> Unit,
    private val onLaunchFile: (
        file: KmpFile,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) -> Unit,
    private val onLaunchBlob: (
        blob: Blob,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) -> Unit,
) {
    actual fun launch(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) {
        onLaunch(bytes, baseName, extension, initialDirectory)
    }

    actual fun launch(
        file: KmpFile,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) {
        onLaunchFile(file, baseName, extension, initialDirectory)
    }

    /**
     * Saves a W3C [File]; see [rememberFileSaverLauncher] for when the browser shows a save dialog.
     *
     * @param file The source [org.w3c.files.File] to save.
     * @param baseName The suggested file name without extension (e.g. "document").
     * @param extension The file extension without dot (e.g. "pdf").
     * @param initialDirectory Ignored on web.
     */
    fun launch(
        file: File,
        baseName: String,
        extension: String,
        initialDirectory: String? = null,
    ) {
        launch(KmpFile(file), baseName, extension, initialDirectory)
    }

    /**
     * Saves a [Blob]; see [rememberFileSaverLauncher] for when the browser shows a save dialog.
     *
     * @param blob The source [Blob] to save.
     * @param baseName The suggested file name without extension (e.g. "document").
     * @param extension The file extension without dot (e.g. "pdf").
     * @param initialDirectory Ignored on web.
     */
    fun launch(
        blob: Blob,
        baseName: String,
        extension: String,
        initialDirectory: String? = null,
    ) {
        onLaunchBlob(blob, baseName, extension, initialDirectory)
    }
}
