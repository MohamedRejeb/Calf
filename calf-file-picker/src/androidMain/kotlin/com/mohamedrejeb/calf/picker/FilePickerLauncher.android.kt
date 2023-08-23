package com.mohamedrejeb.calf.picker

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
    val context = LocalContext.current
    return when (selectionMode) {
        FilePickerSelectionMode.Single -> {
            val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
                onResult = { uri ->
                    val file = URIPathHelper.getPath(context, uri)?.let { File(it) }
                    file?.let { onResult(listOf(it)) }
                }
            )

            remember {
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
        FilePickerSelectionMode.Multiple -> {
            val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickMultipleVisualMedia(),
                onResult = { uriList ->
                    val fileList = uriList.mapNotNull { uri ->
                        URIPathHelper.getPath(context, uri)?.let { File(it) }
                    }
                    onResult(fileList)
                }
            )

            remember {
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