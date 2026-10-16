package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController

@OptIn(BetaInteropApi::class, InternalCalfApi::class, ExperimentalForeignApi::class)
@ExperimentalCalfApi
@Composable
actual fun rememberFileSaverLauncher(
    settings: FilePickerSettings,
    onError: (FilePickerException) -> Unit,
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()
    val currentUIViewController = LocalUIViewController.current
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnError by rememberUpdatedState(onError)

    // Temp files created by onLaunch (bytes) and onLaunchData paths.
    // Cleaned up after the picker finishes (save or cancel).
    val pendingTempUrls = remember { mutableListOf<NSURL>() }

    val delegate = remember {
        DocumentPickerDelegate(
            onPicked = { urls ->
                cleanUpTempFiles(pendingTempUrls)
                scope.launch(Dispatchers.Main) {
                    currentOnResult(urls.firstOrNull()?.let { KmpFile(it) })
                }
            },
            onCancelled = {
                cleanUpTempFiles(pendingTempUrls)
                scope.launch(Dispatchers.Main) {
                    currentOnResult(null)
                }
            },
        )
    }

    val launchStaged: (String?, suspend () -> NSURL?) -> Unit = { initialDirectory, stage ->
        scope.launch {
            val stagedUrl = withContext(Dispatchers.IO) { stage() }
            if (stagedUrl == null) {
                currentOnError(FilePickerException("Could not prepare the file to save"))
                return@launch
            }
            pendingTempUrls.add(stagedUrl)
            withContext(Dispatchers.Main) {
                presentExportPicker(stagedUrl, initialDirectory, delegate, currentUIViewController)
            }
        }
    }

    return remember(currentUIViewController) {
        FileSaverLauncher(
            onLaunch = { bytes, baseName, extension, initialDirectory ->
                val data = if (bytes != null && bytes.isNotEmpty()) {
                    bytes.usePinned { pinned ->
                        NSData.create(
                            bytes = pinned.addressOf(0),
                            length = bytes.size.toULong(),
                        )
                    }
                } else {
                    NSData()
                }
                launchStaged(initialDirectory) { stageExportData(data, "$baseName.$extension") }
            },
            onLaunchFile = { file, baseName, extension, initialDirectory ->
                launchStaged(initialDirectory) { stageExportFile(file.url, "$baseName.$extension") }
            },
            onLaunchData = { data, baseName, extension, initialDirectory ->
                launchStaged(initialDirectory) { stageExportData(data, "$baseName.$extension") }
            },
        )
    }
}

private fun cleanUpTempFiles(tempUrls: MutableList<NSURL>) {
    tempUrls.forEach(::removeStaging)
    tempUrls.clear()
}

private fun presentExportPicker(
    sourceUrl: NSURL,
    initialDirectory: String?,
    delegate: DocumentPickerDelegate,
    viewController: UIViewController,
) {
    // asCopy makes the sheet offer "Save" rather than "Move"; the staged file is removed afterwards.
    val picker = UIDocumentPickerViewController(
        forExportingURLs = listOf(sourceUrl),
        asCopy = true,
    )
    picker.directoryURL = initialDirectoryUrl(initialDirectory)
    delegate.attachTo(picker)
    viewController.presentViewController(
        picker,
        animated = true,
        completion = null,
    )
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
    private val onLaunchData: (
        data: NSData,
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
     * Launches the platform save dialog using an iOS [NSURL] as the source.
     *
     * @param url The source file URL to save.
     * @param baseName The suggested file name without extension (e.g. "document").
     * @param extension The file extension without dot (e.g. "pdf").
     * @param initialDirectory Optional initial directory for the save dialog.
     */
    fun launch(
        url: NSURL,
        baseName: String,
        extension: String,
        initialDirectory: String? = null,
    ) {
        launch(KmpFile(url), baseName, extension, initialDirectory)
    }

    /**
     * Launches the platform save dialog using [NSData] as the source.
     *
     * The data is written to a temporary file and presented via the export picker.
     *
     * @param data The raw data to save.
     * @param baseName The suggested file name without extension (e.g. "document").
     * @param extension The file extension without dot (e.g. "pdf").
     * @param initialDirectory Optional initial directory for the save dialog.
     */
    fun launch(
        data: NSData,
        baseName: String,
        extension: String,
        initialDirectory: String? = null,
    ) {
        onLaunchData(data, baseName, extension, initialDirectory)
    }
}
