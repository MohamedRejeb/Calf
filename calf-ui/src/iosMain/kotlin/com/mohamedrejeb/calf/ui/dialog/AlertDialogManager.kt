package com.mohamedrejeb.calf.ui.dialog

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ForeignException
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
 * @param uiViewController The [UIViewController] that will present the dialog.
 * @constructor Creates an [AlertDialogManager] instance.
 * @see [UIAlertController]
 */
class AlertDialogManager internal constructor(
    internal var onConfirm: () -> Unit,
    internal var onDismiss: () -> Unit,
    internal var confirmText: String,
    internal var dismissText: String,
    internal var title: String,
    internal var text: String,
    internal var properties: DialogProperties,
) {
    private val alertController = UIAlertController.alertControllerWithTitle(
        title = title,
        message = text,
        preferredStyle = UIAlertControllerStyleAlert
    )

    /**
     * Indicates whether the dialog is presented or not.
     */
    private var isPresented = false

    /**
     * Indicates whether the dialog is animating or not.
     */
    private var isAnimating = false

    /**
     * Lambda that dismisses the dialog.
     */
    private val onDismissLambda: (() -> Unit) = {
        alertController.dismissViewControllerAnimated(
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
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private val dismissPointer get() = try {
        sel_registerName("dismiss")
    } catch (e: ForeignException) {
        println("failed")
        null
    } catch (e: Exception) {
        println("exception")
        null
    }

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
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    fun showAlertDialog() {
        if (isPresented || isAnimating) return
        isAnimating = true

        val alertController = UIAlertController.alertControllerWithTitle(
            title = title,
            message = text,
            preferredStyle = UIAlertControllerStyleAlert
        )

        val confirmAction = UIAlertAction.actionWithTitle(
            title = confirmText,
            style = UIAlertActionStyleDefault,
            handler = {
                onConfirm()
            }
        )
        alertController.addAction(confirmAction)

        val cancelAction = UIAlertAction.actionWithTitle(
            title = dismissText,
            style = UIAlertActionStyleDestructive,
            handler = {
                onDismiss()
            }
        )
        alertController.addAction(cancelAction)

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            viewControllerToPresent = alertController,
            animated = true,
            completion = {
                isPresented = true
                isAnimating = false

                if (properties.dismissOnClickOutside) {
                    alertController.view.superview?.setUserInteractionEnabled(true)
                    try {
                        alertController.view.superview?.addGestureRecognizer(
                            UITapGestureRecognizer(
                                target = this,
                                action = dismissPointer
                            )
                        )
                    } catch (e: ForeignException) {
                        println("failed")
                    } catch (e: Exception) {
                        println("exception")
                    }
                }
            }
        )
    }
}