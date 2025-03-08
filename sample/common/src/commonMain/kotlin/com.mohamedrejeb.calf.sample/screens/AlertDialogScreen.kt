package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosAction
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosSeverity
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosTextField
import com.mohamedrejeb.calf.ui.dialog.uikit.rememberAlertDialogIosProperties
import com.mohamedrejeb.calf.ui.uikit.IosKeyboardType

@Composable
fun AlertDialogScreen(
    navigateBack: () -> Unit
) {
    var showSimpleDialog by remember { mutableStateOf(false) }
    var showComplexDialog by remember { mutableStateOf(false) }
    var showActionSheetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        IconButton(
            onClick = {
                navigateBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Button(
                onClick = {
                    showSimpleDialog = true
                },
            ) {
                Text("Show Alert Dialog")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    showComplexDialog = true
                },
            ) {
                Text("Show iOS Complex Alert Dialog")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    showActionSheetDialog = true
                },
            ) {
                Text("Show iOS ActionSheet Dialog")
            }
        }


        if (showSimpleDialog) {
            AdaptiveAlertDialog(
                onConfirm = {
                    showSimpleDialog = false
                },
                onDismiss = {
                    showSimpleDialog = false
                },
                confirmText = "Ok",
                dismissText = "Cancel",
                title = "Alert Dialog",
                text = "This is a native alert dialog from Calf",
            )
        }

        val complexDialogIosProperties = rememberAlertDialogIosProperties(
            title = "Alert Dialog",
            text = "This is a native alert dialog from Calf",
            animated = true,
            style = AlertDialogIosStyle.Alert,
            severity = AlertDialogIosSeverity.Critical,
            actions = listOf(
                AlertDialogIosAction(
                    title = "Ok",
                    style = AlertDialogIosActionStyle.Default,
                    onClick = {
                        showComplexDialog = false
                    }
                ),
                AlertDialogIosAction(
                    title = "Cancel",
                    style = AlertDialogIosActionStyle.Cancel,
                    enabled = false,
                    onClick = {
                        showComplexDialog = false
                    }
                ),
                AlertDialogIosAction(
                    title = "Remove",
                    style = AlertDialogIosActionStyle.Destructive,
                    onClick = {
                        showComplexDialog = false
                    }
                ),
            ),
            textFields = listOf(
                AlertDialogIosTextField(
                    placeholder = "Enter your name",
                    initialValue = "John Doe",
                    keyboardType = IosKeyboardType.EmailAddress,
                    isSecure = false,
                    onValueChange = {
                        println("Name: $it")
                    }
                ),
                AlertDialogIosTextField(
                    placeholder = "Enter your password",
                    keyboardType = IosKeyboardType.NumberPad,
                    initialValue = "",
                    isSecure = true,
                    onValueChange = {
                        println("Password: $it")
                    }
                ),
            )
        )

        if (showComplexDialog) {
            AdaptiveAlertDialog(
                onDismissRequest = {
                    showComplexDialog = false
                },
                materialConfirmButton = {
                    Button(
                        onClick = {
                            showComplexDialog = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                materialDismissButton = {
                    Button(
                        onClick = {
                            showComplexDialog = false
                        }
                    ) {
                        Text("Dismiss")
                    }
                },
                iosProperties = complexDialogIosProperties,
            )
        }

        val actionSheetDialogIosProperties = rememberAlertDialogIosProperties(
            title = "Alert Dialog",
            text = "This is a native alert dialog from Calf",
            animated = true,
            style = AlertDialogIosStyle.ActionSheet,
            severity = AlertDialogIosSeverity.Default,
            actions = listOf(
                AlertDialogIosAction(
                    title = "Ok",
                    style = AlertDialogIosActionStyle.Default,
                    onClick = {
                        showActionSheetDialog = false
                    }
                ),
                AlertDialogIosAction(
                    title = "Cancel",
                    style = AlertDialogIosActionStyle.Cancel,
                    onClick = {
                        showActionSheetDialog = false
                    }
                ),
                AlertDialogIosAction(
                    title = "Remove",
                    style = AlertDialogIosActionStyle.Destructive,
                    enabled = false,
                    onClick = {
                        showActionSheetDialog = false
                    }
                ),
            ),
        )

        if (showActionSheetDialog) {
            AdaptiveAlertDialog(
                onDismissRequest = {
                    showActionSheetDialog = false
                },
                materialConfirmButton = {
                    Button(
                        onClick = {
                            showActionSheetDialog = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                materialDismissButton = {
                    Button(
                        onClick = {
                            showActionSheetDialog = false
                        }
                    ) {
                        Text("Dismiss")
                    }
                },
                iosProperties = actionSheetDialogIosProperties,
            )
        }
    }
}