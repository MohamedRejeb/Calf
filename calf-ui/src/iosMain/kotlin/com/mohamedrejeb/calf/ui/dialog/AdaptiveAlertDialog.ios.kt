package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
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
    val alertDialogManager = remember {
        AlertDialogManager(
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            confirmText = confirmText,
            dismissText = dismissText,
            title = title,
            text = text,
            properties = properties,
        )
    }

    LaunchedEffect(onConfirm, onDismiss, confirmText, dismissText, title, text, properties) {
        alertDialogManager.onConfirm = onConfirm
        alertDialogManager.onDismiss = onDismiss
        alertDialogManager.confirmText = confirmText
        alertDialogManager.dismissText = dismissText
        alertDialogManager.title = title
        alertDialogManager.text = text
        alertDialogManager.properties = properties
    }

    LaunchedEffect(Unit) {
        alertDialogManager.showAlertDialog()
    }

    DisposableEffect(Unit) {
        onDispose {
            alertDialogManager.dismiss()
        }
    }
}