package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.sample.Platform
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

@Composable
fun FilePickerScreen(navigateBack: () -> Unit) {
    val context = LocalPlatformContext.current

    var fileNames by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var isMultiple by remember { mutableStateOf(false) }

    val singlePickerLauncher =
        rememberFilePickerLauncher(
            type = FilePickerFileType.All,
            selectionMode = FilePickerSelectionMode.Single,
            onResult = { files ->
                fileNames = files.map { it.getName(context).orEmpty() }
            },
        )

    val multiplePickerLauncher =
        rememberFilePickerLauncher(
            type = FilePickerFileType.All,
            selectionMode = FilePickerSelectionMode.Multiple,
            onResult = { files ->
                fileNames = files.map { it.getName(context).orEmpty() }
            },
        )

    val directoryPickerLauncher =
        rememberFilePickerLauncher(
            type = FilePickerFileType.Folder,
            selectionMode = FilePickerSelectionMode.Single,
            onResult = { files ->
                fileNames = files.map { it.getName(context).orEmpty() }
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
                    text = "Pick Directory" + if (currentPlatform == Platform.Web) " (Not supported on Web)" else "",
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Files picked:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (fileNames.isEmpty()) {
                Text(
                    text = "No files selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                fileNames.forEach {
                    Text(
                        text = "• $it",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}
