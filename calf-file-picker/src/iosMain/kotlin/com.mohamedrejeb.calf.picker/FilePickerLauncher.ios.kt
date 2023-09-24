package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
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
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.UniformTypeIdentifiers.UTTypeText
import platform.UniformTypeIdentifiers.UTTypeVideo
import platform.darwin.NSObject

@BetaInteropApi
@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL
            ) {
                onResult(listOf(didPickDocumentAtURL))
            }

            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val dataList = didPickDocumentsAtURLs.mapNotNull {
                    it as? NSURL
                }
                onResult(dataList)
            }
        }
    }

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                val pickerController = createUIDocumentPickerViewController(
                    delegate = delegate,
                    type = type,
                    selectionMode = selectionMode,
                )

                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                    pickerController,
                    true,
                    null
                )
            }
        )
    }
}

private fun createUIDocumentPickerViewController(
    delegate: UIDocumentPickerDelegateProtocol,
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
): UIDocumentPickerViewController {
    val contentTypes = type.value.mapNotNull { mimeType ->
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

    val pickerController = UIDocumentPickerViewController(
        forOpeningContentTypes = contentTypes,
        asCopy = type != FilePickerFileType.Folder,
    )
    pickerController.delegate = delegate
    pickerController.allowsMultipleSelection = selectionMode == FilePickerSelectionMode.Multiple
    return pickerController
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