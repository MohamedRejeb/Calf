package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
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
            type = FilePickerFileType.Extension(listOf("pdf")),
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
                text = "Select multiple files?",
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
            Text("Pick Files")
        }

        Text(
            text = "Files picked:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )

        Spacer(Modifier.height(8.dp))

        fileNames.forEach {
            Text(
                text = "- $it",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
    }
}
