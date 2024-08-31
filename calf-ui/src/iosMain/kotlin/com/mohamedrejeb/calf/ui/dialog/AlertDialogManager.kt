package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.ui.window.DialogProperties
import com.mohamedrejeb.calf.core.InternalCalfApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.*
import platform.objc.sel_registerName

/**
 * Manages the [UIAlertController] and its actions.
 *
 * @param onConfirm Lambda that is invoked when the confirm button is clicked.
 * @param onDismiss Lambda that is invoked when the dismiss button is clicked.
 * @param confirmText The text of the confirm button.
 * @param dismissText The text of the dismiss button.
 * @param title The title of the dialog.
 * @param text The text of the dialog.
 * @param properties The properties of the dialog.
 * @param parentUIViewController The [UIViewController] that will present the dialog.
 * @constructor Creates an [AlertDialogManager] instance.
 * @see [UIAlertController]
 */
@InternalCalfApi
class AlertDialogManager internal constructor(
    private val parentUIViewController: UIViewController,
    internal var onConfirm: () -> Unit,
    internal var onDismiss: () -> Unit,
    internal var confirmText: String,
    internal var dismissText: String,
    internal var title: String,
    internal var text: String,
    internal var properties: DialogProperties,
) {

    /**
     * Indicates whether the dialog is presented or not.
     */
    private var isPresented = false

    /**
     * Indicates whether the dialog is animating or not.
     */
    private var isAnimating = false

    /**
     * The ui view controller that is used to present the dialog.
     */
    private val dialogUIViewController: UIViewController by lazy {
        UIAlertController.alertControllerWithTitle(
            title = title,
            message = text,
            preferredStyle = UIAlertControllerStyleAlert
        ).apply {
            val confirmAction = UIAlertAction.actionWithTitle(
                title = confirmText,
                style = UIAlertActionStyleDefault,
                handler = {
                    onConfirm()
                }
            )
            addAction(confirmAction)

            val cancelAction = UIAlertAction.actionWithTitle(
                title = dismissText,
                style = UIAlertActionStyleDestructive,
                handler = {
                    onDismiss()
                }
            )
            addAction(cancelAction)
        }
    }

    /**
     * Lambda that dismisses the dialog.
     */
    private val onDismissLambda: (() -> Unit) = {
        dialogUIViewController.dismissViewControllerAnimated(
            flag = true,
            completion = {
                isPresented = false
                isAnimating = false
                onDismiss()
            }
        )
    }

    /**
     * Pointer to the [dismiss] method.
     */
    @OptIn(ExperimentalForeignApi::class)
    private val dismissPointer = sel_registerName("dismiss")

    /**
     * Dismisses the dialog.
     */
    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun dismiss() {
        if (!isPresented || isAnimating) return
        isAnimating = true
        onDismissLambda.invoke()
    }

    /**
     * Shows the dialog.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun showAlertDialog() {
        if (isPresented || isAnimating) return
        isAnimating = true

        parentUIViewController.presentViewController(
            viewControllerToPresent = dialogUIViewController,
            animated = true,
            completion = {
                isPresented = true
                isAnimating = false

                if (properties.dismissOnClickOutside) {
                    dialogUIViewController.view.superview?.setUserInteractionEnabled(true)
                    dialogUIViewController.view.superview?.addGestureRecognizer(
                        UITapGestureRecognizer(
                            target = this,
                            action = dismissPointer
                        )
                    )
                }
            }
        )
    }
}