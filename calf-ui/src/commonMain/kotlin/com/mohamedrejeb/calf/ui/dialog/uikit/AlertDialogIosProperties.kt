package com.mohamedrejeb.calf.ui.dialog.uikit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * @param title The title of the dialog
 * @param text The text of the dialog
 * @param animated Whether the dialog should be animated
 * @param style The style of the dialog
 * @param severity The severity of the dialog
 * @param actions The actions of the dialog
 * @param textFields The text fields of the dialog, you can only add text fields if the style is `Alert`
 */
@Composable
fun rememberAlertDialogIosProperties(
    title: String = "",
    text: String = "",
    animated: Boolean = true,
    style: AlertDialogIosStyle = AlertDialogIosStyle.Alert,
    severity: AlertDialogIosSeverity = AlertDialogIosSeverity.Default,
    actions: List<AlertDialogIosAction> = listOf(
        AlertDialogIosAction(
            title = "OK",
            style = AlertDialogIosActionStyle.Default,
            onClick = {},
        ),
        AlertDialogIosAction(
            title = "Cancel",
            style = AlertDialogIosActionStyle.Destructive,
            onClick = {},
        )
    ),
    textFields: List<AlertDialogIosTextField> = emptyList(),
): AlertDialogIosProperties {
    return remember(title, text, style, animated, severity, actions) {
        AlertDialogIosProperties(
            title = title,
            text = text,
            animated = animated,
            style = style,
            severity = severity,
            actions = actions,
            textFields = textFields,
        )
    }
}

/**
 * @param title The title of the dialog
 * @param text The text of the dialog
 * @param animated Whether the dialog should be animated
 * @param style The style of the dialog
 * @param severity The severity of the dialog
 * @param actions The actions of the dialog
 * @param textFields The text fields of the dialog, you can only add text fields if the style is `Alert`.
 * The text fields are in the order in which you added them to the alert controller. This order also corresponds to the order in which they are displayed in the alert.
 */
data class AlertDialogIosProperties(
    val title: String = "",
    val text: String = "",
    val animated: Boolean = true,
    val style: AlertDialogIosStyle = AlertDialogIosStyle.Alert,
    val severity: AlertDialogIosSeverity = AlertDialogIosSeverity.Default,
    val actions: List<AlertDialogIosAction> = listOf(
        AlertDialogIosAction(
            title = "OK",
            style = AlertDialogIosActionStyle.Default,
            onClick = {},
        ),
        AlertDialogIosAction(
            title = "Cancel",
            style = AlertDialogIosActionStyle.Destructive,
            onClick = {},
        )
    ),
    val textFields: List<AlertDialogIosTextField> = emptyList(),
)
