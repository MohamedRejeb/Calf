package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosAction
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosProperties
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosStyle

@OptIn(InternalCalfApi::class)
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
    iosConfirmButtonIsPreferred: Boolean,
    properties: DialogProperties,
    modifier: Modifier,
) {
    val currentUIViewController = LocalUIViewController.current

    fun createIosDialogProperties(): AlertDialogIosProperties {
        return AlertDialogIosProperties(
            title = title,
            text = text,
            style = iosDialogStyle,
            actions = listOf(
                AlertDialogIosAction(
                    title = confirmText,
                    style = iosConfirmButtonStyle,
                    onClick = onConfirm,
                    isPreferred = iosConfirmButtonIsPreferred,
                ),
                AlertDialogIosAction(
                    title = dismissText,
                    style = iosDismissButtonStyle,
                    onClick = onDismiss,
                )
            ),
        )
    }

    val alertDialogManager = remember(currentUIViewController) {
        AlertDialogManager(
            parentUIViewController = currentUIViewController,
            onDismissRequest = onConfirm,
            iosProperties = createIosDialogProperties(),
            properties = properties,
        )
    }

    LaunchedEffect(
        onConfirm,
        onDismiss,
        confirmText,
        dismissText,
        title,
        text,
        iosDialogStyle,
        iosConfirmButtonStyle,
        iosDismissButtonStyle,
        iosConfirmButtonIsPreferred,
        properties,
    ) {
        alertDialogManager.onDismissRequest = onDismiss
        alertDialogManager.iosProperties = createIosDialogProperties()
        alertDialogManager.properties = properties
    }

    DisposableEffect(Unit) {
        alertDialogManager.showAlertDialog()

        onDispose {
            alertDialogManager.dismiss()
        }
    }
}

@OptIn(InternalCalfApi::class)
@Composable
actual fun AdaptiveBasicAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    iosProperties: AlertDialogIosProperties,
    properties: DialogProperties,
    materialContent: @Composable () -> Unit,
) {
    val currentUIViewController = LocalUIViewController.current

    val alertDialogManager = remember(currentUIViewController) {
        AlertDialogManager(
            parentUIViewController = currentUIViewController,
            onDismissRequest = onDismissRequest,
            iosProperties = iosProperties,
            properties = properties,
        )
    }

    LaunchedEffect(onDismissRequest, iosProperties, properties) {
        alertDialogManager.onDismissRequest = onDismissRequest
        alertDialogManager.iosProperties = iosProperties
        alertDialogManager.properties = properties
    }

    DisposableEffect(Unit) {
        alertDialogManager.showAlertDialog()

        onDispose {
            alertDialogManager.dismiss()
        }
    }
}