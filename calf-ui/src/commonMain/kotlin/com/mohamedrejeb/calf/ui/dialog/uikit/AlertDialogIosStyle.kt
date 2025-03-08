package com.mohamedrejeb.calf.ui.dialog.uikit

/**
 * Represents the style of an iOS alert dialog.
 *
 * See [UIAlertControllerStyle](https://developer.apple.com/documentation/uikit/uialertcontroller/style?language=objc)
 */
enum class AlertDialogIosStyle  {
    /**
     * An alert displayed modally for the app.
     */
    Alert,

    /**
     * An action sheet displayed by the view controller that presented it.
     */
    ActionSheet
}