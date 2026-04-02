package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.FilePickerSettings
import com.mohamedrejeb.calf.picker.ImageRepresentationMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.picker.rememberFileSaverLauncher
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalCalfApi::class)
@Composable
fun FileSaverScreen(navigateBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    var selectedFile by remember { mutableStateOf<KmpFile?>(null) }
    val fileNameState = remember { TextFieldState("profile_picture") }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    val pickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Single,
        settings = FilePickerSettings(
            title = "Choose a profile picture",
            imageRepresentationMode = ImageRepresentationMode.Compatible,
        ),
        onResult = { files ->
            selectedFile = files.firstOrNull()
            saveMessage = null
        },
    )

    val saverLauncher = rememberFileSaverLauncher(
        onResult = { file ->
            saveMessage = if (file != null) "Saved successfully" else "Save cancelled"
        },
    )

    SampleScreenScaffold(
        title = "File Saver",
        navigateBack = navigateBack,
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                text = "Pick a profile picture, preview it in a circle crop, then export it using the file saver API.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Circle avatar preview
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (selectedFile != null) {
                    AsyncImage(
                        model = selectedFile,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Placeholder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(80.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                state = fileNameState,
                label = { Text("File name") },
                lineLimits = androidx.compose.foundation.text.input.TextFieldLineLimits.SingleLine,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            AdaptiveButton(
                onClick = { pickerLauncher.launch() },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedFile == null) "Choose Photo" else "Change Photo")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AdaptiveButton(
                onClick = {
                    scope.launch {
                        val file = selectedFile ?: return@launch
                        val bytes = file.readByteArray()
                        val baseName = fileNameState.text.toString().ifBlank { "profile_picture" }
                        saverLauncher.launch(
                            bytes = bytes,
                            baseName = baseName,
                            extension = "jpg",
                        )
                    }
                },
                enabled = selectedFile != null,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.SaveAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Photo")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = saveMessage ?: "Export status",
                style = MaterialTheme.typography.bodyMedium,
                color = if (saveMessage != null) Color(0xFF4CAF50) else Color.Transparent,
                modifier = Modifier.alpha(if (saveMessage != null) 1f else 0f),
            )
        }
    }
}
