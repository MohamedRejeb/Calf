package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.EmptyState
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.share.ShareContent
import com.mohamedrejeb.calf.share.ShareResult
import com.mohamedrejeb.calf.share.rememberShareLauncher
import com.mohamedrejeb.calf.ui.button.AdaptiveButton

private const val USAGE = """
val share = rememberShareLauncher { result ->
    shared = result == ShareResult.Success
}

share.launch(ShareContent.Url(url = articleUrl))
share.launch(ShareContent.Text(text = note, subject = "Groceries"))
share.launch(ShareContent.File(file = pdf, mimeType = "application/pdf"))
"""

private const val ARTICLE_URL = "https://kotlinlang.org/docs/multiplatform.html"
private const val ARTICLE_SUBJECT = "Kotlin Multiplatform overview"
private const val NOTE_SUBJECT = "Saturday groceries"
private const val NOTE_TEXT =
    "Oat milk, two lemons, fresh basil, and a sourdough loaf from the bakery on the corner."
private const val NOTE_MIN_LINES = 3

@OptIn(ExperimentalCalfApi::class)
@Composable
fun ShareScreen(navigateBack: () -> Unit) {
    var lastResult by remember { mutableStateOf<ShareResult?>(null) }
    val shareLauncher = rememberShareLauncher { result -> lastResult = result }

    SampleScreenScaffold(title = Screen.Share.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.Share)
            LinkSection(onShare = shareLauncher::launch)
            NoteSection(onShare = shareLauncher::launch)
            FilesSection(onShare = shareLauncher::launch)
            ResultSection(lastResult)
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun LinkSection(onShare: (ShareContent) -> Unit) {
    var url by rememberSaveable { mutableStateOf(ARTICLE_URL) }
    var subject by rememberSaveable { mutableStateOf(ARTICLE_SUBJECT) }
    DemoSection(title = "Link", footer = "On iOS the share sheet can show a preview of the page.") {
        DemoListCard {
            TextAreaRow(
                value = url,
                onValueChange = { url = it },
                placeholder = "https://",
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )
            DemoDivider()
            TextFieldRow(label = "Subject", value = subject, onValueChange = { subject = it }, placeholder = "None")
        }
        ShareButton(label = "Share link", enabled = url.isNotBlank()) {
            onShare(ShareContent.Url(url = url.trim(), subject = subject.ifBlank { null }))
        }
    }
}

@Composable
private fun NoteSection(onShare: (ShareContent) -> Unit) {
    var note by rememberSaveable { mutableStateOf(NOTE_TEXT) }
    var subject by rememberSaveable { mutableStateOf(NOTE_SUBJECT) }
    DemoSection(title = "Note", footer = "Email apps use the subject as the subject line.") {
        DemoListCard {
            TextFieldRow(label = "Subject", value = subject, onValueChange = { subject = it }, placeholder = "None")
            DemoDivider()
            TextAreaRow(
                value = note,
                onValueChange = { note = it },
                placeholder = "Write a note",
                minLines = NOTE_MIN_LINES,
            )
        }
        ShareButton(label = "Share note", enabled = note.isNotBlank()) {
            onShare(ShareContent.Text(text = note, subject = subject.ifBlank { null }))
        }
    }
}

@Composable
private fun FilesSection(onShare: (ShareContent) -> Unit) {
    var files by remember { mutableStateOf<List<KmpFile>>(emptyList()) }
    val pickerLauncher = rememberFilePickerLauncher(
        selectionMode = FilePickerSelectionMode.Multiple,
        onResult = { picked -> if (picked.isNotEmpty()) files = picked },
    )
    val kinds = files.map { FileKind.of(it.getName()) }

    DemoSection(
        title = "Files",
        footer = "One file goes out as ShareContent.File, several as ShareContent.Files, " +
            "with a MIME type guessed from the extensions.",
    ) {
        DemoListCard {
            if (files.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.FolderOpen,
                    title = "No files to share",
                    message = "Choose a few files, a photo or a PDF for example.",
                )
            } else {
                files.forEachIndexed { index, file ->
                    if (index > 0) DemoDivider(startIndent = DividerIndentWithIcon)
                    DemoRow(
                        title = file.getName() ?: "Untitled",
                        subtitle = kinds[index].mimeType,
                        leading = { FileKindTile(kinds[index]) },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m), modifier = Modifier.fillMaxWidth()) {
            AdaptiveButton(
                onClick = { pickerLauncher.launch() },
                colors = ButtonDefaults.filledTonalButtonColors(),
                modifier = Modifier.weight(1f),
            ) {
                Text(if (files.isEmpty()) "Choose files" else "Change files")
            }
            AdaptiveButton(
                onClick = { onShare(files.toShareContent(kinds)) },
                enabled = files.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Text(if (files.size > 1) "Share ${files.size} files" else "Share file")
            }
        }
    }
}

@Composable
private fun ResultSection(result: ShareResult?) {
    DemoSection(
        title = "Last result",
        footer = "Desktop copies text to the clipboard and opens links in the browser. " +
            "The web uses navigator.share() and falls back to the clipboard.",
    ) {
        DemoCard {
            ValueRow(
                label = "ShareResult",
                value = when (result) {
                    null -> "Nothing shared yet"
                    ShareResult.Success -> "Success"
                    ShareResult.Dismissed -> "Dismissed"
                    ShareResult.Unavailable -> "Unavailable"
                },
                valueColor = when (result) {
                    null, ShareResult.Dismissed -> MaterialTheme.colorScheme.onSurfaceVariant
                    ShareResult.Success -> MaterialTheme.colorScheme.primary
                    ShareResult.Unavailable -> MaterialTheme.colorScheme.error
                },
            )
        }
    }
}

@Composable
private fun ShareButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    AdaptiveButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text(label)
    }
}

private fun List<KmpFile>.toShareContent(kinds: List<FileKind>): ShareContent =
    singleOrNull()?.let { ShareContent.File(file = it, mimeType = kinds.first().mimeType) }
        ?: ShareContent.Files(files = this, mimeType = sharedMimeType(kinds))
