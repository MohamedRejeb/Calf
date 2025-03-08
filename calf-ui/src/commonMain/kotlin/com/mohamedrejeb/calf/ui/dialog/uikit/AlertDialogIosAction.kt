package com.mohamedrejeb.calf.ui.dialog.uikit

/**
 * Represents an action in an iOS alert dialog.
 *
 * @param title The title of the action.
 * @param style The style of the action.
 * @param onClick The lambda that is invoked when the action is clicked.
 * @param isPreferred Indicates whether the action is the preferred action or not.
 * @param enabled Indicates whether the action is enabled or not.
 *
 *
 * See [UIAlertAction](https://developer.apple.com/documentation/uikit/uialertaction?language=objc)
 */
data class AlertDialogIosAction(
    val title: String,
    val style: AlertDialogIosActionStyle,
    val onClick: () -> Unit,
    val isPreferred: Boolean = false,
    val enabled: Boolean = true,
)