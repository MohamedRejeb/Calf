package com.mohamedrejeb.calf.picker

import android.content.ActivityNotFoundException
import android.net.Uri
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
                        initialDirectory = settings.initialDirectory,
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
                        initialDirectory = settings.initialDirectory,
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
                filePickerLauncher.launch(currentType.toMimeTypes())
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
                filePickerLauncher.launch(currentType.toMimeTypes())
            },
        )
    }
}

@Composable
private fun pickFolder(
    type: FilePickerFileType,
    initialDirectory: String?,
    onResult: (List<KmpFile>) -> Unit,
): FilePickerLauncher {
    val currentInitialDirectory by rememberUpdatedState(initialDirectory)

    val folderPickerLauncher =
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
                val initialUri = currentInitialDirectory?.let { Uri.parse(it) }
                folderPickerLauncher.launch(initialUri)
            },
        )
    }
}

/**
 * Additional MIME type mappings for extensions where [MimeTypeMap] may
 * return only one variant, causing files to be missing in the picker.
 */
private val extensionMimeTypeFallbacks = mapOf(
    "csv" to listOf("text/csv", "text/comma-separated-values", "application/csv"),
)

/**
 * Resolves MIME types for a [FilePickerFileType], handling extensions that need
 * multiple MIME type variants to work reliably across devices.
 */
internal fun FilePickerFileType.toMimeTypes(): Array<String> {
    if (this is FilePickerFileType.Extension) {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return value.flatMap { ext ->
            val lowerExt = ext.lowercase()
            val fallbacks = extensionMimeTypeFallbacks[lowerExt]
            if (fallbacks != null) {
                // Use known fallbacks + system mapping (deduplicated)
                val systemType = mimeTypeMap.getMimeTypeFromExtension(lowerExt)
                (fallbacks + listOfNotNull(systemType)).distinct()
            } else {
                listOfNotNull(mimeTypeMap.getMimeTypeFromExtension(lowerExt))
            }
        }.toTypedArray()
    }
    return value.toList().toTypedArray()
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
