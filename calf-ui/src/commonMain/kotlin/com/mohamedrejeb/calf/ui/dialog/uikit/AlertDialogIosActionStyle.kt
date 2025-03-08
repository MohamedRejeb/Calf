package com.mohamedrejeb.calf.ui.dialog.uikit

/**
 * Represents the style of an action in an iOS alert dialog.
 *
 * See [UIAlertActionStyle](https://developer.apple.com/documentation/uikit/uialertaction/style-swift.property?language=objc)
 */
enum class AlertDialogIosActionStyle  {
    /**
     * Apply the default style to the action’s button.
     */
    Default,
    /**
     * Apply a style that indicates the action cancels the operation and leaves things unchanged.
     */
    Cancel,
    /**
     * Apply a style that indicates the action might change or delete data.
     */
    Destructive
}