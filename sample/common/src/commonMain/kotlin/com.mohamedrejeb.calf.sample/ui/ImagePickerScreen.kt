package com.mohamedrejeb.calf.sample.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.picker.toImageBitmap
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

@Composable
fun ImagePickerScreen(
    navigateBack: () -> Unit
) {
    var imageBitmaps by remember {
        mutableStateOf<List<ImageBitmap>>(emptyList())
    }
    var isMultiple by remember { mutableStateOf(false) }
    val singlePickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Single,
        onResult = { files ->
            imageBitmaps = files.mapNotNull {
                try {
                    it.readByteArray().toImageBitmap()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        },
    )
    val multiplePickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Multiple,
        onResult = { files ->
            imageBitmaps = files.mapNotNull {
                try {
                    it.readByteArray().toImageBitmap()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.ime)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(
            onClick = {
                navigateBack()
            },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Row(Modifier.padding(16.dp)) {
            Text("Select multiple images?", Modifier.weight(1f))
            AdaptiveSwitch(isMultiple, onCheckedChange = { checked ->
                isMultiple = checked
            })
        }

        Button(
            onClick = {
                if (isMultiple) {
                    multiplePickerLauncher.launch()
                } else {
                    singlePickerLauncher.launch()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Pick image")
        }

        Text(
            text = "Image picked:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        imageBitmaps.forEach {
            Image(
                bitmap = it,
                contentDescription = "Image",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        }
    }
}