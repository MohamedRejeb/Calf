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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.FilePickerSettings
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.picker.rememberFilePickerSettings
import com.mohamedrejeb.calf.sample.Platform
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

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

private data class PickedFileInfo(
    val name: String,
    val path: String,
)

@Composable
fun FilePickerScreen(navigateBack: () -> Unit) {
    var pickedFiles by remember { mutableStateOf<List<PickedFileInfo>>(emptyList()) }
    var isMultiple by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("Pick a file") }
    var initialDirectory by remember { mutableStateOf<String?>(null) }
    var selectedTypeIndex by remember { mutableStateOf(0) }

    val selectedFileType = fileTypeOptions[selectedTypeIndex].type

    val settings = rememberFilePickerSettings(
        title = dialogTitle,
        initialDirectory = initialDirectory,
    )

    val singlePickerLauncher = rememberFilePickerLauncher(
        type = selectedFileType,
        selectionMode = FilePickerSelectionMode.Single,
        settings = settings,
        onResult = { files ->
            pickedFiles = files.map { file ->
                PickedFileInfo(
                    name = file.getName().orEmpty(),
                    path = file.getPath() ?: "Unknown path",
                )
            }
        },
    )

    val multiplePickerLauncher = rememberFilePickerLauncher(
        type = selectedFileType,
        selectionMode = FilePickerSelectionMode.Multiple,
        settings = settings,
        onResult = { files ->
            pickedFiles = files.map { file ->
                PickedFileInfo(
                    name = file.getName().orEmpty(),
                    path = file.getPath() ?: "Unknown path",
                )
            }
        },
    )

    val directoryPickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Folder,
        selectionMode = FilePickerSelectionMode.Single,
        settings = FilePickerSettings(title = "Pick a directory"),
        onResult = { files ->
            pickedFiles = files.map { file ->
                PickedFileInfo(
                    name = file.getName().orEmpty(),
                    path = file.getPath() ?: "Unknown path",
                )
            }
        },
    )

    val initialDirectoryPickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Folder,
        selectionMode = FilePickerSelectionMode.Single,
        settings = FilePickerSettings(title = "Choose initial directory"),
        onResult = { files ->
            val dir = files.firstOrNull()
            if (dir != null) {
                initialDirectory = dir.getPath()
            }
        },
    )

    SampleScreenScaffold(
        title = "Adaptive File Picker",
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
                text = initialDirectory ?: "System default",
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
                    TextButton(onClick = { initialDirectory = null }) {
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
                onClick = { directoryPickerLauncher.launch() },
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
                }
            }
        }
    }
}
