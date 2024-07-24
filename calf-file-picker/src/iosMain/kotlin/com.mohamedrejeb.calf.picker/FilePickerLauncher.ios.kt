package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCurrent
import platform.PhotosUI.PHPickerConfigurationSelectionOrdered
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeApplication
import platform.UniformTypeIdentifiers.UTTypeAudio
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeText
import platform.UniformTypeIdentifiers.UTTypeVideo
import platform.darwin.NSObject

@BetaInteropApi
@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher =
    if (type == FilePickerFileType.Image || type == FilePickerFileType.Video || type == FilePickerFileType.ImageVideo) {
        rememberImageVideoPickerLauncher(type, selectionMode, onResult)
    } else {
        rememberDocumentPickerLauncher(type, selectionMode, onResult)
    }

@OptIn(InternalCalfApi::class)
@Composable
private fun rememberDocumentPickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val coroutineScope = rememberCoroutineScope()
    val delegate =
        remember {
            object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentAtURL: NSURL,
                ) {
                    coroutineScope.launch(Dispatchers.Main) {
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

                    coroutineScope.launch(Dispatchers.Main) {
                        onResult(dataList)
                    }
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    coroutineScope.launch(Dispatchers.Main) {
                        onResult(emptyList())
                    }
                }
            }
        }

    return remember {
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

                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                    pickerController,
                    true,
                    null,
                )
            },
        )
    }
}

@OptIn(InternalCalfApi::class)
@Composable
private fun rememberImageVideoPickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {

    val pickerDelegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(
                picker: PHPickerViewController,
                didFinishPicking: List<*>,
            ) {
                var pendingOperations = didFinishPicking.size
                val results = mutableListOf<KmpFile>()

                didFinishPicking.forEach {
                    val result = it as? PHPickerResult ?: return@forEach


                    println(result.itemProvider.registeredTypeIdentifiers.size)
                    result.itemProvider.loadFileRepresentationForTypeIdentifier(
                        typeIdentifier = result.itemProvider.registeredTypeIdentifiers.firstOrNull() as? String ?: UTTypeImage.identifier) { url, error ->
                        if (error != null) {
                            return@loadFileRepresentationForTypeIdentifier
                        }

                        url?.createTempFile()?.let { tempUrl ->
                            results.add(
                                KmpFile(
                                    url = url,
                                    tempUrl = tempUrl,
                                )
                            )
                        }

                        pendingOperations--

                        if(pendingOperations == 0){
                            //only call one onResult, in file picker it isnt called twice
                            onResult(results)
                        }
                    }
                }
                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }

    return remember {
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

                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                    imagePicker,
                    true,
                    null,
                )
            },
        )
    }
}

@OptIn(InternalCalfApi::class)
private suspend fun loadFileRepresentationForTypeIdentifier(result: PHPickerResult): List<KmpFile> {
    val results = mutableListOf<KmpFile>()


    return results
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
    val filterList = mutableListOf<PHPickerFilter>()
    when (type) {
        FilePickerFileType.Image ->
            filterList.add(PHPickerFilter.imagesFilter())

        FilePickerFileType.Video ->
            filterList.add(PHPickerFilter.videosFilter())

        else -> {
            filterList.add(PHPickerFilter.imagesFilter())
            filterList.add(PHPickerFilter.videosFilter())
        }
    }
    val newFilter =
        PHPickerFilter.anyFilterMatchingSubfilters(filterList.toList())
    configuration.filter = newFilter
    configuration.preferredAssetRepresentationMode =
        PHPickerConfigurationAssetRepresentationModeCurrent
    configuration.selection = PHPickerConfigurationSelectionOrdered
    configuration.selectionLimit = if (selectionMode == FilePickerSelectionMode.Multiple) 0 else 1
    configuration.preselectedAssetIdentifiers = listOf<Nothing>()
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
