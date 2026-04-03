package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
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

@ExperimentalCalfApi
@Composable
actual fun rememberFileSaverLauncher(
    settings: FilePickerSettings,
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    return remember {
        FileSaverLauncher(
            onLaunch = { bytes, baseName, extension, _ ->
                val fileName = "$baseName.$extension"
                val mimeType = extensionToMimeType(extension)
                val content = bytes ?: byteArrayOf()

                val blob = createBlob(content, mimeType)
                triggerDownload(blob, fileName)
            },
            onLaunchFile = { file, baseName, extension, _ ->
                val fileName = "$baseName.$extension"
                triggerDownload(file.file, fileName)
            },
            onLaunchBlob = { blob, baseName, extension, _ ->
                val fileName = "$baseName.$extension"
                triggerDownload(blob, fileName)
            },
        )
    }
}

/**
 * Triggers a browser download for the given [Blob] with the specified file name.
 * Downloads are fire-and-forget — the browser handles the save dialog.
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
     * Launches a browser download using a W3C [File] as the source.
     *
     * @param file The source [org.w3c.files.File] to download.
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
     * Launches a browser download using a [Blob] as the source.
     *
     * @param blob The source [Blob] to download.
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
