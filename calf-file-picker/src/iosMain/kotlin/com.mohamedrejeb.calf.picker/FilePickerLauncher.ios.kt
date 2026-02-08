package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.uikit.LocalUIViewController
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSItemProvider
import platform.Foundation.NSURL
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCompatible
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCurrent
import platform.PhotosUI.PHPickerConfigurationSelectionOrdered
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeApplication
import platform.UniformTypeIdentifiers.UTTypeAudio
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeText
import platform.UniformTypeIdentifiers.UTTypeVideo
import platform.darwin.NSObject
import kotlin.coroutines.resume
import platform.UniformTypeIdentifiers.UTTypeMovie

@OptIn(BetaInteropApi::class)
@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher =
    if (type is FilePickerFileType.Image || type == FilePickerFileType.Video || type == FilePickerFileType.ImageVideo) {
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

    val delegate =
        remember {
            object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentAtURL: NSURL,
                ) {
                    scope.launch(Dispatchers.Main) {
                        val result =
                            if (type == FilePickerFileType.Folder)
                                listOf(KmpFile(didPickDocumentAtURL))
                            else
                                listOfNotNull(
                                    didPickDocumentAtURL.createTempFile()?.let { tempUrl ->
                                        KmpFile(
                                            url = didPickDocumentAtURL,
                                            tempUrl = tempUrl,
                                        )
                                    }
                                )

                        onResult(result)
                    }
                }

                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAtURLs: List<*>,
                ) {
                    val dataList =
                        didPickDocumentsAtURLs.mapNotNull {
                            val nsUrl = it as? NSURL ?: return@mapNotNull null
                            if (type == FilePickerFileType.Folder)
                                KmpFile(nsUrl)
                            else
                                nsUrl.createTempFile()?.let { tempUrl ->
                                    KmpFile(
                                        url = nsUrl,
                                        tempUrl = tempUrl
                                    )
                                }
                        }

                    scope.launch(Dispatchers.Main) {
                        onResult(dataList)
                    }
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    scope.launch(Dispatchers.Main) {
                        onResult(emptyList())
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
                        type = type,
                        selectionMode = selectionMode,
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

    val pickerDelegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(
                picker: PHPickerViewController,
                didFinishPicking: List<*>,
            ) {
                scope.launch {
                    val results = didFinishPicking
                        .mapNotNull {
                            val result = it as? PHPickerResult ?: return@mapNotNull null

                            async {
                                result.itemProvider.loadFileRepresentationForTypeIdentifierSuspend(type)
                            }
                        }
                        .awaitAll()
                        .filterNotNull()

                    withContext(Dispatchers.Main) {
                        onResult(results)
                    }
                }

                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }

    return remember(currentUIViewController) {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                val imagePicker =
                    createPHPickerViewController(
                        delegate = pickerDelegate,
                        type = type,
                        selectionMode = selectionMode,
                    )

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
private suspend fun NSItemProvider.loadFileRepresentationForTypeIdentifierSuspend(type: FilePickerFileType): KmpFile? =
    suspendCancellableCoroutine { continuation ->
        val identifier = when(type) {
            is FilePickerFileType.Image -> UTTypeImage.identifier
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
    pickerController.allowsMultipleSelection = selectionMode == FilePickerSelectionMode.Multiple
    return pickerController
}


private fun createPHPickerViewController(
    delegate: PHPickerViewControllerDelegateProtocol,
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
): PHPickerViewController {
    val configuration = PHPickerConfiguration(PHPhotoLibrary.sharedPhotoLibrary())
    configuration.preferredAssetRepresentationMode = PHPickerConfigurationAssetRepresentationModeCurrent
    val filterList = mutableListOf<PHPickerFilter>()
    when (type) {
        is FilePickerFileType.Image -> {
            if(type.isCompat) {
                configuration.preferredAssetRepresentationMode =
                    PHPickerConfigurationAssetRepresentationModeCompatible
            }
            filterList.add(PHPickerFilter.imagesFilter())
        }

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
    configuration.selectionLimit = if (selectionMode == FilePickerSelectionMode.Multiple) 0 else 1
    val picker = PHPickerViewController(configuration)
    picker.delegate = delegate
    return picker
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
