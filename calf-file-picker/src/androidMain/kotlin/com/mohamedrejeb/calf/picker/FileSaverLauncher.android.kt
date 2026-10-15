package com.mohamedrejeb.calf.picker

import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileNotFoundException
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
 * cannot be written, the created document is deleted and `onError` is called.
 */
@ExperimentalCalfApi
@Composable
actual fun rememberFileSaverLauncher(
    settings: FilePickerSettings,
    onError: (FilePickerException) -> Unit,
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnError by rememberUpdatedState(onError)
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
                val failure = contentResolver.writeSaveSource(source, uri)
                if (failure == null) currentOnResult(KmpFile(uri)) else currentOnError(failure)
            }
        },
    )

    val launchSave: (SaveSource, String) -> Unit = { source, fileName ->
        pendingSource.set(source)
        try {
            launcher.launch(fileName)
        } catch (e: ActivityNotFoundException) {
            // No app handles ACTION_CREATE_DOCUMENT, e.g. the documents app is disabled.
            pendingSource.take()
            currentOnError(FilePickerException("No app on this device can save files", e))
        }
    }

    return remember {
        FileSaverLauncher(
            onLaunch = { bytes, baseName, extension, _ ->
                launchSave(SaveSource.Bytes(bytes ?: ByteArray(0)), "$baseName.$extension")
            },
            onLaunchFile = { file, baseName, extension, _ ->
                launchSave(SaveSource.File(file), "$baseName.$extension")
            },
        )
    }
}

/**
 * Writes [source] into the document the save sheet created at [destination].
 *
 * @return `null` if the source was written; otherwise why not, and the empty document is deleted.
 */
private suspend fun ContentResolver.writeSaveSource(source: SaveSource?, destination: Uri): FilePickerException? =
    withContext(Dispatchers.IO) {
        val failure = if (source == null) {
            FilePickerException("The content to save was lost, likely because the app was restarted while the save dialog was open")
        } else {
            try {
                write(source, destination)
                null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                FilePickerException("Could not write the saved file", e)
            }
        }
        if (failure != null) deleteCreatedDocument(destination)
        failure
    }

private fun ContentResolver.write(source: SaveSource, destination: Uri) {
    val output = openOutputStream(destination, TRUNCATE_WRITE_MODE)
        ?: throw FileNotFoundException("The provider gave no output stream for $destination")
    output.use { outputStream ->
        when (source) {
            is SaveSource.Bytes -> outputStream.write(source.bytes)
            is SaveSource.File -> {
                val input = openInputStream(source.file.uri)
                    ?: throw FileNotFoundException("The provider gave no input stream for ${source.file.uri}")
                input.use { it.copyTo(outputStream) }
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
