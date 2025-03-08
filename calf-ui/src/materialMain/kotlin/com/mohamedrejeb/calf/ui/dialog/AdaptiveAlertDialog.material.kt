package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosProperties
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosStyle

@Composable
actual fun AdaptiveAlertDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String,
    dismissText: String,
    title: String,
    text: String,
    materialConfirmButton: @Composable (() -> Unit)?,
    materialDismissButton: @Composable (() -> Unit)?,
    materialIcon: @Composable (() -> Unit)?,
    materialTitle: @Composable (() -> Unit)?,
    materialText: @Composable (() -> Unit)?,
    shape: Shape,
    containerColor: Color,
    iconContentColor: Color,
    titleContentColor: Color,
    textContentColor: Color,
    tonalElevation: Dp,
    iosDialogStyle: AlertDialogIosStyle,
    iosConfirmButtonStyle: AlertDialogIosActionStyle,
    iosDismissButtonStyle: AlertDialogIosActionStyle,
    properties: DialogProperties,
    modifier: Modifier,
) {
    AlertDialogImpl(
        onDismissRequest = onDismiss,
        confirmButton = materialConfirmButton ?: {
            Button(
                onClick = onConfirm,
            ) {
                Text(confirmText)
            }
        },
        dismissButton = materialDismissButton ?: {
            Button(
                onClick = onDismiss,
            ) {
                Text(dismissText)
            }
        },
        icon = materialIcon,
        title = materialTitle ?: {
            Text(title)
        },
        text = materialText ?: {
            Text(text)
        },
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
        modifier = modifier,
    )
}

@Composable
actual fun AdaptiveAlertDialog(
    onDismissRequest: () -> Unit,
    materialConfirmButton: @Composable () -> Unit,
    materialDismissButton: @Composable (() -> Unit)?,
    materialIcon: @Composable (() -> Unit)?,
    materialTitle: @Composable (() -> Unit)?,
    materialText: @Composable (() -> Unit)?,
    shape: Shape,
    containerColor: Color,
    iconContentColor: Color,
    titleContentColor: Color,
    textContentColor: Color,
    tonalElevation: Dp,
    iosProperties: AlertDialogIosProperties,
    properties: DialogProperties,
    modifier: Modifier,
) {
    AlertDialogImpl(
        onDismissRequest = onDismissRequest,
        confirmButton = materialConfirmButton,
        dismissButton = materialDismissButton,
        icon = materialIcon,
        title = materialTitle,
        text = materialText,
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
        modifier = modifier,
    )
}

@Composable
private fun AlertDialogImpl(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?,
    icon: @Composable (() -> Unit)?,
    title: @Composable (() -> Unit)?,
    text: @Composable (() -> Unit)?,
    shape: Shape,
    containerColor: Color,
    iconContentColor: Color,
    titleContentColor: Color,
    textContentColor: Color,
    tonalElevation: Dp,
    properties: DialogProperties,
    modifier: Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
    )
}