package com.mohamedrejeb.calf.picker

import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalCalfApi
@Composable
actual fun rememberFileSaverLauncher(
    settings: FilePickerSettings,
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    val context = LocalContext.current.applicationContext

    // Holds the source to write after the user picks a location
    var pendingSource = remember<Any?> { null } // ByteArray? or KmpFile

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
        onResult = { uri ->
            if (uri == null) {
                currentOnResult(null)
                return@rememberLauncherForActivityResult
            }

            val source = pendingSource
            scope.launch {
                withContext(Dispatchers.IO) {
                    when (source) {
                        is ByteArray -> {
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                outputStream.write(source)
                            }
                        }
                        is KmpFile -> {
                            context.contentResolver.openInputStream(source.uri)?.use { inputStream ->
                                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                        }
                    }
                }
                currentOnResult(KmpFile(uri))
            }
        },
    )

    return remember {
        FileSaverLauncher(
            onLaunch = { bytes, baseName, extension, _ ->
                pendingSource = bytes
                launcher.launch("$baseName.$extension")
            },
            onLaunchFile = { file, baseName, extension, _ ->
                pendingSource = file
                launcher.launch("$baseName.$extension")
            },
        )
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
