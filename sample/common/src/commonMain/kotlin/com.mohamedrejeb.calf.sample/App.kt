package com.mohamedrejeb.calf.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.ui.theme.CalfTheme
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.datepicker.UIKitDisplayMode
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import com.mohamedrejeb.calf.ui.progress.CupertinoActivityIndicator
import com.mohamedrejeb.calf.ui.sheet.AdaptiveBottomSheet
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePicker
import com.mohamedrejeb.calf.ui.timepicker.rememberAdaptiveTimePickerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() = CalfTheme {
    var openDialog by remember { mutableStateOf(false) }
    var openBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberAdaptiveSheetState()
    val datePickerState = rememberAdaptiveDatePickerState(
        initialMaterialDisplayMode = DisplayMode.Picker,
        initialUIKitDisplayMode = UIKitDisplayMode.Picker,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            item {
                ListItem(
                    headlineContent = { Text("Adaptive Alert Dialog") },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Adaptive Alert Dialog",
                        )
                    },
                    modifier = Modifier.clickable {
                        openDialog = !openDialog
                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Adaptive Bottom Sheet") },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Pages,
                            contentDescription = "Adaptive Bottom Sheet",
                        )
                    },
                    modifier = Modifier.clickable {
                        openBottomSheet = !openBottomSheet
                    }
                )
                Divider()
                AdaptiveCircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                )
                Divider()
                CupertinoActivityIndicator(
                    modifier = Modifier.size(100.dp),
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Adaptive Date Picker") },
                    leadingContent = {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = "Adaptive Date Picker",
                        )
                    },
                    modifier = Modifier.clickable {

                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Adaptive Time Picker") },
                    leadingContent = {
                        Icon(
                            Icons.Filled.PunchClock,
                            contentDescription = "Adaptive Time Picker",
                        )
                    },
                    modifier = Modifier.clickable {

                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Adaptive File Picker") },
                    leadingContent = {
                        Icon(
                            Icons.Filled.AttachFile,
                            contentDescription = "Adaptive File Picker",
                        )
                    },
                    modifier = Modifier.clickable {

                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Adaptive Notification") },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Adaptive Notification",
                        )
                    },
                    modifier = Modifier.clickable {

                    }
                )

                Divider()
                ListItem(
                    headlineContent = { Text("Adaptive Permission") },
                    leadingContent = {
                        Icon(
                            Icons.Filled.PermIdentity,
                            contentDescription = "Adaptive Permission",
                        )
                    },
                    modifier = Modifier.clickable {

                    }
                )
                Divider()
                AdaptiveDatePicker(
                    state = datePickerState,
                    modifier = Modifier
                )
                LaunchedEffect(datePickerState.selectedDateMillis) {
                    println("selectedDateMillis: ${datePickerState.selectedDateMillis}")
                }
                Divider()
                val timePickerState = rememberAdaptiveTimePickerState()
                AdaptiveTimePicker(
                    state = timePickerState,
                    modifier = Modifier,
                )
                LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                    println("hour: ${timePickerState.hour}")
                    println("minute: ${timePickerState.minute}")
                }
                Divider()
//                AdaptiveCircularProgressIndicator(
//                    color = Color.Red,
//                    strokeWidth = 12.dp,
//                    trackColor = Color.LightGray,
//                )
//                Divider()
//                AdaptiveCircularProgressIndicator(
//                    modifier = Modifier.size(100.dp),
//                )
//                Divider()
            }
        }

        if (openDialog) {
            AdaptiveAlertDialog(
                onConfirm = {
                    openDialog = false
                },
                onDismiss = {
                    openDialog = false
                },
                confirmText = "Ok",
                dismissText = "Cancel",
                title = "Alert Dialog",
                text = "This is a native alert dialog from Calf",
            )
        }

        if (openBottomSheet) {
            AdaptiveBottomSheet(
                onDismissRequest = {
                    openBottomSheet = false
                },
                adaptiveSheetState = sheetState,
            ) {
                Column {
                    Text("Bottom Sheet")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    openBottomSheet = false
                                }
                            }
                        }
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}