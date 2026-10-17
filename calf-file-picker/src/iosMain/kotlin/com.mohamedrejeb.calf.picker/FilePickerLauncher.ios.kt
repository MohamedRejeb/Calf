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
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCompatible
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCurrent
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIPresentationController
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
    onError: (FilePickerException) -> Unit,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher =
    if (type == FilePickerFileType.Image || type == FilePickerFileType.Video || type == FilePickerFileType.ImageVideo) {
        rememberImageVideoPickerLauncher(type, selectionMode, settings, onError, onResult)
    } else {
        rememberDocumentPickerLauncher(type, selectionMode, settings, onError, onResult)
    }

@OptIn(InternalCalfApi::class, BetaInteropApi::class)
@Composable
private fun rememberDocumentPickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    settings: FilePickerSettings,
    onError: (FilePickerException) -> Unit,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val scope = rememberCoroutineScope()
    val currentUIViewController = LocalUIViewController.current
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnError by rememberUpdatedState(onError)
    val currentType by rememberUpdatedState(type)
    val currentSelectionMode by rememberUpdatedState(selectionMode)
    val currentInitialDirectory by rememberUpdatedState(settings.initialDirectory)

    val delegate = remember {
        DocumentPickerDelegate(
            onPicked = { urls ->
                scope.launch(Dispatchers.Main) {
                    val maxItems = (currentSelectionMode as? FilePickerSelectionMode.Multiple)?.maxItems
                    val picked = if (maxItems != null) urls.take(maxItems) else urls
                    val staged =
                        if (currentType == FilePickerFileType.Folder)
                            StagedPick(files = picked.map { KmpFile(it) }, failure = null)
                        else
                            withContext(Dispatchers.IO) { stagePickedUrls(picked) }

                    staged.failure?.let { currentOnError(it) }
                    currentOnResult(staged.files)
                }
            },
            onCancelled = {
                scope.launch(Dispatchers.Main) {
                    currentOnResult(emptyList())
                }
            },
        )
    }

    return remember(currentUIViewController) {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                val pickerController =
                    createUIDocumentPickerViewController(
                        type = currentType,
                        selectionMode = currentSelectionMode,
                        initialDirectory = currentInitialDirectory,
                    )
                delegate.attachTo(pickerController)

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
    settings: FilePickerSettings,
    onError: (FilePickerException) -> Unit,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val scope = rememberCoroutineScope()
    val currentUIViewController = LocalUIViewController.current
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnError by rememberUpdatedState(onError)
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
                val processResult = hasFinished.compareAndSet(0, 1)

                if (didFinishPicking.isNotEmpty()) {
                    scope.launch {
                        val loaded = didFinishPicking
                            .mapNotNull {
                                val result = it as? PHPickerResult ?: return@mapNotNull null

                                async {
                                    result.itemProvider.loadFileRepresentationForTypeIdentifierSuspend(
                                        currentType
                                    )
                                }
                            }
                            .awaitAll()
                        val files = loaded.filterNotNull()

                        withContext(Dispatchers.Main) {
                            partialPickFailure(failed = loaded.size - files.size, picked = loaded.size)
                                ?.let { currentOnError(it) }
                            currentOnResult(files)
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
                        imageRepresentationMode = settings.imageRepresentationMode,
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

            // The provider deletes its file when this callback returns, so copy it now.
            continuation.resume(url?.toStagedKmpFile())
        }

        continuation.invokeOnCancellation {
            progress.cancel()
        }
    }
}

private fun createUIDocumentPickerViewController(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    initialDirectory: String?,
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
    pickerController.allowsMultipleSelection = selectionMode is FilePickerSelectionMode.Multiple
    pickerController.directoryURL = initialDirectoryUrl(initialDirectory)
    return pickerController
}


private fun createPHPickerViewController(
    delegate: PHPickerViewControllerDelegateProtocol,
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    imageRepresentationMode: ImageRepresentationMode = ImageRepresentationMode.Compatible,
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
    configuration.preferredAssetRepresentationMode = when (imageRepresentationMode) {
        ImageRepresentationMode.Compatible -> PHPickerConfigurationAssetRepresentationModeCompatible
        ImageRepresentationMode.Current -> PHPickerConfigurationAssetRepresentationModeCurrent
    }
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
