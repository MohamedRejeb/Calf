package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.fromUriString
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getMimeType
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.getSize
import com.mohamedrejeb.calf.io.toUriString
import com.mohamedrejeb.calf.io.source
import com.mohamedrejeb.calf.picker.FilePickerException
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.FilePickerSettings
import com.mohamedrejeb.calf.picker.ImageRepresentationMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.picker.rememberFilePickerSettings
import com.mohamedrejeb.calf.sample.Platform
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.RawSource

private data class FileTypeOption(
    val label: String,
    val type: FilePickerFileType,
)

private val fileTypeOptions = listOf(
    FileTypeOption("All", FilePickerFileType.All),
    FileTypeOption("Image", FilePickerFileType.Image),
    FileTypeOption("Video", FilePickerFileType.Video),
    FileTypeOption("Audio", FilePickerFileType.Audio),
    FileTypeOption("PDF", FilePickerFileType.Pdf),
    FileTypeOption("Document", FilePickerFileType.Document),
    FileTypeOption("Text", FilePickerFileType.Text),
)

private const val STREAM_CHUNK_SIZE = 64L * 1024
private const val STREAMING_IN_PROGRESS = "Streaming with kotlinx-io…"
private const val DIRECTORY_NOT_STREAMED = "Directory, not streamed"

private data class PickedFileInfo(
    val file: KmpFile,
    val name: String,
    val path: String,
    val details: String,
    val streamingSummary: String,
)

