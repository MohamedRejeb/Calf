package com.mohamedrejeb.calf.picker

import android.content.ActivityNotFoundException
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.initializeKmpFileContext

@OptIn(InternalCalfApi::class)
@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    settings: FilePickerSettings,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    initializeKmpFileContext(LocalContext.current.applicationContext)

    return when (selectionMode) {
        FilePickerSelectionMode.Single -> {
            when (type) {
                FilePickerFileType.Image, FilePickerFileType.Video, FilePickerFileType.ImageVideo ->
                    pickSingleVisualMedia(
                        type = type,
                        onResult = onResult,
                    )
                FilePickerFileType.Folder ->
                    pickFolder(
                        type = type,
                        onResult = onResult,
                    )
                else ->
                    pickSingleFile(
                        type = type,
                        onResult = onResult,
                    )
            }
        }
        is FilePickerSelectionMode.Multiple -> {
            when (type) {
                FilePickerFileType.Image, FilePickerFileType.Video, FilePickerFileType.ImageVideo ->
                    pickMultipleVisualMedia(
                        type = type,
                        maxItems = selectionMode.maxItems,
                        onResult = onResult,
                    )
                FilePickerFileType.Folder ->
                    pickFolder(
                        type = type,
                        onResult = onResult,
                    )
                else ->
                    pickMultipleFiles(
                        type = type,
                        maxItems = selectionMode.maxItems,
                        onResult = onResult,
                    )
            }
        }
    }
}

@Composable
private fun pickSingleVisualMedia(
    type: FilePickerFileType,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val currentType by rememberUpdatedState(type)

    val documentFallbackLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                onResult(uri?.let { listOf(KmpFile(it)) } ?: emptyList())
            },
        )

    val mediaPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                onResult(uri?.let { listOf(KmpFile(it)) } ?: emptyList())
            },
        )

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = FilePickerSelectionMode.Single,
            onLaunch = {
                try {
                    mediaPickerLauncher.launch(
                        currentType.toPickVisualMediaRequest(),
                    )
                } catch (_: ActivityNotFoundException) {
                    documentFallbackLauncher.launch(
                        currentType.toVisualMediaMimeTypes(),
                    )
                }
            },
        )
    }
}

@Composable
private fun pickMultipleVisualMedia(
    type: FilePickerFileType,
    maxItems: Int?,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val currentType by rememberUpdatedState(type)

    val documentFallbackLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
            onResult = { uriList ->
                val fileList = uriList
                    .map { uri -> KmpFile(uri) }
                    .let { if (maxItems != null) it.take(maxItems) else it }
                onResult(fileList)
            },
        )

    val contract = if (maxItems != null) {
        ActivityResultContracts.PickMultipleVisualMedia(maxItems)
    } else {
        ActivityResultContracts.PickMultipleVisualMedia()
    }

    val mediaPickerLauncher =
        rememberLauncherForActivityResult(
            contract = contract,
            onResult = { uriList ->
                val fileList =
                    uriList.map { uri ->
                        KmpFile(uri)
                    }
                onResult(fileList)
            },
        )

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = FilePickerSelectionMode.Multiple,
            onLaunch = {
                try {
                    mediaPickerLauncher.launch(
                        currentType.toPickVisualMediaRequest(),
                    )
                } catch (_: ActivityNotFoundException) {
                    documentFallbackLauncher.launch(
                        currentType.toVisualMediaMimeTypes(),
                    )
                }
            },
        )
    }
}

@Composable
private fun pickSingleFile(
    type: FilePickerFileType,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val currentType by rememberUpdatedState(type)

    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                onResult(uri?.let { listOf(KmpFile(it)) } ?: emptyList())
            },
        )

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = FilePickerSelectionMode.Single,
            onLaunch = {
                val mimeTypeMap = MimeTypeMap.getSingleton()

                filePickerLauncher.launch(
                    if (currentType is FilePickerFileType.Extension)
                        currentType.value.mapNotNull { mimeTypeMap.getMimeTypeFromExtension(it) }.toTypedArray()
                    else
                        currentType.value.toList().toTypedArray()
                )
            },
        )
    }
}

@Composable
private fun pickMultipleFiles(
    type: FilePickerFileType,
    maxItems: Int?,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val currentType by rememberUpdatedState(type)

    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
            onResult = { uriList ->
                val fileList = uriList
                    .map { uri -> KmpFile(uri) }
                    .let { if (maxItems != null) it.take(maxItems) else it }
                onResult(fileList)
            },
        )

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = FilePickerSelectionMode.Multiple,
            onLaunch = {
                val mimeTypeMap = MimeTypeMap.getSingleton()

                filePickerLauncher.launch(
                    if (currentType is FilePickerFileType.Extension)
                        currentType.value.mapNotNull { mimeTypeMap.getMimeTypeFromExtension(it) }.toTypedArray()
                    else
                        currentType.value.toList().toTypedArray()
                )
            },
        )
    }
}

@Composable
private fun pickFolder(
    type: FilePickerFileType,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val singlePhotoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
            onResult = { uri ->
                onResult(uri?.let { listOf(KmpFile(it)) } ?: emptyList())
            },
        )

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = FilePickerSelectionMode.Single,
            onLaunch = {
                singlePhotoPickerLauncher.launch(null)
            },
        )
    }
}

internal fun FilePickerFileType.toVisualMediaMimeTypes(): Array<String> {
    return when (this) {
        FilePickerFileType.Image -> arrayOf(FilePickerFileType.ImageContentType)
        FilePickerFileType.Video -> arrayOf(FilePickerFileType.VideoContentType)
        else -> arrayOf(FilePickerFileType.ImageContentType, FilePickerFileType.VideoContentType)
    }
}

internal fun FilePickerFileType.toPickVisualMediaRequest(): PickVisualMediaRequest {
    return when (this) {
        FilePickerFileType.Image -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        FilePickerFileType.Video -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        else -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
    }
}

internal fun FilePickerFileType.isVisualMedia(): Boolean {
    return when (this) {
        FilePickerFileType.Image -> true
        FilePickerFileType.Video -> true
        FilePickerFileType.ImageVideo -> true
        else -> false
    }
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
