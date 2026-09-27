package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSettings
import com.mohamedrejeb.calf.picker.ImageRepresentationMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.picker.rememberFileSaverLauncher
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.StatusLabel
import com.mohamedrejeb.calf.sample.components.TextFieldRow
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import kotlinx.coroutines.launch

private const val USAGE = """
// file is null when the user cancels
val saver = rememberFileSaverLauncher { file ->
    saved = file != null
}

saver.launch(
    bytes = croppedPng,
    baseName = "profile_picture",
    extension = "png",
)
"""

private const val DEFAULT_FILE_NAME = "profile_picture"
private const val EXPORT_EXTENSION = "png"
private const val PROFILE_NAME = "Amira Haddad"
private const val PROFILE_DETAIL = "Product designer, Tunis"
private val AvatarSize = 132.dp
private val AvatarPlaceholderIconSize = 64.dp

/** What happened to the last export, shown under the export options. */
private sealed interface ExportStatus {
    data class Saved(val fileName: String?) : ExportStatus
    data object Cancelled : ExportStatus
    data class Failed(val reason: String) : ExportStatus
}

@OptIn(ExperimentalCalfApi::class)
@Composable
fun FileSaverScreen(navigateBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var photo by remember { mutableStateOf<KmpFile?>(null) }
    var fileName by rememberSaveable { mutableStateOf(DEFAULT_FILE_NAME) }
    var status by remember { mutableStateOf<ExportStatus?>(null) }

    val pickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        settings = FilePickerSettings(
            title = "Choose a profile photo",
            imageRepresentationMode = ImageRepresentationMode.Compatible,
        ),
        onResult = { files ->
            files.firstOrNull()?.let { photo = it }
            status = null
        },
    )
    val saverLauncher = rememberFileSaverLauncher(
        onResult = { file ->
            status = if (file != null) ExportStatus.Saved(file.getName()) else ExportStatus.Cancelled
        },
    )

    SampleScreenScaffold(title = Screen.ProfilePicture.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.ProfilePicture)
            ProfileSection(photo = photo, onChoosePhoto = { pickerLauncher.launch() })
            ExportSection(
                fileName = fileName,
                onFileNameChange = { fileName = it },
                status = status,
                canSave = photo != null,
                onSave = {
                    val source = photo ?: return@ExportSection
                    scope.launch {
                        circleCropPng(source)
                            .onSuccess { png ->
                                saverLauncher.launch(
                                    bytes = png,
                                    baseName = fileName.ifBlank { DEFAULT_FILE_NAME },
                                    extension = EXPORT_EXTENSION,
                                )
                            }
                            .onFailure { error ->
                                status = ExportStatus.Failed(error.message ?: "The photo could not be read")
                            }
                    }
                },
            )
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun ProfileSection(
    photo: KmpFile?,
    onChoosePhoto: () -> Unit,
) {
    DemoSection(title = "Profile photo") {
        DemoCard(
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Avatar(photo)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = PROFILE_NAME,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = PROFILE_DETAIL,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AdaptiveButton(
                onClick = onChoosePhoto,
                colors = ButtonDefaults.filledTonalButtonColors(),
            ) {
                Text(if (photo == null) "Choose photo" else "Change photo")
            }
        }
    }
}

/** The photo in the circle it will be saved as, or a placeholder before one is chosen. */
@Composable
private fun Avatar(photo: KmpFile?) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(AvatarSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(AvatarPlaceholderIconSize),
            )
        }
    }
}

@Composable
private fun ExportSection(
    fileName: String,
    onFileNameChange: (String) -> Unit,
    status: ExportStatus?,
    canSave: Boolean,
    onSave: () -> Unit,
) {
    DemoSection(
        title = "Export",
        footer = "The photo is cropped to a circle and saved as a PNG with a transparent background.",
    ) {
        DemoListCard {
            TextFieldRow(
                label = "File name",
                value = fileName,
                onValueChange = onFileNameChange,
                placeholder = DEFAULT_FILE_NAME,
                suffix = ".$EXPORT_EXTENSION",
            )
            if (status != null) {
                DemoDivider()
                ExportStatusRow(status)
            }
        }
        AdaptiveButton(
            onClick = onSave,
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save photo")
        }
    }
}

@Composable
private fun ExportStatusRow(status: ExportStatus) {
    val (label, color) = when (status) {
        is ExportStatus.Saved -> "Saved" to TileTeal
        ExportStatus.Cancelled -> "Cancelled" to MaterialTheme.colorScheme.outline
        is ExportStatus.Failed -> "Failed" to MaterialTheme.colorScheme.error
    }
    DemoRow(
        title = "Last export",
        subtitle = when (status) {
            is ExportStatus.Saved -> status.fileName
            ExportStatus.Cancelled -> null
            is ExportStatus.Failed -> status.reason
        },
        trailing = { StatusLabel(label = label, color = color, modifier = Modifier.padding(start = Spacing.s)) },
    )
}
