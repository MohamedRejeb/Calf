package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSItemProvider
import platform.Foundation.NSURL
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCurrent
import platform.PhotosUI.PHPickerConfigurationSelectionOrdered
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIPresentationController
import platform.UIKit.UIViewController
import platform.UIKit.presentationController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeApplication
import platform.UniformTypeIdentifiers.UTTypeAudio
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeText
import platform.UniformTypeIdentifiers.UTTypeVideo
import platform.darwin.NSObject
import kotlin.concurrent.AtomicInt
import kotlin.coroutines.resume
import platform.UniformTypeIdentifiers.UTTypeMovie

@OptIn(BetaInteropApi::class)
@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    settings: FilePickerSettings,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher =
    if (type == FilePickerFileType.Image || type == FilePickerFileType.Video || type == FilePickerFileType.ImageVideo) {
        rememberImageVideoPickerLauncher(type, selectionMode, onResult)
    } else {
        rememberDocumentPickerLauncher(type, selectionMode, onResult)
    }

@OptIn(InternalCalfApi::class, BetaInteropApi::class)
@Composable
private fun rememberDocumentPickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val scope = rememberCoroutineScope()
    val currentUIViewController = LocalUIViewController.current
    val currentOnResult by rememberUpdatedState(onResult)
    val currentType by rememberUpdatedState(type)
    val currentSelectionMode by rememberUpdatedState(selectionMode)

    val delegate =
        remember {
            object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentAtURL: NSURL,
                ) {
                    scope.launch(Dispatchers.Main) {
                        val result =
                            if (currentType == FilePickerFileType.Folder)
                                listOf(KmpFile(didPickDocumentAtURL))
                            else
                                withContext(Dispatchers.IO) {
                                    listOfNotNull(
                                        didPickDocumentAtURL.createTempFile()?.let { tempUrl ->
                                            KmpFile(
                                                url = didPickDocumentAtURL,
                                                tempUrl = tempUrl,
                                            )
                                        }
                                    )
                                }

                        currentOnResult(result)
                    }
                }

                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAtURLs: List<*>,
                ) {
                    scope.launch(Dispatchers.Main) {
                        val maxItems =
                            (currentSelectionMode as? FilePickerSelectionMode.Multiple)?.maxItems
                        val dataList =
                            didPickDocumentsAtURLs.mapNotNull {
                                val nsUrl = it as? NSURL ?: return@mapNotNull null
                                if (currentType == FilePickerFileType.Folder)
                                    KmpFile(nsUrl)
                                else
                                    withContext(Dispatchers.IO) {
                                        nsUrl.createTempFile()?.let { tempUrl ->
                                            KmpFile(
                                                url = nsUrl,
                                                tempUrl = tempUrl
                                            )
                                        }
                                    }
                            }.let { if (maxItems != null) it.take(maxItems) else it }

                        currentOnResult(dataList)
                    }
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    scope.launch(Dispatchers.Main) {
                        currentOnResult(emptyList())
                    }
                }
            }
        }

    return remember(currentUIViewController) {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                val pickerController =
                    createUIDocumentPickerViewController(
                        delegate = delegate,
                        type = currentType,
                        selectionMode = currentSelectionMode,
                    )

                currentUIViewController.presentViewController(
                    pickerController,
                    true,
                    null,
                )
            },
        )
    }
}

@OptIn(BetaInteropApi::class)
@Composable
private fun rememberImageVideoPickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val scope = rememberCoroutineScope()
    val currentUIViewController = LocalUIViewController.current
    val currentOnResult by rememberUpdatedState(onResult)
    val currentType by rememberUpdatedState(type)
    val currentSelectionMode by rememberUpdatedState(selectionMode)

    // Guard against double-callback, PHPicker may fire didFinishPicking more than once
    val hasFinished = remember { AtomicInt(0) }

    val pickerDelegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(
                picker: PHPickerViewController,
                didFinishPicking: List<*>,
            ) {
                // Prevent processing results twice
                val processResult =  hasFinished.compareAndSet(0, 1)

                if (didFinishPicking.isNotEmpty()) {
                    scope.launch {
                        val results = didFinishPicking
                            .mapNotNull {
                                val result = it as? PHPickerResult ?: return@mapNotNull null

                                async {
                                    result.itemProvider.loadFileRepresentationForTypeIdentifierSuspend(
                                        currentType
                                    )
                                }
                            }
                            .awaitAll()
                            .filterNotNull()

                        withContext(Dispatchers.Main) {
                            currentOnResult(results)
                        }
                    }
                } else if (processResult) {
                    scope.launch(Dispatchers.Main) {
                        currentOnResult(emptyList())
                    }
                }

                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }

    // Catches swipe-to-dismiss as cancellation (iOS 13+ interactive dismiss)
    val dismissDelegate = remember {
        object : NSObject(), UIAdaptivePresentationControllerDelegateProtocol {
            override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
                if (hasFinished.compareAndSet(0, 1).not()) return
                scope.launch(Dispatchers.Main) {
                    currentOnResult(emptyList())
                }
            }
        }
    }

    return remember(currentUIViewController) {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                // Reset guard for new launch
                hasFinished.compareAndSet(1, 0)

                val imagePicker =
                    createPHPickerViewController(
                        delegate = pickerDelegate,
                        type = currentType,
                        selectionMode = currentSelectionMode,
                    )

                // Attach dismiss delegate to detect swipe-to-dismiss
                imagePicker.presentationController?.delegate = dismissDelegate

                currentUIViewController.presentViewController(
                    imagePicker,
                    true,
                    null,
                )
            },
        )
    }
}

