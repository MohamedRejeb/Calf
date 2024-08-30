package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.DialogProperties
import com.mohamedrejeb.calf.core.InternalCalfApi

@OptIn(InternalCalfApi::class)
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
    val currentUIViewController = LocalUIViewController.current

    val alertDialogManager = remember(currentUIViewController) {
        AlertDialogManager(
            parentUIViewController = currentUIViewController,
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

    DisposableEffect(Unit) {
        alertDialogManager.showAlertDialog()

        onDispose {
            alertDialogManager.dismiss()
        }
    }
}