@Composable
fun FilePickerScreen(navigateBack: () -> Unit) {
    var pickedFiles by remember { mutableStateOf<List<PickedFileInfo>>(emptyList()) }
    var pickError by remember { mutableStateOf<String?>(null) }
    var isMultiple by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("Pick a file") }
    // Kept as its URI so it survives configuration changes and process death.
    var initialDirectoryUri by rememberSaveable { mutableStateOf<String?>(null) }
    val initialDirectory = remember(initialDirectoryUri) { initialDirectoryUri?.let { KmpFile.fromUriString(it) } }
    var selectedTypeIndex by remember { mutableStateOf(0) }

    val selectedFileType = fileTypeOptions[selectedTypeIndex].type

    val scope = rememberCoroutineScope()
    var streamingJob: Job? by remember { mutableStateOf(null) }

    // Reported before onResult, which still receives the files that could be read.
    val onPickError: (FilePickerException) -> Unit = { error -> pickError = error.message }

    val onFilesPicked: (List<KmpFile>) -> Unit = { files ->
        pickedFiles = files.map { file -> file.toPickedFileInfo(STREAMING_IN_PROGRESS) }
        streamingJob?.cancel()
        streamingJob = scope.launch {
            pickedFiles = pickedFiles.map { info ->
                info.copy(streamingSummary = info.file.describeStreaming())
            }
        }
    }

    val onDirectoryPicked: (List<KmpFile>) -> Unit = { files ->
        streamingJob?.cancel()
        pickedFiles = files.map { file -> file.toPickedFileInfo(DIRECTORY_NOT_STREAMED) }
    }

    // A picked folder is handed over as is; each platform opens the dialog in it in its own way.
    val settings = rememberFilePickerSettings(
        initialDirectory = initialDirectory,
        title = dialogTitle,
        imageRepresentationMode = ImageRepresentationMode.Current,
    )

    val singlePickerLauncher = rememberFilePickerLauncher(
        type = selectedFileType,
        selectionMode = FilePickerSelectionMode.Single,
        settings = settings,
        onError = onPickError,
        onResult = onFilesPicked,
    )

    val multiplePickerLauncher = rememberFilePickerLauncher(
        type = selectedFileType,
        selectionMode = FilePickerSelectionMode.Multiple,
        settings = settings,
        onError = onPickError,
        onResult = onFilesPicked,
    )

    val directoryPickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Folder,
        selectionMode = FilePickerSelectionMode.Single,
        settings = FilePickerSettings(title = "Pick a directory"),
        onError = onPickError,
        onResult = onDirectoryPicked,
    )

    val initialDirectoryPickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Folder,
        selectionMode = FilePickerSelectionMode.Single,
        settings = FilePickerSettings(title = "Choose initial directory"),
        onResult = { files ->
            val dir = files.firstOrNull()
            if (dir != null) {
                initialDirectoryUri = dir.toUriString()
            }
        },
    )

    SampleScreenScaffold(
        title = "File Picker",
        navigateBack = navigateBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = "File selection using platform-native file pickers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dialog title field
            OutlinedTextField(
                value = dialogTitle,
                onValueChange = { dialogTitle = it },
                label = { Text("Dialog title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Initial directory section
            Text(
                text = "Initial directory",
                style = MaterialTheme.typography.labelLarge,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = initialDirectory?.let { it.getPath() ?: it.toUriString() } ?: "System default",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row {
                TextButton(
                    onClick = { initialDirectoryPickerLauncher.launch() },
                    enabled = currentPlatform != Platform.Web,
                ) {
                    Text("Change")
                }

                if (initialDirectory != null) {
                    TextButton(onClick = { initialDirectoryUri = null }) {
                        Text("Reset")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File type selector
            Text(
                text = "File type",
                style = MaterialTheme.typography.labelLarge,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                fileTypeOptions.forEachIndexed { index, option ->
                    FilterChip(
                        selected = selectedTypeIndex == index,
                        onClick = { selectedTypeIndex = index },
                        label = { Text(option.label) },
                    )
                    if (index < fileTypeOptions.lastIndex) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Single/multiple toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Select multiple files?",
                    modifier = Modifier.weight(1f),
                )
                AdaptiveSwitch(
                    checked = isMultiple,
                    onCheckedChange = { isMultiple = it },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            AdaptiveButton(
                onClick = {
                    pickError = null
                    if (isMultiple) {
                        multiplePickerLauncher.launch()
                    } else {
                        singlePickerLauncher.launch()
                    }
                },
            ) {
                Text("Pick Files")
            }

            Spacer(modifier = Modifier.height(8.dp))

            AdaptiveButton(
                onClick = {
                    pickError = null
                    directoryPickerLauncher.launch()
                },
                enabled = currentPlatform != Platform.Web,
            ) {
                Text(
                    text = "Pick Directory" +
                        if (currentPlatform == Platform.Web) " (Not supported on Web)" else "",
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Results section
            Text(
                text = if (pickedFiles.isEmpty()) {
                    "No files selected"
                } else {
                    "Files picked (${pickedFiles.size}):"
                },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            pickError?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            pickedFiles.forEach { fileInfo ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = fileInfo.name,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = fileInfo.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = fileInfo.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = fileInfo.streamingSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

/**
 * Reads the whole file through the kotlinx-io [source] extension in fixed-size chunks, so the
 * file never has to fit in memory, and describes the outcome for the results list.
 */
private suspend fun KmpFile.describeStreaming(): String = withContext(Dispatchers.Default) {
    try {
        val bytes = source().use { source -> source.countBytes() }
        "$bytes bytes read in ${STREAM_CHUNK_SIZE / 1024} KB chunks with kotlinx-io"
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        "Not streamable: ${e.message}"
    }
}

private fun KmpFile.toPickedFileInfo(streamingSummary: String): PickedFileInfo =
    PickedFileInfo(
        file = this,
        name = getName().orEmpty(),
        // Picked files on Android are content URIs, which have no file system path.
        path = getPath() ?: toUriString() ?: "No path",
        details = describeMetadata(),
        streamingSummary = streamingSummary,
    )

private fun KmpFile.describeMetadata(): String {
    val size = getSize()?.let { "$it bytes" } ?: "Unknown size"
    val mimeType = getMimeType() ?: "unknown type"
    return "$size · $mimeType"
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