@OptIn(InternalCalfApi::class)
private suspend fun NSItemProvider.loadFileRepresentationForTypeIdentifierSuspend(
    type: FilePickerFileType
): KmpFile? = withContext(Dispatchers.IO) {
    suspendCancellableCoroutine { continuation ->
        val identifier = when (type) {
            FilePickerFileType.Image -> UTTypeImage.identifier
            FilePickerFileType.Video -> UTTypeMovie.identifier
            else -> registeredTypeIdentifiers.firstOrNull() as? String ?: UTTypeImage.identifier
        }

        val progress = loadFileRepresentationForTypeIdentifier(
            typeIdentifier = identifier
        ) { url, error ->
            if (error != null) {
                continuation.resume(null)
                return@loadFileRepresentationForTypeIdentifier
            }

            continuation.resume(
                url?.createTempFile()?.let { tempUrl ->
                    KmpFile(
                        url = url,
                        tempUrl = tempUrl,
                    )
                }
            )
        }

        continuation.invokeOnCancellation {
            progress.cancel()
        }
    }
}

private fun createUIDocumentPickerViewController(
    delegate: UIDocumentPickerDelegateProtocol,
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
): UIDocumentPickerViewController {
    val contentTypes =
        if (type is FilePickerFileType.Extension)
            type.value
                .mapNotNull { extension ->
                    UTType.typeWithFilenameExtension(extension)
                }
                .ifEmpty { listOf(UTTypeData) }
        else
            type.value.mapNotNull { mimeType ->
                when (mimeType) {
                    FilePickerFileType.ImageContentType -> UTTypeImage
                    FilePickerFileType.VideoContentType -> UTTypeVideo
                    FilePickerFileType.AudioContentType -> UTTypeAudio
                    FilePickerFileType.DocumentContentType -> UTTypeApplication
                    FilePickerFileType.TextContentType -> UTTypeText
                    FilePickerFileType.AllContentType -> UTTypeData
                    FilePickerFileType.FolderContentType -> UTTypeFolder
                    else -> UTType.typeWithMIMEType(mimeType)
                }
            }

    val pickerController =
        UIDocumentPickerViewController(
            forOpeningContentTypes = contentTypes,
            asCopy = type != FilePickerFileType.Folder,
        )
    pickerController.delegate = delegate
    pickerController.allowsMultipleSelection = selectionMode is FilePickerSelectionMode.Multiple
    return pickerController
}


private fun createPHPickerViewController(
    delegate: PHPickerViewControllerDelegateProtocol,
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
): PHPickerViewController {
    val configuration = PHPickerConfiguration()
    val filterList = mutableListOf<PHPickerFilter>()
    when (type) {
        FilePickerFileType.Image ->
            filterList.add(PHPickerFilter.imagesFilter())

        FilePickerFileType.Video -> {
            filterList.add(PHPickerFilter.videosFilter())
            filterList.add(PHPickerFilter.livePhotosFilter())
        }

        else -> {
            filterList.add(PHPickerFilter.imagesFilter())
            filterList.add(PHPickerFilter.videosFilter())
        }
    }
    val newFilter = PHPickerFilter.anyFilterMatchingSubfilters(filterList.toList())
    configuration.filter = newFilter
    configuration.preferredAssetRepresentationMode =
        PHPickerConfigurationAssetRepresentationModeCurrent
    configuration.selectionLimit = when (selectionMode) {
        is FilePickerSelectionMode.Multiple -> selectionMode.maxItems?.toLong() ?: 0
        else -> 1
    }
    val picker = PHPickerViewController(configuration)
    picker.delegate = delegate
    return picker
}


@Stable
actual class FilePickerLauncher actual constructor(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    private val onLaunch: () -> Unit,
) {
    actual fun launch() {
        onLaunch()
    }
}
