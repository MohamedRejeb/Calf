package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
    AlertDialog(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun AdaptiveBasicAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    iosProperties: AlertDialogIosProperties,
    properties: DialogProperties,
    materialContent: @Composable () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = properties,
        content = materialContent,
    )
}
