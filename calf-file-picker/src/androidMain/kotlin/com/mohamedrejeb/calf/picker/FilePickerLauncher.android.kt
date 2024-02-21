package com.mohamedrejeb.calf.picker

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mohamedrejeb.calf.io.KmpFile
import java.io.File

@Composable
actual fun rememberFilePickerLauncher(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    return when (selectionMode) {
        FilePickerSelectionMode.Single -> {
            when (type) {
                FilePickerFileType.Image, FilePickerFileType.Video, FilePickerFileType.ImageVideo ->
                    pickSingleVisualMedia(
                        type = type,
                        selectionMode = selectionMode,
                        onResult = onResult,
                    )
                FilePickerFileType.Folder ->
                    pickFolder(
                        type = type,
                        selectionMode = selectionMode,
                        onResult = onResult,
                    )
                else ->
                    pickSingleFile(
                        type = type,
                        selectionMode = selectionMode,
                        onResult = onResult,
                    )
            }
        }
        FilePickerSelectionMode.Multiple -> {
            when (type) {
                FilePickerFileType.Image, FilePickerFileType.Video, FilePickerFileType.ImageVideo ->
                    pickMultipleVisualMedia(
                        type = type,
                        selectionMode = selectionMode,
                        onResult = onResult,
                    )
                FilePickerFileType.Folder ->
                    pickFolder(
                        type = type,
                        selectionMode = selectionMode,
                        onResult = onResult,
                    )
                else ->
                    pickMultipleFiles(
                        type = type,
                        selectionMode = selectionMode,
                        onResult = onResult,
                    )
            }
        }
    }
}

@Composable
private fun pickSingleVisualMedia(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val context = LocalContext.current

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            val file = URIPathHelper.getPath(context, uri)?.let { File(it) }
            file?.let { onResult(listOf(it)) } ?: onResult(emptyList())
        }
    )

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                singlePhotoPickerLauncher.launch(
                    type.toPickVisualMediaRequest()
                )
            }
        )
    }
}

@Composable
fun pickMultipleVisualMedia(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val context = LocalContext.current

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uriList ->
            val fileList = uriList.mapNotNull { uri ->
                URIPathHelper.getPath(context, uri)?.let { File(it) }
            }
            onResult(fileList)
        }
    )

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                singlePhotoPickerLauncher.launch(
                    type.toPickVisualMediaRequest()
                )
            }
        )
    }
}

@Composable
private fun pickSingleFile(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val context = LocalContext.current

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            val file = URIPathHelper.getPath(context, uri)?.let { File(it) }
            file?.let { onResult(listOf(it)) } ?: onResult(emptyList())
        }
    )

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                singlePhotoPickerLauncher.launch(
                    type.value.first()
                )
            }
        )
    }
}

@Composable
private fun pickMultipleFiles(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val context = LocalContext.current

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uriList ->
            val fileList = uriList.mapNotNull { uri ->
                URIPathHelper.getPath(context, uri)?.let { File(it) }
            }
            onResult(fileList)
        }
    )

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                singlePhotoPickerLauncher.launch(
                    type.value.first()
                )
            }
        )
    }
}

@Composable
private fun pickFolder(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val context = LocalContext.current

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val file = URIPathHelper.getPath(context, uri)?.let { File(it) }
                file?.let { onResult(listOf(it)) } ?: onResult(emptyList())
            }
        }
    )

    return remember {
        FilePickerLauncher(
            type = type,
            selectionMode = selectionMode,
            onLaunch = {
                singlePhotoPickerLauncher.launch(null)
            }
        )
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

actual class FilePickerLauncher actual constructor(
    type: FilePickerFileType,
    selectionMode: FilePickerSelectionMode,
    private val onLaunch: () -> Unit,
) {
    actual fun launch() {
        onLaunch()
    }
}
