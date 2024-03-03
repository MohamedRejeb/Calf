package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.picker.toImageBitmap
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import kotlinx.coroutines.launch

@Composable
fun ImagePickerScreen(navigateBack: () -> Unit) {
    val context = LocalPlatformContext.current
    val scope = rememberCoroutineScope()

    var imageBitmaps by remember {
        mutableStateOf<List<ImageBitmap>>(emptyList())
    }
    var isMultiple by remember { mutableStateOf(false) }

    val singlePickerLauncher =
        rememberFilePickerLauncher(
            type = FilePickerFileType.Image,
            selectionMode = FilePickerSelectionMode.Single,
            onResult = { files ->
                scope.launch {
                    imageBitmaps =
                        files.mapNotNull {
                            try {
                                it.readByteArray(context).toImageBitmap()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                }
            },
        )

    val multiplePickerLauncher =
        rememberFilePickerLauncher(
            type = FilePickerFileType.Image,
            selectionMode = FilePickerSelectionMode.Multiple,
            onResult = { files ->
                scope.launch {
                    imageBitmaps =
                        files.mapNotNull {
                            try {
                                it.readByteArray(context).toImageBitmap()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                }
            },
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars)
                .windowInsetsPadding(WindowInsets.ime)
                .verticalScroll(rememberScrollState()),
    ) {
        IconButton(
            onClick = {
                navigateBack()
            },
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            Icon(
                Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Row(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Select multiple images?",
                modifier = Modifier.weight(1f),
            )

            AdaptiveSwitch(
                checked = isMultiple,
                onCheckedChange = { checked ->
                    isMultiple = checked
                },
            )
        }

        Button(
            onClick = {
                if (isMultiple) {
                    multiplePickerLauncher.launch()
                } else {
                    singlePickerLauncher.launch()
                }
            },
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Pick image",
            )
        }

        Text(
            text = "Image picked:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )

        imageBitmaps.forEach {
            Image(
                bitmap = it,
                contentDescription = "Image",
                contentScale = ContentScale.FillWidth,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(MaterialTheme.shapes.medium),
            )
        }
    }
}
