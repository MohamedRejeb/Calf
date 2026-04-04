package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.share.ShareContent
import com.mohamedrejeb.calf.share.ShareResult
import com.mohamedrejeb.calf.share.rememberShareLauncher

@OptIn(ExperimentalCalfApi::class)
@Composable
fun ShareScreen(
    navigateBack: () -> Unit,
) {
    var pickedFile by remember { mutableStateOf<KmpFile?>(null) }
    var text by remember { mutableStateOf("Hello from Calf!") }
    var url by remember { mutableStateOf("https://github.com/MohamedRejeb/Calf") }
    var lastResult by remember { mutableStateOf<ShareResult?>(null) }

    val shareLauncher = rememberShareLauncher { result ->
        lastResult = result
    }

    val filePickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.All,
        selectionMode = FilePickerSelectionMode.Single,
        onResult = { files ->
            pickedFile = files.firstOrNull()
        },
    )

    SampleScreenScaffold(
        title = "Content Sharing",
        navigateBack = navigateBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Share text, URLs, and files using the platform's native share sheet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Share File
            Text(
                text = "Share File",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { filePickerLauncher.launch() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(pickedFile?.let { "Picked: ${it.getName()}" } ?: "Pick a file")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    pickedFile?.let { file ->
                        shareLauncher.launch(
                            ShareContent.File(
                                file = file,
                                mimeType = "*/*",
                            )
                        )
                    }
                },
                enabled = pickedFile != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Share File")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Share Text
            Text(
                text = "Share Text",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Text to share") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    shareLauncher.launch(ShareContent.Text(text = text))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Share Text")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Share URL
            Text(
                text = "Share URL",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL to share") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    shareLauncher.launch(ShareContent.Url(url = url))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Share URL")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Result
            lastResult?.let { result ->
                Text(
                    text = "Last result: ${
                        when (result) {
                            ShareResult.Success -> "Success"
                            ShareResult.Dismissed -> "Dismissed"
                            ShareResult.Unavailable -> "Unavailable"
                        }
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (result) {
                        ShareResult.Success -> MaterialTheme.colorScheme.primary
                        ShareResult.Dismissed -> MaterialTheme.colorScheme.onSurfaceVariant
                        ShareResult.Unavailable -> MaterialTheme.colorScheme.error
                    },
                )
            }
        }
    }
}
