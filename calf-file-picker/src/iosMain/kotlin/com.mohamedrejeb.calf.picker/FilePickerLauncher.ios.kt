package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.*
import com.mohamedrejeb.calf.io.KmpFile
import kotlinx.cinterop.BetaInteropApi
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.*
import platform.UIKit.UIApplication
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.darwin.DISPATCH_QUEUE_CONCURRENT
import platform.darwin.NSObject

@BetaInteropApi
@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val pickerDelegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)
                println("didFinishPicking: $didFinishPicking")
                didFinishPicking.forEach {
                    val result = it as? PHPickerResult ?: return@forEach
                    if (result.itemProvider.hasItemConformingToTypeIdentifier(UTTypeMovie.identifier)) {
                        println("video")
                    } else if (result.itemProvider.hasItemConformingToTypeIdentifier(UTTypeImage.identifier)) {
                        result.itemProvider.loadDataRepresentationForTypeIdentifier(
                            typeIdentifier = UTTypeImage.identifier,
                        ) { data, error ->
                            if (error != null) {
                                println("Error: $error")
                                return@loadDataRepresentationForTypeIdentifier
                            }
                            onResult(listOfNotNull(data))
                        }
                    }
                }
            }
        }
    }

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                val imagePicker = createPHPickerViewController(
                    delegate = pickerDelegate,
                    selectionMode = selectionMode,
                )
                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                    imagePicker,
                    true,
                    null
                )
            }
        )
    }
}

private fun createPHPickerViewController(
    delegate: PHPickerViewControllerDelegateProtocol,
    selectionMode: FilePickerSelectionMode,
): PHPickerViewController {
    val configuration = PHPickerConfiguration(PHPhotoLibrary.sharedPhotoLibrary())
    val newFilter = PHPickerFilter.anyFilterMatchingSubfilters(listOf(PHPickerFilter.imagesFilter(), PHPickerFilter.videosFilter()))
    configuration.filter = newFilter
    configuration.preferredAssetRepresentationMode = PHPickerConfigurationAssetRepresentationModeCurrent
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