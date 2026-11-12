package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.FilePickerSettings
import com.mohamedrejeb.calf.picker.ImageRepresentationMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.EmptyState
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

private const val USAGE = """
val launcher = rememberFilePickerLauncher(
    type = FilePickerFileType.Image,
    selectionMode = FilePickerSelectionMode.Multiple,
) { files -> photos = files }

// Needs KmpFileFetcher.Factory() in Coil
AsyncImage(model = photos.first(), contentDescription = null)
"""

private const val GRID_COLUMNS = 3
private const val SINGLE_PHOTO_ASPECT_RATIO = 4f / 3f

@Composable
fun ImagePickerScreen(navigateBack: () -> Unit) {
    var photos by remember { mutableStateOf<List<KmpFile>>(emptyList()) }
    var multiple by rememberSaveable { mutableStateOf(true) }

    // Compatible turns HEIC into JPEG on iOS, which Compose can decode; ignored elsewhere.
    val singleLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Single,
        settings = FilePickerSettings(
            title = "Choose a photo",
            imageRepresentationMode = ImageRepresentationMode.Compatible,
        ),
        onResult = { photos = it },
    )
    val multipleLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Multiple,
        settings = FilePickerSettings(
            title = "Choose photos",
            imageRepresentationMode = ImageRepresentationMode.Compatible,
        ),
        onResult = { photos = it },
    )

    SampleScreenScaffold(title = Screen.ImagePicker.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.ImagePicker)
            PhotosSection(
                photos = photos,
                multiple = multiple,
                onPick = { if (multiple) multipleLauncher.launch() else singleLauncher.launch() },
            )
            DemoSection(title = "Options") {
                DemoListCard {
                    DemoRow(
                        title = "Select multiple",
                        subtitle = "FilePickerSelectionMode.Multiple",
                        trailing = { AdaptiveSwitch(checked = multiple, onCheckedChange = { multiple = it }) },
                    )
                }
            }
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun PhotosSection(
    photos: List<KmpFile>,
    multiple: Boolean,
    onPick: () -> Unit,
) {
    DemoSection(
        title = "New post",
        footer = when (photos.size) {
            0 -> "On iOS, HEIC photos arrive as JPEG so Coil can show them."
            1 -> "1 photo, loaded by Coil straight from the KmpFile."
            else -> "${photos.size} photos, loaded by Coil straight from the KmpFile."
        },
    ) {
        if (photos.isEmpty()) {
            DemoCard {
                EmptyState(
                    icon = Icons.Outlined.PhotoLibrary,
                    title = "No photos yet",
                    message = "Choose from your library to preview them here.",
                )
            }
        } else {
            DemoCard(contentPadding = PaddingValues(Spacing.s)) {
                if (photos.size == 1) SinglePhoto(photos.first()) else PhotoGrid(photos)
            }
        }
        AdaptiveButton(onClick = onPick, modifier = Modifier.fillMaxWidth()) {
            Text(if (multiple) "Choose photos" else "Choose a photo")
        }
    }
}

@Composable
private fun SinglePhoto(photo: KmpFile) {
    Photo(
        photo = photo,
        modifier = Modifier.fillMaxWidth().aspectRatio(SINGLE_PHOTO_ASPECT_RATIO),
    )
}

@Composable
private fun PhotoGrid(photos: List<KmpFile>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        photos.chunked(GRID_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                row.forEach { photo ->
                    Photo(photo = photo, modifier = Modifier.weight(1f).aspectRatio(1f))
                }
                // Keeps the last, shorter row on the same grid as the others.
                repeat(GRID_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Photo(photo: KmpFile, modifier: Modifier = Modifier) {
    AsyncImage(
        model = photo,
        contentDescription = "Picked photo",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    )
}
