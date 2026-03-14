package com.mohamedrejeb.calf.picker

import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mohamedrejeb.calf.io.KmpFile

@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    return when (selectionMode) {
        FilePickerSelectionMode.Single -> {
            when (type) {
                is FilePickerFileType.Image, FilePickerFileType.Video, FilePickerFileType.ImageVideo ->
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
        FilePickerSelectionMode.Multiple -> {
            when (type) {
                is FilePickerFileType.Image, FilePickerFileType.Video, FilePickerFileType.ImageVideo ->
                    pickMultipleVisualMedia(
                        type = type,
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
                mediaPickerLauncher.launch(
                    type.toPickVisualMediaRequest(),
                )
            },
        )
    }
}

@Composable
fun pickMultipleVisualMedia(
    type: FilePickerFileType,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val mediaPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(),
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
                mediaPickerLauncher.launch(
                    type.toPickVisualMediaRequest(),
                )
            },
        )
    }
}

@Composable
private fun pickSingleFile(
    type: FilePickerFileType,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
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
                    if (type is FilePickerFileType.Extension)
                        type.value.mapNotNull { mimeTypeMap.getMimeTypeFromExtension(it) }.toTypedArray()
                    else
                        type.value.toList().toTypedArray()
                )
            },
        )
    }
}

@Composable
private fun pickMultipleFiles(
    type: FilePickerFileType,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
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
                val mimeTypeMap = MimeTypeMap.getSingleton()

                filePickerLauncher.launch(
                    if (type is FilePickerFileType.Extension)
                        type.value.mapNotNull { mimeTypeMap.getMimeTypeFromExtension(it) }.toTypedArray()
                    else
                        type.value.toList().toTypedArray()
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

internal fun FilePickerFileType.toPickVisualMediaRequest(): PickVisualMediaRequest {
    return when (this) {
        is FilePickerFileType.Image -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        FilePickerFileType.Video -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        else -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
    }
}

internal fun FilePickerFileType.isVisualMedia(): Boolean {
    return when (this) {
        is FilePickerFileType.Image-> true
        FilePickerFileType.Video -> true
        FilePickerFileType.ImageVideo -> true
        else -> false
    }
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
