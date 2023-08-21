package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.window.DialogProperties

@Composable
actual fun AdaptiveAlertDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String,
    dismissText: String,
    title: String,
    text: String,
    properties: DialogProperties
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
            ) {
                Text(dismissText)
            }
        },
        title = { Text(title) },
        text = { Text(text) },
    )
}