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
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
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

    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL,
            ) {
                scope.launch(Dispatchers.Main) {
                    currentOnResult(KmpFile(didPickDocumentAtURL))
                }
            }

            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>,
            ) {
                scope.launch(Dispatchers.Main) {
                    val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                    currentOnResult(url?.let { KmpFile(it) })
                }
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                scope.launch(Dispatchers.Main) {
                    currentOnResult(null)
                }
            }
        }
    }

    return remember(currentUIViewController) {
        FileSaverLauncher(
            onLaunch = { bytes, baseName, extension, _ ->
                scope.launch {
                    // Write bytes to a temp file
                    val tempUrl = withContext(Dispatchers.IO) {
                        val fileName = "$baseName.$extension"
                        val tempPath = "${NSTemporaryDirectory().removeSuffix("/")}/${NSUUID().UUIDString}-$fileName"
                        val tempUrl = NSURL.fileURLWithPath(tempPath)

                        if (bytes != null && bytes.isNotEmpty()) {
                            val data = bytes.usePinned { pinned ->
                                NSData.create(
                                    bytes = pinned.addressOf(0),
                                    length = bytes.size.toULong(),
                                )
                            }
                            data.writeToURL(tempUrl, atomically = true)
                        } else {
                            // Create empty file
                            NSData().writeToURL(tempUrl, atomically = true)
                        }

                        tempUrl
                    }

                    withContext(Dispatchers.Main) {
                        val picker = UIDocumentPickerViewController(
                            forExportingURLs = listOf(tempUrl),
                        )
                        picker.delegate = delegate

                        currentUIViewController.presentViewController(
                            picker,
                            animated = true,
                            completion = null,
                        )
                    }
                }
            }
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
) {
    actual fun launch(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) {
        onLaunch(bytes, baseName, extension, initialDirectory)
    }
}
