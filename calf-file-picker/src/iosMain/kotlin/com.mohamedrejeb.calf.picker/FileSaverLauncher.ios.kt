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
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.URLByDeletingLastPathComponent
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(BetaInteropApi::class, InternalCalfApi::class, ExperimentalForeignApi::class)
@ExperimentalCalfApi
@Composable
actual fun rememberFileSaverLauncher(
    settings: FilePickerSettings,
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()
    val currentUIViewController = LocalUIViewController.current
    val currentOnResult by rememberUpdatedState(onResult)

    // Temp files created by onLaunch (bytes) and onLaunchData paths.
    // Cleaned up after the picker finishes (save or cancel).
    val pendingTempUrls = remember { mutableListOf<NSURL>() }

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL,
            ) {
                cleanUpTempFiles(pendingTempUrls)
                scope.launch(Dispatchers.Main) {
                    currentOnResult(KmpFile(didPickDocumentAtURL))
                }
            }

            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>,
            ) {
                cleanUpTempFiles(pendingTempUrls)
                scope.launch(Dispatchers.Main) {
                    val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                    currentOnResult(url?.let { KmpFile(it) })
                }
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                cleanUpTempFiles(pendingTempUrls)
                scope.launch(Dispatchers.Main) {
                    currentOnResult(null)
                }
            }
        }
    }

    val launchStaged: (suspend () -> NSURL) -> Unit = { stage ->
        scope.launch {
            val stagedUrl = withContext(Dispatchers.IO) { stage() }
            pendingTempUrls.add(stagedUrl)
            withContext(Dispatchers.Main) {
                presentExportPicker(stagedUrl, delegate, currentUIViewController)
            }
        }
    }

    return remember(currentUIViewController) {
        FileSaverLauncher(
            onLaunch = { bytes, baseName, extension, _ ->
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
                launchStaged { stageExportData(data, "$baseName.$extension") }
            },
            onLaunchFile = { file, baseName, extension, _ ->
                launchStaged { stageExportFile(file.url, "$baseName.$extension") }
            },
            onLaunchData = { data, baseName, extension, _ ->
                launchStaged { stageExportData(data, "$baseName.$extension") }
            },
        )
    }
}

/**
 * The export picker shows, and saves under, the staged file's own name, so each export is staged in
 * a directory of its own and the file keeps the name the caller asked for.
 */
@OptIn(ExperimentalForeignApi::class)
private fun exportStagingUrl(fileName: String): NSURL {
    val directory = "${NSTemporaryDirectory().removeSuffix("/")}/$EXPORT_STAGING_DIR/${NSUUID().UUIDString}"
    NSFileManager.defaultManager.createDirectoryAtPath(
        directory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    return NSURL.fileURLWithPath("$directory/$fileName")
}

/** Writes [data] to a fresh staging directory as [fileName]; see [exportStagingUrl]. */
internal fun stageExportData(data: NSData, fileName: String): NSURL {
    val url = exportStagingUrl(fileName)
    data.writeToURL(url, atomically = true)
    return url
}

/** Copies [source] into a fresh staging directory as [fileName]; the source is left in place. */
@OptIn(ExperimentalForeignApi::class)
internal fun stageExportFile(source: NSURL, fileName: String): NSURL {
    val url = exportStagingUrl(fileName)
    NSFileManager.defaultManager.copyItemAtURL(source, toURL = url, error = null)
    return url
}

/** Removes a staged export together with its private directory. */
@OptIn(ExperimentalForeignApi::class)
internal fun removeExportStaging(stagedUrl: NSURL) {
    val directory = stagedUrl.URLByDeletingLastPathComponent ?: return
    NSFileManager.defaultManager.removeItemAtURL(directory, error = null)
}

private fun cleanUpTempFiles(tempUrls: MutableList<NSURL>) {
    tempUrls.forEach(::removeExportStaging)
    tempUrls.clear()
}

private const val EXPORT_STAGING_DIR = "calf-file-saver"

private fun presentExportPicker(
    sourceUrl: NSURL,
    delegate: UIDocumentPickerDelegateProtocol,
    viewController: UIViewController,
) {
    val picker = UIDocumentPickerViewController(
        forExportingURLs = listOf(sourceUrl),
    )
    picker.delegate = delegate
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
