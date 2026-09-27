package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.EmptyState
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

private val CheckIconSize = 20.dp
private val TrailingTextMaxWidth = 96.dp

@Composable
internal fun FilePickerAttachmentsSection(
    files: List<PickedFile>,
    multiple: Boolean,
    foldersSupported: Boolean,
    onPickFiles: () -> Unit,
    onPickFolder: () -> Unit,
) {
    val folderNote = if (foldersSupported) "" else " Browsers cannot pick a folder."
    DemoSection(
        title = "Attachments",
        footer = "Sizes come from streaming each file with KmpFile.source() in " +
            "${STREAM_CHUNK_SIZE / 1024} KB chunks, so a large file never sits in memory.$folderNote",
    ) {
        DemoListCard {
            if (files.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.AttachFile,
                    title = "No attachments",
                    message = "Pick files or a folder. They show up here with their size.",
                )
            } else {
                files.forEachIndexed { index, file ->
                    if (index > 0) DemoDivider(startIndent = DividerIndentWithIcon)
                    PickedFileRow(file)
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AdaptiveButton(onClick = onPickFiles, modifier = Modifier.weight(1f)) {
                Text(if (multiple) "Attach files" else "Attach a file")
            }
            AdaptiveButton(
                onClick = onPickFolder,
                enabled = foldersSupported,
                colors = ButtonDefaults.filledTonalButtonColors(),
                modifier = Modifier.weight(1f),
            ) {
                Text("Attach a folder")
            }
        }
    }
}

@Composable
private fun PickedFileRow(file: PickedFile) {
    val read = file.read
    DemoRow(
        title = file.name.ifEmpty { "Untitled" },
        subtitle = if (read is ReadResult.Failed) "Could not read: ${read.reason}" else file.path,
        leading = { FileKindTile(file.kind) },
        trailing = {
            Text(
                text = when (read) {
                    ReadResult.Reading -> "Reading…"
                    is ReadResult.Read -> formatFileSize(read.bytes)
                    is ReadResult.Failed -> "Unreadable"
                    ReadResult.Folder -> "Folder"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (read is ReadResult.Failed) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                // Keeps a long label from squeezing the file name.
                modifier = Modifier.widthIn(max = TrailingTextMaxWidth),
            )
        },
    )
}

@Composable
internal fun FilePickerTypeSection(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    DemoSection(
        title = "File type",
        footer = "Photos keep their original format on iOS, HEIC included, with " +
            "ImageRepresentationMode.Current.",
    ) {
        DemoListCard {
            FileTypeOptions.forEachIndexed { index, option ->
                if (index > 0) DemoDivider(startIndent = DividerIndentWithIcon)
                DemoRow(
                    title = option.label,
                    leading = { FileKindTile(option.kind) },
                    trailing = {
                        if (index == selectedIndex) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(CheckIconSize),
                            )
                        }
                    },
                    onClick = { onSelect(index) },
                )
            }
        }
    }
}

@Composable
internal fun FilePickerOptionsSection(
    multiple: Boolean,
    onMultipleChange: (Boolean) -> Unit,
    dialogTitle: String,
    onDialogTitleChange: (String) -> Unit,
    startFolder: String?,
    onPickStartFolder: (() -> Unit)?,
    onResetStartFolder: () -> Unit,
) {
    DemoSection(
        title = "Options",
        footer = "The title and start folder are hints: each platform's dialog uses the ones it supports.",
    ) {
        DemoListCard {
            DemoRow(
                title = "Select multiple",
                trailing = { AdaptiveSwitch(checked = multiple, onCheckedChange = onMultipleChange) },
            )
            DemoDivider()
            TextFieldRow(
                label = "Dialog title",
                value = dialogTitle,
                onValueChange = onDialogTitleChange,
                placeholder = "None",
            )
            DemoDivider()
            DemoRow(
                title = "Start folder",
                subtitle = when {
                    onPickStartFolder == null -> "Not available in a browser"
                    startFolder == null -> "System default"
                    else -> startFolder
                },
                trailing = if (startFolder != null) {
                    { TextButton(onClick = onResetStartFolder) { Text("Reset") } }
                } else {
                    null
                },
                onClick = onPickStartFolder,
            )
        }
    }
}
