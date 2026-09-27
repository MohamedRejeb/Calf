package com.mohamedrejeb.calf.sample.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.source
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.FilePickerSettings
import com.mohamedrejeb.calf.picker.ImageRepresentationMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.picker.rememberFilePickerSettings
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.sample.navigation.Screen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.RawSource

private const val USAGE = """
val launcher = rememberFilePickerLauncher(
    type = FilePickerFileType.Pdf,
    selectionMode = FilePickerSelectionMode.Multiple,
    settings = rememberFilePickerSettings(title = "Attach files"),
) { files -> attachments = files }

AdaptiveButton(onClick = { launcher.launch() }) { Text("Attach") }

// Stream it with kotlinx-io, whatever its size
file.source().buffered().use { source -> upload(source) }
"""

internal const val STREAM_CHUNK_SIZE = 64L * 1024
private const val DEFAULT_DIALOG_TITLE = "Attach files"
private const val FOLDER_DIALOG_TITLE = "Attach a folder"
private const val START_FOLDER_DIALOG_TITLE = "Choose where the picker opens"

/** A file type the demo can filter on, with the tile its option row shows. */
internal data class FileTypeOption(
    val label: String,
    val type: FilePickerFileType,
    val kind: FileKind,
)

internal val FileTypeOptions = listOf(
    FileTypeOption("All files", FilePickerFileType.All, FileKind.Other),
    FileTypeOption("Images", FilePickerFileType.Image, FileKind.Image),
    FileTypeOption("Videos", FilePickerFileType.Video, FileKind.Video),
    FileTypeOption("Audio", FilePickerFileType.Audio, FileKind.Audio),
    FileTypeOption("PDF", FilePickerFileType.Pdf, FileKind.Pdf),
    FileTypeOption("Documents", FilePickerFileType.Document, FileKind.Document),
    FileTypeOption("Text", FilePickerFileType.Text, FileKind.Text),
)

/** How far reading a picked file with kotlinx-io got. */
internal sealed interface ReadResult {
    data object Reading : ReadResult
    data class Read(val bytes: Long) : ReadResult
    data class Failed(val reason: String) : ReadResult
    data object Folder : ReadResult
}

@Immutable
internal data class PickedFile(
    val file: KmpFile,
    val name: String,
    val path: String,
    val kind: FileKind,
    val read: ReadResult,
)

@Composable
fun FilePickerScreen(navigateBack: () -> Unit) {
    var multiple by rememberSaveable { mutableStateOf(true) }
    var dialogTitle by rememberSaveable { mutableStateOf(DEFAULT_DIALOG_TITLE) }
    var startFolder by rememberSaveable { mutableStateOf<String?>(null) }
    var typeIndex by rememberSaveable { mutableIntStateOf(0) }
    val attachments = rememberAttachmentsState()
    val foldersSupported = !currentPlatform.isWeb

    val settings = rememberFilePickerSettings(
        title = dialogTitle,
        initialDirectory = startFolder,
        // Keeps photos in their original format on iOS (HEIC included); ignored elsewhere.
        imageRepresentationMode = ImageRepresentationMode.Current,
    )
    val type = FileTypeOptions[typeIndex].type
    val singleLauncher = rememberFilePickerLauncher(
        type = type,
        selectionMode = FilePickerSelectionMode.Single,
        settings = settings,
        onResult = attachments::onFilesPicked,
    )
    val multipleLauncher = rememberFilePickerLauncher(
        type = type,
        selectionMode = FilePickerSelectionMode.Multiple,
        settings = settings,
        onResult = attachments::onFilesPicked,
    )
    val folderLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Folder,
        settings = FilePickerSettings(title = FOLDER_DIALOG_TITLE),
        onResult = attachments::onFolderPicked,
    )
    val startFolderLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Folder,
        settings = FilePickerSettings(title = START_FOLDER_DIALOG_TITLE),
        onResult = { folders -> folders.firstOrNull()?.getPath()?.let { startFolder = it } },
    )

    SampleScreenScaffold(title = Screen.FilePicker.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.FilePicker)
            FilePickerAttachmentsSection(
                files = attachments.files,
                multiple = multiple,
                foldersSupported = foldersSupported,
                onPickFiles = { if (multiple) multipleLauncher.launch() else singleLauncher.launch() },
                onPickFolder = { folderLauncher.launch() },
            )
            FilePickerTypeSection(selectedIndex = typeIndex, onSelect = { typeIndex = it })
            FilePickerOptionsSection(
                multiple = multiple,
                onMultipleChange = { multiple = it },
                dialogTitle = dialogTitle,
                onDialogTitleChange = { dialogTitle = it },
                startFolder = startFolder,
                onPickStartFolder = if (foldersSupported) ({ startFolderLauncher.launch() }) else null,
                onResetStartFolder = { startFolder = null },
            )
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun rememberAttachmentsState(): AttachmentsState {
    val scope = rememberCoroutineScope()
    return remember(scope) { AttachmentsState(scope) }
}

/**
 * The picked files, and the job that measures them by streaming each one with kotlinx-io. A new
 * pick cancels the reads of the previous one.
 */
@Stable
private class AttachmentsState(private val scope: CoroutineScope) {
    var files by mutableStateOf<List<PickedFile>>(emptyList())
        private set

    private var readJob: Job? = null

    fun onFilesPicked(picked: List<KmpFile>) {
        readJob?.cancel()
        files = picked.map { it.toPickedFile(ReadResult.Reading) }
        readJob = scope.launch {
            picked.forEach { file ->
                val result = file.readThroughSource()
                files = files.map { if (it.file == file) it.copy(read = result) else it }
            }
        }
    }

    fun onFolderPicked(picked: List<KmpFile>) {
        readJob?.cancel()
        files = picked.map { it.toPickedFile(ReadResult.Folder).copy(kind = FileKind.Folder) }
    }
}

private fun KmpFile.toPickedFile(read: ReadResult): PickedFile {
    val name = getName().orEmpty()
    return PickedFile(
        file = this,
        name = name,
        path = getPath() ?: "Unknown path",
        kind = FileKind.of(name),
        read = read,
    )
}

/**
 * Reads the whole file through the kotlinx-io [source] extension in fixed-size chunks, so the
 * file never has to fit in memory. The byte count is the size the demo shows.
 */
private suspend fun KmpFile.readThroughSource(): ReadResult = withContext(Dispatchers.Default) {
    try {
        ReadResult.Read(bytes = source().use { it.countBytes() })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ReadResult.Failed(reason = e.message ?: "Could not read the file")
    }
}

private fun RawSource.countBytes(): Long {
    val chunk = Buffer()
    var total = 0L
    while (true) {
        val read = readAtMostTo(chunk, STREAM_CHUNK_SIZE)
        if (read == -1L) return total
        total += read
        chunk.clear()
    }
}
