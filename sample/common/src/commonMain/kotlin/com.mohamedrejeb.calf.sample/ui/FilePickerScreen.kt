package com.mohamedrejeb.calf.sample.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.name
import com.mohamedrejeb.calf.io.path
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.picker.toImageBitmap
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.ui.sheet.AdaptiveBottomSheet
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePickerScreen(
    navigateBack: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { navigateBack() }
                    ) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                },
                title = {
                    Text(
                        text = Screen.FilePicker.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    ) { paddingValues ->

        var imageBitmaps by remember {
            mutableStateOf<List<ImageBitmap>>(listOf())
        }
        var pickedFiles by remember {
            mutableStateOf<List<KmpFile>>(listOf())
        }
        var fileType by remember {
            mutableStateOf<FilePickerFileType>(FilePickerFileType.Image)
        }

        val pickerLauncher = rememberFilePickerLauncher(
            type = fileType,
            selectionMode = FilePickerSelectionMode.Multiple,
            onResult = { files ->
                pickedFiles = files

                if (fileType == FilePickerFileType.Image) {
                    files.map { file ->
                        try {
                            imageBitmaps += file.readByteArray().toImageBitmap()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            },
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
        ) {

            FileTypeChooserBottomSheet(
                selectedFileType = fileType,
                onSelect = {
                    fileType = it
                }
            )

            Button(
                onClick = {
                    pickerLauncher.launch()
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Pick Files")
            }

            Text(
                text = "Files picked:",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )

            if (pickedFiles.isEmpty())
                Text(
                    text = "No Files Chosen",
                    modifier = Modifier.padding(16.dp)
                )
            else
                pickedFiles.map { file ->
                    Text(
                        text = "Name: ${file.name} - SizeOfByteArray: ${file.readByteArray().size}",
                        softWrap = false,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    )
                    Text(
                        text = "Path: ${file.path}",
                        softWrap = true,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    )
                }


            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                imageBitmaps.map {
                    Image(
                        bitmap = it,
                        contentDescription = "Image",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .requiredWidth(200.dp)
                            .padding(16.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileTypeChooserBottomSheet(
    selectedFileType: FilePickerFileType,
    onSelect: (FilePickerFileType) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberAdaptiveSheetState()
    var openBottomSheet by remember { mutableStateOf(false) }

    Button(
        onClick = {
            openBottomSheet = true
        },
    ) {
        Text("File Type: $selectedFileType")
    }

    if (openBottomSheet) {
        AdaptiveBottomSheet(
            onDismissRequest = {
                openBottomSheet = false
            },
            adaptiveSheetState = sheetState,
        ) {
            LazyColumn {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.requiredWidth(24.dp))
                        Text("Choose File Type")
                        IconButton(
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        openBottomSheet = false
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Close, "close")
                        }
                    }

                }
                item {
                    Text(
                        text = FilePickerFileType.Image.toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(FilePickerFileType.Image)
                                openBottomSheet = false
                            }
                            .padding(16.dp)
                    )
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = FilePickerFileType.Document.toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(FilePickerFileType.Document)
                                openBottomSheet = false
                            }
                            .padding(16.dp)
                    )
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = FilePickerFileType.All.toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(FilePickerFileType.All)
                                openBottomSheet = false
                            }
                            .padding(16.dp)
                    )
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}