package com.mohamedrejeb.calf.picker

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * On Android the save sheet creates the document first and Calf writes the source into it.
 *
 * A [KmpFile] source survives process death while the sheet is open; a `ByteArray` source does
 * not, because it can be larger than the saved instance state allows. If the source is lost, or
 * cannot be written, the created document is deleted and `onResult` receives `null`.
 */
@ExperimentalCalfApi
@Composable
actual fun rememberFileSaverLauncher(
    settings: FilePickerSettings,
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    val contentResolver = LocalContext.current.applicationContext.contentResolver

    val pendingSource = rememberSaveable(saver = PendingSaveSource.Saver) { PendingSaveSource() }

    val launcher = rememberLauncherForActivityResult(
        contract = CreateDocumentForFileName(),
        onResult = { uri ->
            val source = pendingSource.take()
            if (uri == null) {
                currentOnResult(null)
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                val saved = contentResolver.writeSaveSource(source, uri)
                currentOnResult(if (saved) KmpFile(uri) else null)
            }
        },
    )

    return remember {
        FileSaverLauncher(
            onLaunch = { bytes, baseName, extension, _ ->
                pendingSource.set(SaveSource.Bytes(bytes ?: ByteArray(0)))
                launcher.launch("$baseName.$extension")
            },
            onLaunchFile = { file, baseName, extension, _ ->
                pendingSource.set(SaveSource.File(file))
                launcher.launch("$baseName.$extension")
            },
        )
    }
}

/**
 * Writes [source] into the document the save sheet created at [destination].
 *
 * @return `true` if the source was written; otherwise the empty document is deleted.
 */
private suspend fun ContentResolver.writeSaveSource(source: SaveSource?, destination: Uri): Boolean =
    withContext(Dispatchers.IO) {
        val written = try {
            source != null && write(source, destination)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
        if (!written) deleteCreatedDocument(destination)
        written
    }

private fun ContentResolver.write(source: SaveSource, destination: Uri): Boolean {
    val output = openOutputStream(destination, TRUNCATE_WRITE_MODE) ?: return false
    return output.use { outputStream ->
        when (source) {
            is SaveSource.Bytes -> {
                outputStream.write(source.bytes)
                true
            }
            is SaveSource.File -> {
                val input = openInputStream(source.file.uri) ?: return false
                input.use { it.copyTo(outputStream) }
                true
            }
        }
    }
}

private fun ContentResolver.deleteCreatedDocument(document: Uri) {
    try {
        DocumentsContract.deleteDocument(this, document)
    } catch (e: Exception) {
        // The provider may not support deletion; an empty document is all that is left behind.
    }
}

/** [ActivityResultContracts.CreateDocument] typed after the extension of the suggested file name. */
private class CreateDocumentForFileName : ActivityResultContracts.CreateDocument(FALLBACK_MIME_TYPE) {
    override fun createIntent(context: Context, input: String): Intent =
        super.createIntent(context, input).setType(mimeTypeForFileName(input))
}

private fun mimeTypeForFileName(fileName: String): String {
    val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: FALLBACK_MIME_TYPE
}

private const val FALLBACK_MIME_TYPE = "application/octet-stream"

/** Truncates a document that already has content, which some providers keep with plain "w". */
private const val TRUNCATE_WRITE_MODE = "wt"

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
     * Launches the platform save dialog using an Android [Uri] as the source.
     *
     * @param uri The source content URI to save.
     * @param baseName The suggested file name without extension (e.g. "document").
     * @param extension The file extension without dot (e.g. "pdf").
     * @param initialDirectory Optional initial directory for the save dialog.
     */
    fun launch(
        uri: Uri,
        baseName: String,
        extension: String,
        initialDirectory: String? = null,
    ) {
        launch(KmpFile(uri), baseName, extension, initialDirectory)
    }

    /**
     * Launches the platform save dialog using a [java.io.File] as the source.
     *
     * @param file The source file to save.
     * @param baseName The suggested file name without extension (e.g. "document").
     * @param extension The file extension without dot (e.g. "pdf").
     * @param initialDirectory Optional initial directory for the save dialog.
     */
    fun launch(
        file: File,
        baseName: String,
        extension: String,
        initialDirectory: String? = null,
    ) {
        launch(KmpFile(Uri.fromFile(file)), baseName, extension, initialDirectory)
    }
}
