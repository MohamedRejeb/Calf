package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.dialog.AdaptiveBasicAlertDialog
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosAction
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosSeverity
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosTextField
import com.mohamedrejeb.calf.ui.dialog.uikit.rememberAlertDialogIosProperties
import com.mohamedrejeb.calf.ui.uikit.IosKeyboardType

@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun AlertDialogScreen(
    navigateBack: () -> Unit
) {
    var showSimpleDialog by remember { mutableStateOf(false) }
    var showTextFieldDialog by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf("") }
    var showComplexDialog by remember { mutableStateOf(false) }
    var showActionSheetDialog by remember { mutableStateOf(false) }

    SampleScreenScaffold(
        title = "Adaptive Alert Dialog",
        navigateBack = navigateBack,
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Demonstrates native alert dialogs. On iOS, uses UIAlertController. On other platforms, uses Material3 AlertDialog.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Simple Alert Dialog",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AdaptiveButton(
                onClick = { showSimpleDialog = true },
            ) {
                Text("Show Alert Dialog")
            }

            if (currentPlatform.isIOS) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Dialog with Text Field",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Confirm button is disabled until text is entered",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                AdaptiveButton(
                    onClick = {
                        textFieldValue = ""
                        showTextFieldDialog = true
                    },
                ) {
                    Text("Show Text Field Dialog")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "iOS Complex Alert Dialog",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                AdaptiveButton(
                    onClick = { showComplexDialog = true },
                ) {
                    Text("Show Complex Dialog")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "iOS ActionSheet Dialog",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                AdaptiveButton(
                    onClick = { showActionSheetDialog = true },
                ) {
                    Text("Show ActionSheet")
                }
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

        val textFieldDialogIosProperties = rememberAlertDialogIosProperties(
            title = "Enter Name",
            text = "Please type a name to enable the confirm button.",
            style = AlertDialogIosStyle.Alert,
            actions = listOf(
                AlertDialogIosAction(
                    title = "Confirm",
                    style = AlertDialogIosActionStyle.Default,
                    enabled = textFieldValue.isNotEmpty(),
                    onClick = {
                        showTextFieldDialog = false
                    },
                ),
                AlertDialogIosAction(
                    title = "Cancel",
                    style = AlertDialogIosActionStyle.Cancel,
                    onClick = {
                        showTextFieldDialog = false
                    },
                ),
            ),
            textFields = listOf(
                AlertDialogIosTextField(
                    placeholder = "Name",
                    initialValue = "",
                    onValueChange = { textFieldValue = it },
                ),
            ),
        )

        if (showTextFieldDialog) {
            AdaptiveBasicAlertDialog(
                onDismissRequest = {
                    showTextFieldDialog = false
                },
                iosProperties = textFieldDialogIosProperties,
                materialContent = {
                    Button(
                        onClick = {
                            showTextFieldDialog = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
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
            AdaptiveBasicAlertDialog(
                onDismissRequest = {
                    showComplexDialog = false
                },
                iosProperties = complexDialogIosProperties,
                materialContent = {
                    Button(
                        onClick = {
                            showComplexDialog = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
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
            AdaptiveBasicAlertDialog(
                onDismissRequest = {
                    showActionSheetDialog = false
                },
                iosProperties = actionSheetDialogIosProperties,
                materialContent = {
                    Button(
                        onClick = {
                            showActionSheetDialog = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
            )
        }
    }
}